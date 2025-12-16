package com.findr.findr.fragments

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.findr.findr.R
import androidx.camera.view.PreviewView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import kotlin.math.max

class CameraFragment : Fragment() {

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView

    private var lensFacing = CameraSelector.LENS_FACING_BACK // Track current camera

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        previewView = view.findViewById(R.id.previewView)
        val captureButton = view.findViewById<Button>(R.id.captureButton)


        //the camera capture code needs to run on a new thread
        cameraExecutor = Executors.newSingleThreadExecutor()

        captureButton.setOnClickListener { takePhoto() }


        //this basically is a fragment wide listener that detects double-taps to switch cameras
        //it builds the GestureDetector class then modifies it using lambda function and overrides the
        //onDoubleTap function
        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                toggleCamera()
                return true
            }
        })
        //this captures the touches in the view aka the video feed on the ui and passes them to the gestureDetector.
        //cuz the gesture detector has no way to access screen events by itself
        previewView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
    }

    private fun requestPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraFragment", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        //ATTENTION -- NEEDS TO CLOSE DOWN THE EXTRA THREAD!!!!!!!!!!
        cameraExecutor.shutdown()
    }

    private fun takePhoto() {
        val rawFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "IMG_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(rawFile).build()

        imageCapture?.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    lifecycleScope.launch {
                        // Rotate + downscale to 1080p — NO MIRRORING
                        val processedFile = processPhotoUpright1080p(
                            requireContext(),
                            rawFile
                        )

                        val previewFragment = PhotoPreviewFragment.newInstance(processedFile.absolutePath)

                        parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, previewFragment).addToBackStack(null).commit()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Capture Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    suspend fun processPhotoUpright1080p(context: Context, inputFile: File): File = withContext(Dispatchers.IO) {

        val exif = ExifInterface(inputFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        // Decode efficiently to max 1080p
        val bitmap = decodeDownscaledBitmap(inputFile, 1080)

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val rotated = Bitmap.createBitmap(
            bitmap,
            0, 0,
            bitmap.width, bitmap.height,
            matrix,
            true
        )

        val outFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "PROCESSED_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(outFile).use { stream ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            stream.flush()
        }

        bitmap.recycle()
        rotated.recycle()

        return@withContext outFile
    }

    private fun decodeDownscaledBitmap(file: File, maxSize: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)

        val maxDim = max(options.outWidth, options.outHeight)
        var sample = 1

        if (maxDim > maxSize) {
            sample = maxDim / maxSize
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = sample
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        return BitmapFactory.decodeFile(file.absolutePath, options)
    }


}


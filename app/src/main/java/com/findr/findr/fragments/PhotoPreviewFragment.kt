package com.findr.findr.fragments

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.fragment.app.Fragment
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.config.LocationConfig
import com.findr.findr.entity.LocationData
import com.findr.findr.entity.Post
import com.google.android.gms.common.api.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

class PhotoPreviewFragment : Fragment() {

    companion object {
        private const val ARG_PHOTO_PATH = "photo_path"

        fun newInstance(photoPath: String): PhotoPreviewFragment {
            val fragment = PhotoPreviewFragment()
            val args = Bundle()
            args.putString(ARG_PHOTO_PATH, photoPath)
            fragment.arguments = args
            return fragment
        }
    }

    private var photoPath: String? = null
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoPath = arguments?.getString(ARG_PHOTO_PATH)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo_preview, container, false)
        imageView = view.findViewById(R.id.photoImageView)
        uploadButton = view.findViewById(R.id.uploadButton)

        photoPath?.let {
            val bitmap = getRotatedBitmap(it)
            imageView.setImageBitmap(bitmap)
        } ?: run {
            Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
        }

        uploadButton.setOnClickListener {

            //all api work must be done within coroutines
            CoroutineScope(Dispatchers.IO).launch {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Upload clicked for $photoPath",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                uploadPostWithImage()
            }
        }


        val saveButton = view.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            photoPath?.let {
                saveToCameraRoll(it)
            }
        }

        return view
    }

    //the sole purpose of ovveriding the ondestory function is to call deletetempimage
    override fun onDestroyView() {
        super.onDestroyView()
        deleteTempImage()
    }




    //this is a cleanup function to prevent leftover photos from building up in the apps temp directory
    private fun deleteTempImage() {
        photoPath?.let {
            val file = File(it)
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) {
                    Log.w("PhotoPreviewFragment", "Failed to delete temp image at $it")
                } else {
                    Log.d("PhotoPreviewFragment", "Temp image deleted: $it")
                }
            }
        }
    }

    //this function is necessary becuase it wants to store the photos sideways
    private fun getRotatedBitmap(filePath: String): Bitmap {
        val originalBitmap = BitmapFactory.decodeFile(filePath)

        val ei = try {
            ExifInterface(filePath)
        } catch (e: IOException) {
            e.printStackTrace()
            return originalBitmap
        }

        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        if (rotationDegrees == 0) return originalBitmap

        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }

        return Bitmap.createBitmap(
            originalBitmap, 0, 0,
            originalBitmap.width, originalBitmap.height, matrix, true
        )
    }

    private fun saveToCameraRoll(filePath: String) {
        val resolver = requireContext().contentResolver
        val file = File(filePath)
        val fileName = file.name
        val mimeType = "image/jpeg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
                put(MediaStore.Images.Media.DATA, "$picturesDir/$fileName")
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            resolver.openOutputStream(imageUri).use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }

            Toast.makeText(context, "Saved to Camera Roll", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }


    //this is what uploads the photo to the api when the upload button is clicked
    //it looks insanely humongous and complicated but 50 out of the 70 some lines are error handling and checking
    private fun uploadPostWithImage() {
        if (photoPath == null) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "No photo to upload", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val file = File(photoPath!!)
        if (!file.exists()) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Photo file does not exist", Toast.LENGTH_SHORT)
                    .show()
            }
            return
        }

        val descriptionText = view?.findViewById<EditText>(R.id.descriptionEditText)?.text?.toString() ?: "DEFAULT"

        // For example, get author username from somewhere (logged-in user)
        val authorUsername = RetrofitClient.getCurrentUsername()

        // LocationData object - replace with actual location from your app
        val location = LocationConfig(requireContext()).preciseLocation
        val currentLocation = LocationData(longitude = location.longitude, latitude = location.latitude)

        // Prepare file part
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        // Prepare other parts as RequestBody (text/plain)
        val authorPart = RequestBody.create("text/plain".toMediaTypeOrNull(), authorUsername)
        val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), descriptionText)
        val longitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), currentLocation.longitude.toString())
        val latitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), currentLocation.latitude.toString())

        val apiService = RetrofitClient.getInstanceWithoutNewAuth().create(ApiService::class.java)
        apiService.createPostWithImage(imagePart, authorPart, descriptionPart, longitudePart, latitudePart)
            .enqueue(object : retrofit2.Callback<Post> {
                override fun onResponse(call: retrofit2.Call<Post>, response: retrofit2.Response<Post>) {
                    Log.d("Server Responses", response.toString())
                    if (response.isSuccessful) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Post uploaded successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        parentFragmentManager.popBackStack()
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Upload failed: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<Post>, t: Throwable) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Upload error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

}

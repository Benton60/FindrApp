package com.findr.findr.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.findr.findr.R
import java.io.File

class PhotoPreviewFragment : Fragment() {

    private var photoPath: String? = null

    companion object {
        fun newInstance(photoPath: String): PhotoPreviewFragment {
            val fragment = PhotoPreviewFragment()
            val args = Bundle()
            args.putString("photoPath", photoPath)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoPath = arguments?.getString("photoPath")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo_preview, container, false)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val descriptionEditText = view.findViewById<EditText>(R.id.descriptionEditText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        // Load and display the captured photo
        photoPath?.let {
            val imgFile = File(it)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }

        // Handle Post button
        saveButton.setOnClickListener {
            // Save or upload the data here...

            // Go back to CameraFragment
            parentFragmentManager.popBackStack()
        }

        // Disable back button manually
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Do nothing to prevent accidental back navigation
        }

        return view
    }
}

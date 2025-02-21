package com.findr.findr

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import com.findr.findr.api.ApiService
import java.util.concurrent.ExecutorService

class CameraFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        return view
    }

}

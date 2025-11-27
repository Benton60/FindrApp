package com.findr.findr.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.findr.findr.R

class ProfileViewerFragment : Fragment() {

    // Key for arguments
    companion object {
        private const val ARG_USERNAME = "username"

        // Factory method to create a new instance of this fragment with a username
        fun newInstance(username: String): ProfileViewerFragment {
            val fragment = ProfileViewerFragment()
            val args = Bundle()
            args.putString(ARG_USERNAME, username)
            fragment.arguments = args
            return fragment
        }
    }

    // Store the username in a variable
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve username from arguments
        username = arguments?.getString(ARG_USERNAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_viewer, container, false)
    }
}

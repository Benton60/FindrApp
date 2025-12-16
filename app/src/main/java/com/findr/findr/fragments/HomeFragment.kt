package com.findr.findr.fragments



//this fragment displays the users list of friends and posts by location
import PostsViewModel
import android.content.Intent
import androidx.fragment.app.viewModels
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.GradientDrawable
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.findr.findr.InternetLessActivity
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.config.LocationConfig
import com.findr.findr.entity.User
import com.findr.findr.repositories.PostsRepository
import com.findr.findr.adapters.PostsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File

class HomeFragment(private val retrofitClient: ApiService) : Fragment(R.layout.fragment_home) {



    private val viewModel: PostsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = PostsRepository(retrofitClient)
                val locationConfig = LocationConfig(context)
                @Suppress("UNCHECKED_CAST")
                return PostsViewModel(repository, requireContext()) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Note: ensure fragment_home.xml uses a RecyclerView with id recyclerViewPosts
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView for Posts
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPosts)
        val adapter = PostsAdapter(
            api = retrofitClient,
            rotateFn = { file -> rotateBitmapByExif(file, BitmapFactory.decodeFile(file.absolutePath)) },
            loadMoreCallback = { viewModel.loadMore() },
            onAuthorClick = { username ->
                // Create fragment and pass arguments
                val fragment = ProfileViewerFragment(retrofitClient).apply {
                    arguments = Bundle().apply {
                        putString("username", username)
                    }
                }

                // Launch fragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe posts flow and submit to adapter
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.posts.collect { list ->
                adapter.submitList(list)
            }
        }

        // Trigger initial load
        viewModel.loadInitial()

        // Still load friends as before
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getFriends()
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Unable to access location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private suspend fun getFriends(){
        CoroutineScope(Dispatchers.IO).launch {
            val friendsContainer = view?.findViewById<LinearLayout>(R.id.friendsContainer)
            try {
                val friends: List<User> = retrofitClient.getFriendsByUsername(RetrofitClient.getCurrentUsername())

                for (friend in friends) {
                    // Load the profile photo
                    var profilePic: ResponseBody? = null
                    try {
                        profilePic = retrofitClient.downloadProfilePhoto(friend.username)
                    } catch (e: Exception) {
                        Log.e("ProfilePic", "Failed to download profile picture for ${friend.username}", e)
                    }

                    // Inflate friend item layout
                    val friendView = LayoutInflater.from(context).inflate(R.layout.item_friend, friendsContainer, false)
                    Log.d("Friends", friends.size.toString())

                    friendView.findViewById<TextView>(R.id.friendName).text = friend.username

                    // Change the circle around the user to green
                    val strokeWidth = (3 * resources.displayMetrics.density).toInt() // Convert 3 pixels to 3dp
                    val drawable = friendView.findViewById<View>(R.id.color_border).background as GradientDrawable
                    drawable.setStroke(strokeWidth, getColor(requireContext(), R.color.green))

                    val friendImageView = friendView.findViewById<ImageView>(R.id.friendImage)

                    // Launch a coroutine for UI update
                    CoroutineScope(Dispatchers.Main).launch {
                        // If profile picture exists, decode and set it
                        if (profilePic != null) {
                            val bitmap = BitmapFactory.decodeStream(profilePic.byteStream())
                            if (bitmap != null) {
                                friendImageView.setImageBitmap(bitmap)
                            } else {
                                Log.e("ProfilePic", "Failed to decode bitmap for ${friend.username}")
                            }
                        } else {
                            Log.w("ProfilePic", "No profile picture found for ${friend.username}, using default")
                        }


                        //this launches the profile_viewer fragment when the user is clicked
                        friendView.setOnClickListener {
                            val clickedUsername = friend.username
                            val fragment = ProfileViewerFragment.newInstance(clickedUsername, retrofitClient)
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null)
                                .commit()
                        }

                        friendsContainer?.addView(friendView)
                        Log.d("Friends", friend.username)
                    }
                }
            } catch (e: Exception) {
                if (isAdded) {
                    startActivity(Intent(requireContext(), InternetLessActivity::class.java))
                }
            }
        }
    }

    //helper functions

    //this function is necessary because it reads the photo metadata and orients the picture correctly regardless of which
    //camera it was taken on. I was having difficulty because the front camera on some phones is mirrored
    //it uses the exif portion of the metadata. It is slightly more time costly to do this but i didn't notice a difference
    //and in my opinion its necessary
    fun rotateBitmapByExif(file: File, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)

            ExifInterface.ORIENTATION_TRANSPOSE -> { // flip + rotate 90
                matrix.preScale(-1f, 1f)
                matrix.postRotate(90f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> { // flip + rotate 270
                matrix.preScale(-1f, 1f)
                matrix.postRotate(270f)
            }
        }

        return Bitmap.createBitmap(
            bitmap,
            0, 0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

}
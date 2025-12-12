package com.findr.findr.fragments

//this fragment is dedicated to displaying information about a specific user.
//this includes a list of friends, their age, their name, and a description, as well as their posts
//also this fragment is really similar to the home fragment so if you have a question on a bit of code in here you can probably
//find more documentation in that file as i wrote it first.

import PostsViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.GradientDrawable
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.config.LocationConfig
import com.findr.findr.entity.Post
import com.findr.findr.entity.User
import com.findr.findr.repository.PostsRepository
import com.findr.findr.ui.PostsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File

class ProfileViewerFragment(private val retrofitClient: ApiService) : Fragment(R.layout.fragment_profile_viewer) {

    // Key for arguments
    companion object {
        private const val ARG_USERNAME = "username"

        // Factory method to create a new instance of this fragment with a username
        fun newInstance(username: String, retrofitClient: ApiService): ProfileViewerFragment {
            val fragment = ProfileViewerFragment(retrofitClient)
            val args = Bundle()
            args.putString(ARG_USERNAME, username)
            fragment.arguments = args
            return fragment
        }
    }

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

    // Store the username in a variable
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve username from arguments
        username = arguments?.getString(ARG_USERNAME)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Setup RecyclerView for Posts
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPostsProfileViewer)
        val adapter = PostsAdapter(
            api = retrofitClient,
            rotateFn = { file -> rotateBitmapByExif(file, BitmapFactory.decodeFile(file.absolutePath)) },
            loadMoreCallback = { viewModel.loadMore() },
            onAuthorClick = { username ->
                //okay so normally this would open a ProfileViewerFragment but
                //in this case the posts possibly displayed are already this Profiles posts
                //so clicking on the author would only open a new instance of ProfileViewerFragment
                //of the exact same Profile. which just builds up on the stack and ram usage.
                //so for the ProfileViewerFragment implementation only the onAuthorClick does nothing
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
        viewModel.loadInitial(username = username)

        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getFriends()
                loadUserProfile()
                setupFriendButton()
            }catch(e: SecurityException){
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(requireContext(), "Unable to access location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun loadUserProfile() {
        CoroutineScope(Dispatchers.IO).launch {
            // Download profile picture
            var profilePic: ResponseBody? = null
            try {
                profilePic = retrofitClient.downloadProfilePhoto(username.toString())
            } catch (e: Exception) {
                Log.e("ProfilePic", "Failed to download profile picture for $username", e)
            }

            // Get user info
            var user: User? = null
            try {
                user = retrofitClient.getUserByUsername(username.toString())
            } catch (e: Exception) {
                Log.e("UserFetch", "Failed to get user info for ${username.toString()}", e)
            }

            // Switch to main thread to update UI
            withContext(Dispatchers.Main) {
                val profilePicture = view?.findViewById<ImageView>(R.id.profileImageProfileView)
                val tvName = view?.findViewById<TextView>(R.id.tvNameProfileView)
                val tvUsername = view?.findViewById<TextView>(R.id.tvUsernameProfileView)
                val tvAge = view?.findViewById<TextView>(R.id.tvAgeProfileView)
                val tvDescription = view?.findViewById<TextView>(R.id.tvDescriptionProfileView)

                // Set user info
                tvName?.text = user?.name ?: "Unknown Name"
                tvUsername?.text = user?.username ?: "@unknown"
                tvAge?.text = user?.age?.toString() ?: "N/A"
                tvDescription?.text = user?.description ?: ""



                // Set profile picture
                if (profilePic != null) {
                    val bitmap = BitmapFactory.decodeStream(profilePic.byteStream())
                    if (bitmap != null) {
                        profilePicture?.setImageBitmap(bitmap)
                    } else {
                        Log.e("ProfilePic", "Failed to decode bitmap for ${username.toString()}")
                    }
                } else {
                    Log.w("ProfilePic", "No profile picture found for ${username.toString()}, using default")
                }
            }
        }
    }
    private suspend fun getFriends(){
        CoroutineScope(Dispatchers.IO).launch {
            val friendsContainer = view?.findViewById<LinearLayout>(R.id.friendsContainerProfileViewer)
            try {
                val friends: List<User> = retrofitClient.getFriendsByUsername(username.toString())

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
                //TODO -- make it jump to the no internet activity
            }
        }
    }


    //this function holds all logic relating to adding/removing friendships
    private suspend fun setupFriendButton(){
        var btnFriends = view?.findViewById<Button>(R.id.btnFriendProfileView)

        //this makes so you cant friend yourself
        if(username.toString() == RetrofitClient.getCurrentUsername().toString()){
            btnFriends?.visibility = View.GONE
        }

        if (btnFriends != null) {
            btnFriends.setOnClickListener{
                if(btnFriends.text == "Remove Friend"){
                    CoroutineScope(Dispatchers.IO).launch {
                        retrofitClient.removeFriend(username.toString())
                    }
                    btnFriends?.text = "Add Friend"
                }else if(btnFriends.text == "Add Friend"){
                    CoroutineScope(Dispatchers.IO).launch {
                        retrofitClient.addFriend(username.toString())
                    }
                    btnFriends?.text = "Remove Friend"
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            if(retrofitClient.checkFriendshipStatus(username.toString())){
                withContext(Dispatchers.Main) {
                    btnFriends?.text = "Remove Friend"
                }
            }else{
                withContext(Dispatchers.Main) {
                    btnFriends?.text = "Add Friend"
                }
            }
        }
    }

    //helper functions

    //this function is necessary becuase it reads the photo metadata and orients the picture correctly regardless of which
    //camera it was taken on. I was having difficulty becuase the front camera on some phones is mirrored
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

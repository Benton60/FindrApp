package com.findr.findr.fragments



//this fragment displays the users list of friends and posts by location
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.GradientDrawable
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.config.LocationConfig
import com.findr.findr.entity.Post
import com.findr.findr.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File

class HomeFragment(private val retrofitClient: ApiService) : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getPosts()
                getFriends()
            }catch(e: SecurityException){
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(requireContext(), "Unable to access location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    //both functions work very similarly to retrieve photos/users from the api and display them.
    private suspend fun getPosts() {

        //the getPosts() function uses the "byLocation" endpoint this is the endpoint that uses a location based algorithm to determine
        //which posts to serve the user


        CoroutineScope(Dispatchers.IO).launch{
            val postsContainer = view?.findViewById<LinearLayout>(R.id.postsContainer)

            try {
                val location = LocationConfig(context).roughLocation
                val posts: List<Post> =
                    retrofitClient.getPostsByLocation(location.longitude, location.latitude)

                for (post in posts) {

                    var postPic: ResponseBody?

                    try {
                        postPic = retrofitClient.downloadPostPhoto(
                            post.photoPath.replace("\\", " ")
                        )
                        // Inflate post view
                        val postView = LayoutInflater.from(context)
                            .inflate(R.layout.item_post, postsContainer, false)

                        postView.findViewById<TextView>(R.id.postDescription).text =
                            post.description

                        //author gets a variable assignment because it gets an onclick listener later
                        val author = postView.findViewById<TextView>(R.id.postAuthor)
                        author.text = post.author


                        val postImageView = postView.findViewById<ImageView>(R.id.postImage)
                        //this entire coroutinescope is dedicated to orienting the picture correctly
                        withContext(Dispatchers.Main) {
                            if (postPic != null) {

                                // -------------------------------
                                // STEP 1: Save image to temp file
                                // -------------------------------
                                val tempFile = File.createTempFile("post_", ".jpg", context?.cacheDir)
                                tempFile.outputStream().use { out ->
                                    out.write(postPic.bytes())
                                }

                                // -------------------------------
                                // STEP 2: Decode bitmap
                                // -------------------------------
                                val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)

                                if (bitmap != null) {

                                    // -------------------------------
                                    // STEP 3: Rotate based on EXIF
                                    // -------------------------------
                                    val correctedBitmap =
                                        rotateBitmapByExif(tempFile, bitmap)

                                    postImageView.setImageBitmap(correctedBitmap)

                                } else {
                                    Log.e("PostPic", "Failed to decode bitmap for ${post.id}")
                                }
                            }

                            postsContainer?.addView(postView)
                        }



                        //this is to check whether the post has been like previously and set the icon_background accordingly
                        //this is wrapped in a coroutinescope to separate it from the rest of the post loading runtime. likes should be checked concurrently not linearly
                        //to help with perceived latency
                        val postHeart = postView.findViewById<ImageButton>(R.id.postHeart)

                        val isLiked = withContext(Dispatchers.IO) { retrofitClient.checkLike(post.id) }

                        //set background
                        withContext(Dispatchers.Main) {
                            if (isLiked) {
                                postHeart.setBackgroundResource(R.drawable.ic_heart_filled)
                            } else {
                                postHeart.setBackgroundResource(R.drawable.ic_heart)
                            }
                        }
                        postHeart.tag = isLiked

                        postHeart.setOnClickListener { btn ->
                            val liked = btn.tag as Boolean
                            btn.tag = !liked // toggle

                            if (!liked) {
                                postHeart.setBackgroundResource(R.drawable.ic_heart_filled)
                            } else {
                                postHeart.setBackgroundResource(R.drawable.ic_heart)
                            }

                            // Fire-and-forget network request
                            CoroutineScope(Dispatchers.IO).launch {
                                if (!liked) {
                                    retrofitClient.addLike(post.id)
                                } else {
                                    retrofitClient.removeLike(post.id)
                                }
                            }
                        }


                        //I put the onClickListener for the authors name last because i want as much time as possible
                        //for the other stuff to load to help prevent memory leaks. it shouldn't leak memory anyways but
                        //just in case
                        withContext(Dispatchers.Main) {
                            author.setOnClickListener {
                                val clickedUsername = author.text
                                val fragment = ProfileViewerFragment.newInstance(
                                    clickedUsername.toString(),
                                    retrofitClient
                                )
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainer, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("PostPic",
                            "Failed to download or decode post picture for ${post.photoPath}",
                            e
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("Posts", "Failed to load posts", e)
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
                        profilePic = retrofitClient.downloadProfilePhoto(friend.username, "profile.png")
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
                //TODO -- make it jump to the no internet activity
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

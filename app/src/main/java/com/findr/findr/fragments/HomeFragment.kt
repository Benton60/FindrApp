package com.findr.findr.fragments
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
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
import org.w3c.dom.Text

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


    //both functions work very similarly to retrieve photos from the api and display them.
    private suspend fun getPosts(){
        CoroutineScope(Dispatchers.IO).launch {
            val postsContainer = view?.findViewById<LinearLayout>(R.id.postsContainer)
            try{
                //retrieve the posts objects as a list of posts.
                //this is sent and received as json so there are no images those are retrieved later
                val location = LocationConfig(context).roughLocation
                val posts: List<Post> = retrofitClient.getPostsByLocation(location.longitude, location.latitude)

                for(post in posts){
                    //load the photo associated with the post
                    var postPic: ResponseBody? = null
                    try{

                        //this line retrieves the image from the api. the replace call is because the \ in the file url messes with the api endpoint
                        postPic = retrofitClient.downloadPostPhoto(post.photoPath.replace("\\"," "))
                        Log.d("PostPics", postPic.toString())

                        //this generates the postView we will add to the linear layout
                        val postView = LayoutInflater.from(context).inflate(R.layout.item_post, postsContainer, false)
                        Log.d("Posts", posts.size.toString())

                        //sets the description in the post view
                        postView.findViewById<TextView>(R.id.postDescription).text = post.author //Yes i Know they're flipped i don't know why.
                        postView.findViewById<TextView>(R.id.postAuthor).text = post.description + ":" //also its easily worked around so ill deal with it later
                                                                                                 //TODO -- see comments above


                        //retrieves the image view to use later
                        val postImageView = postView.findViewById<ImageView>(R.id.postImage)

                        // Launch a coroutine for UI update
                        CoroutineScope(Dispatchers.Main).launch {
                            // If post picture exists, decode and set it
                            if (postPic != null) {
                                val bitmap = BitmapFactory.decodeStream(postPic.byteStream())
                                if (bitmap != null) {
                                    postImageView.setImageBitmap(bitmap)
                                } else {
                                    Log.e("PostPic", "Failed to decode bitmap for ${post.id}")
                                }
                            } else {
                                Log.w("PostPic", "No profile picture found for ${post.id}, using default")
                            }

                            postsContainer?.addView(postView)
                            Log.d("Posts", post.id.toString())
                        }

                    }catch (e: Exception){
                        Log.e("PostPic", "Failed to download post picture for ${post.photoPath}", e)
                    }
                }
            }catch (e: Exception){
                //TODO -- make it jump to the no internet activity
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

                        friendsContainer?.addView(friendView)
                        Log.d("Friends", friend.username)
                    }
                }
            } catch (e: Exception) {
                //TODO -- make it jump to the no internet activity
            }
        }
    }
}

package com.findr.findr.adapters

import android.content.Context
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.entity.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File



//this class is the adapter that takes the Post and the Container that it will be stored in and maps
//each element in the container to a field in the PostViewHolder so they can be accessed by the
//code in the PostsViewModel Class.
class PostsAdapter(
    private val api: ApiService,
    private val rotateFn: (File) -> android.graphics.Bitmap,   // rotated bitmap helper
    private val loadMoreCallback: () -> Unit,
    private val onAuthorClick: (String) -> Unit,    // Pass the username to the callback
    private val context: Context
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(old: Post, new: Post) = old.id == new.id
        override fun areContentsTheSame(old: Post, new: Post) = old.id == new.id
    }

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.postImage)
        val author: TextView = view.findViewById(R.id.postAuthor)
        val description: TextView = view.findViewById(R.id.postDescription)
        val heart: ImageButton = view.findViewById(R.id.postHeart)
        val likeCount: TextView = view.findViewById(R.id.postLikeCounter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)

        holder.author.text = post.author
        holder.description.text = post.description
        holder.likeCount.text = post.likes.toString()

        // LOAD IMAGE WITHOUT GLIDE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = api.downloadPostPhoto(post.photoPath.replace("\\", " "))
                val bytes = body.bytes()

                // Create temp file
                val file = File.createTempFile("post_", ".jpg", holder.itemView.context.cacheDir)
                file.writeBytes(bytes)

                // Decode → rotate → set
                val rotatedBitmap = rotateFn(file)

                withContext(Dispatchers.Main) {
                    holder.image.setImageBitmap(rotatedBitmap)
                }
            } catch (_: Exception) { }
        }


        // LIKE BUTTON HANDLING
        //checks for like when the post is loaded
        CoroutineScope(Dispatchers.IO).launch {
            val isLiked = api.checkLike(post.id)
            withContext(Dispatchers.Main) {
                holder.heart.tag = isLiked
                holder.heart.setBackgroundResource(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                )
            }
        }
        //adds the like listener
        holder.heart.setOnClickListener { btn ->
            changeLike(btn, holder.likeCount, post)
        }


        // DOUBLE TAP TO LIKE HANDLING
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
            override fun onDoubleTap(e: MotionEvent): Boolean {
                changeLike(holder.heart, holder.likeCount, post)
                return true
            }
        })
        holder.image.setOnTouchListener{_, event ->
            gestureDetector.onTouchEvent(event)
            true
        }


        // CLICKING ON AUTHOR HANDLING
        holder.author.setOnClickListener{
            onAuthorClick(post.author) //on author click is a callback function
        }


        // PAGINATION TRIGGER
        if (position == itemCount - 1) {
            loadMoreCallback()
        }
    }


    private fun changeLike(btn: View, likeCount: TextView, post: Post){
        val liked = btn.tag as Boolean
        val newState = !liked
        btn.tag = newState


        if(newState){
            //background image
            btn.setBackgroundResource(R.drawable.ic_heart_filled)
            //api addLike call
            CoroutineScope(Dispatchers.IO).launch { api.addLike(post.id) }
            //update like count
            post.likes++
            likeCount.text = post.likes.toString()
        }else{
            //background image
            btn.setBackgroundResource(R.drawable.ic_heart)
            //api deleteLike call
            CoroutineScope(Dispatchers.IO).launch { api.removeLike(post.id) }
            //update like count
            post.likes--
            likeCount.text = post.likes.toString()
        }
    }
}

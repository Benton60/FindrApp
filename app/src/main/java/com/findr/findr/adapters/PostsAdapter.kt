package com.findr.findr.ui

import android.graphics.BitmapFactory
import android.view.LayoutInflater
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
import java.io.File

class PostsAdapter(
    private val api: ApiService,
    private val rotateFn: (File) -> android.graphics.Bitmap,   // rotated bitmap helper
    private val loadMoreCallback: () -> Unit,
    private val onAuthorClick: (String) -> Unit    // Pass the username to the callback
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

                CoroutineScope(Dispatchers.Main).launch {
                    holder.image.setImageBitmap(rotatedBitmap)
                }
            } catch (_: Exception) { }
        }

        // LIKE BUTTON HANDLING
        CoroutineScope(Dispatchers.IO).launch {
            val isLiked = api.checkLike(post.id)
            CoroutineScope(Dispatchers.Main).launch {
                holder.heart.tag = isLiked
                holder.heart.setBackgroundResource(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                )
            }
        }


        holder.heart.setOnClickListener { btn ->
            val liked = btn.tag as Boolean
            val newState = !liked
            btn.tag = newState

            btn.setBackgroundResource(
                if (newState) R.drawable.ic_heart_filled else R.drawable.ic_heart
            )

            CoroutineScope(Dispatchers.IO).launch {
                if (newState) api.addLike(post.id) else api.removeLike(post.id)
            }
        }



        // CLICKING ON AUTHOR HANDLING
        holder.author.setOnClickListener{
            onAuthorClick(post.author)
        }


        // PAGINATION TRIGGER
        if (position == itemCount - 1) {
            loadMoreCallback()
        }
    }
}

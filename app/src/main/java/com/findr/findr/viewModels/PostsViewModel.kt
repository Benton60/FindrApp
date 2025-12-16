import android.content.Context
import android.content.Intent
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.findr.findr.InternetLessActivity
import com.findr.findr.config.LocationConfig
import com.findr.findr.entity.Post
import com.findr.findr.repositories.PostsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostsViewModel(
    private val repository: PostsRepository,
    private val context: Context
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private var page = 0
    private var isLoading = false

    private var lat: Double? = null
    private var lon: Double? = null
    private var currentUsername: String? = null // If set, load posts for this user

    /** Load initial posts for feed or user profile */
    fun loadInitial(username: String? = null) {
        page = 0
        _posts.value = emptyList()
        currentUsername = username

        if (username != null) {
            // Load user-specific posts
            loadMore()
        } else {
            // Load feed by location
            viewModelScope.launch {
                val location = withContext(Dispatchers.IO) {
                    LocationConfig(context).getRoughLocation() // runs off main thread
                }
                lat = location.latitude
                lon = location.longitude

                loadMore()
            }
        }
    }

    /** Load next page of posts (for feed) or all posts (for user profile) */
    fun loadMore() {
        if (isLoading) return

        isLoading = true
        viewModelScope.launch {
            try {
                val newPosts = when {
                    currentUsername != null -> {
                        repository.getPostsByUser(page, currentUsername!!)
                    }
                    lat != null && lon != null -> {
                        repository.getPosts(page, lat!!, lon!!)
                    }
                    else -> emptyList()
                }

                if (newPosts.isNotEmpty()) {
                    _posts.value = _posts.value + newPosts
                    page++ // increment page only for feed
                }
            }finally {
                isLoading = false
            }
        }
    }
}

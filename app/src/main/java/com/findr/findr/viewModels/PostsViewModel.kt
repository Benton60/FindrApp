import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.findr.findr.config.LocationConfig
import com.findr.findr.entity.Post
import com.findr.findr.repository.PostsRepository
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

    fun loadInitial() {
        if (_posts.value.isEmpty()) {
            // Run location fetch on IO thread
            viewModelScope.launch {
                val location = withContext(Dispatchers.IO) {
                    LocationConfig(context).getRoughLocation() // runs off main thread
                }
                lat = location.latitude
                lon = location.longitude

                // Now load the first page
                loadMore()
            }
        }
    }

    fun loadMore() {
        if (isLoading) return
        if (lat == null || lon == null) return // wait for location

        isLoading = true
        viewModelScope.launch {
            try {
                val newPosts = repository.getPosts(page, lat!!, lon!!)
                if (newPosts.isNotEmpty()) {
                    _posts.value = _posts.value + newPosts
                    page++ // increment page only if posts returned
                }
            } finally {
                isLoading = false
            }
        }
    }
}

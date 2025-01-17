import com.findr.findr.entity.User
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): User
}

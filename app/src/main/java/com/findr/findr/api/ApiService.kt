import com.findr.findr.entity.Post
import com.findr.findr.entity.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    //Users
    @POST("users")
    suspend fun createUser(@Body user: User)
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): User

    //Posts
    @GET("posts/byAuthor/{author}")
    suspend fun getPosts(@Path("author") author: String): List<Post>


    //Friends
    @GET("friendships/friends/{username}")
    suspend fun getFriends(@Path("username") username: String): List<User>
    @POST("friendships/addFriend/{follower}/{followee}")
    suspend fun addFriend(@Path("follower") follower: Long, @Path("followee") followee: Long)


}

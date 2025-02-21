package com.findr.findr.api
import android.graphics.Point
import com.findr.findr.entity.LocationData
import com.findr.findr.entity.Post
import com.findr.findr.entity.User
import okhttp3.MultipartBody
import okhttp3.MultipartBody.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    //Users
    @GET("users/checkCredentials")
    suspend fun checkCredentials()
    @POST("users/updateLocation")
    suspend fun updateLocation(@Body location: LocationData)
    @POST("users/createUser")
    suspend fun createUser(@Body user: User)
    @GET("users/byUsername/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): User
    @GET("users/byID/{id}")
    suspend fun getUserByID(@Path("id") id: Long): User

    //Posts
    @POST("posts/createPost")
    suspend fun createPost(@Body post: Post)
    @GET("posts/byAuthor/{author}")
    suspend fun getPostsByAuthor(@Path("author") author: String): List<Post>
    @GET("posts/byLocation/{longitude}/{latitude}")
    suspend fun getPostsByLocation(@Path("longitude") longitude: Double, @Path("latitude") latitude: Double): List<Post>

    //Friends
    @GET("friendships/friends/{username}")
    suspend fun getFriendsByUsername(@Path("username") username: String): List<User>
    @POST("friendships/addFriend/{follower}/{followee}")
    suspend fun addFriend(@Path("follower") follower: Long, @Path("followee") followee: Long)

    //Photos
    @Multipart
    @POST("files/upload")
    fun uploadFile(@Part file: MultipartBody.Part): Call<String>

    @GET("files/download/profile/{userFolder}/{filename}")
    suspend fun downloadProfilePhoto(@Path("userFolder") userFolder: String, @Path("filename") filename: String): ResponseBody
}

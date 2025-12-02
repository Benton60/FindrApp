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



//this interface acts as the interface for the Retrofit Client class and
//provides the functions used to interact with the backend api

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
    @Multipart
    @POST("posts/createPostWithImage")
    fun createPostWithImage(@Part image: MultipartBody.Part,
                            @Part("author") author: RequestBody,
                            @Part("description") description: RequestBody,
                            @Part("longitude") longitude: RequestBody,
                            @Part("latitude") latitude: RequestBody
    ): Call<Post>
    @GET("posts/byAuthor/{author}")
    suspend fun getPostsByAuthor(@Path("author") author: String): List<Post>
    @GET("posts/byLocation/{longitude}/{latitude}")
    suspend fun getPostsByLocation(@Path("longitude") longitude: Double, @Path("latitude") latitude: Double): List<Post>

    //Friends
    @GET("friendships/friends/{username}")
    suspend fun getFriendsByUsername(@Path("username") username: String): List<User>
    @POST("friendships/addFriend/{follower}/{followee}")
    suspend fun addFriend(@Path("follower") follower: Long, @Path("followee") followee: Long)

    // Photos
    @GET("files/download/profile/{userFolder}/{filename}")
    suspend fun downloadProfilePhoto(@Path("userFolder") userFolder: String, @Path("filename") filename: String): ResponseBody
    @GET("files/download/post/{filePath}")
    suspend fun downloadPostPhoto(@Path("filePath") filePath: String): ResponseBody

    //Likes
    @GET("likes/addLike/{postID}")
    suspend fun addLike(@Path("postID") postID: Long): ResponseBody
    @GET("likes/removeLike/{postID}")
    suspend fun  removeLike(@Path("postID") postID: Long): ResponseBody
    @GET("likes/checkLike/{postID}")
    suspend fun checkLike(@Path("postID") postID: Long): Boolean
}

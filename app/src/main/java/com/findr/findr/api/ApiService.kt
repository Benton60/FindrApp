package com.findr.findr.api
import com.findr.findr.entity.Comment
import com.findr.findr.entity.LocationData
import com.findr.findr.entity.Post
import com.findr.findr.entity.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path



//this interface acts as the interface for the Retrofit Client class and
//provides the functions used to interact with the backend api

interface ApiService {



    //health
    @GET("health")
    suspend fun healthCheck(): Response<Unit>

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
    @GET("posts/byAuthor/{page}/{author}")
    suspend fun getPostsByAuthor(@Path("page") page: Int, @Path("author") author: String): List<Post>
    @GET("posts/byLocation/{longitude}/{latitude}")
    //TODO -- the get By location function is deprecated and will be dropped whenever i have time to refactor
    suspend fun getPostsByLocation(@Path("longitude") longitude: Double, @Path("latitude") latitude: Double): List<Post>
    @GET("posts/byPage/{page}/{longitude}/{latitude}")
    suspend fun getPostsByPage(@Path("page") page: Int, @Path("longitude") longitude: Double, @Path("latitude") latitude: Double): List<Post>

    //Friends
    @GET("friendships/friends/{username}")
    suspend fun getFriendsByUsername(@Path("username") username: String): List<User>
    @POST("friendships/addFriend/{username}")
    suspend fun addFriend(@Path("username") username: String)
    @DELETE("friendships/removeFriend/{username}")
    suspend fun removeFriend(@Path("username") username:String)
    @GET("friendships/checkFriendshipStatus/{username}")
    suspend fun checkFriendshipStatus(@Path("username") username: String): Boolean

    // Photos
    @GET("files/download/profile/{userFolder}")
    suspend fun downloadProfilePhoto(@Path("userFolder") userFolder: String): ResponseBody
    @GET("files/download/post/{filePath}")
    suspend fun downloadPostPhoto(@Path("filePath") filePath: String): ResponseBody
    @Multipart
    @POST("files/upload/profile/{username}")
    suspend fun uploadProfilePic(@Path("username") username: String, @Part image: MultipartBody.Part): ResponseBody

    //Likes
    @POST("likes/addLike/{postID}")
    suspend fun addLike(@Path("postID") postID: Long): ResponseBody
    @POST("likes/removeLike/{postID}")
    suspend fun  removeLike(@Path("postID") postID: Long): ResponseBody
    @GET("likes/checkLike/{postID}")
    suspend fun checkLike(@Path("postID") postID: Long): Boolean


    //Comments
    @POST("comments/createComment")
    suspend fun createComment(@Body comment: Comment): Comment
    @GET("comments/byPost/{postID}")
    suspend fun getCommentsByPostID(@Path("postID") postID: Long): List<Comment>
}

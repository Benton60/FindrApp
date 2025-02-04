package com.findr.findr.api
import android.graphics.Point
import com.findr.findr.entity.Post
import com.findr.findr.entity.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    //Users
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
    @GET("posts/byLocation/{x}/{y}")
    suspend fun getPostsByLocation(@Path("x") x: Integer, @Path("y") y: Integer): List<Post>

    //Friends
    @GET("friendships/friends/{username}")
    suspend fun getFriendsByUsername(@Path("username") username: String): List<User>
    @POST("friendships/addFriend/{follower}/{followee}")
    suspend fun addFriend(@Path("follower") follower: Long, @Path("followee") followee: Long)


}

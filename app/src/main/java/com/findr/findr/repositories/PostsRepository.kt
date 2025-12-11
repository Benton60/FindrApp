package com.findr.findr.repository

import com.findr.findr.api.ApiService
import com.findr.findr.entity.Post


//this is a "repository" where you define how the posts are retrieved and stored.
//if you had a database in ram or an arraylist or whatever you would define
//how the RecyclerView gets more posts here
class PostsRepository(private val api: ApiService) {

    // Load posts by location & page (existing)
    suspend fun getPosts(page: Int, lat: Double, lon: Double): List<Post> {
        return api.getPostsByPage(page, lon, lat)
    }

    // Load posts for a specific user
    suspend fun getPostsByUser(page: Int, username: String): List<Post> {
        return api.getPostsByAuthor(page, username)
    }
}

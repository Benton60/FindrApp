package com.findr.findr.repository

import com.findr.findr.api.ApiService
import com.findr.findr.entity.Post

class PostsRepository(
    private val api: ApiService
) {

    // Load posts by location & page (existing)
    suspend fun getPosts(page: Int, lat: Double, lon: Double): List<Post> {
        return api.getPostsByPage(page, lon, lat)
    }

    // Load posts for a specific user
    suspend fun getPostsByUser(page: Int, username: String): List<Post> {
        return api.getPostsByAuthor(page, username)
    }
}

package com.findr.findr.repository

import com.findr.findr.api.ApiService
import com.findr.findr.entity.Post

class PostsRepository(private val api: ApiService) {
    suspend fun getPosts(page: Int, lat: Double, lon: Double): List<Post> {
        return api.getPostsByPage(page, lon, lat) // page = 0-based
    }
}


package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(post: Post): Post
    fun save(post: Post)
    fun removeById(id: Long)

    fun getAllAsync(callback: GetAllCallback)
    fun saveAsync(post: Post, callback: GetPostCallback)
    fun likeByIdAsync(post: Post, callback: GetPostCallback)
    fun removeByIdAsync(id: Long, callback: GetCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
    }

    interface GetPostCallback {
        fun onSuccess(post: Post) {}
        fun onError(e: Exception) {}
    }

    interface GetCallback {
        fun onSuccess() {}
        fun onError(e: Exception) {}
    }
}

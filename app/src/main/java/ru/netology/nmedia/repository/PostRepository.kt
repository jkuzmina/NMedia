package ru.netology.nmedia.repository

import android.app.Application
import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAll()
    suspend fun likeById(post: Post): Post
    suspend fun save(post: Post)
    suspend fun removeById(post: Post)
    suspend fun likeByIdLocal(id: Long)
    suspend fun saveLocal(post: Post)

    /*fun getAllAsync(callback: Callback<List<Post>>)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun likeByIdAsync(post: Post, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)

    interface Callback<T> {
        fun onSuccess(posts: T) {}
        fun onError(e: Exception) {}
    }*/

}

package ru.netology.nmedia.repository

import android.app.Application
import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(post: Post): Post
    fun save(post: Post)
    fun removeById(id: Long)

    fun getAllAsync(callback: Callback<List<Post>>)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun likeByIdAsync(post: Post, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)

    interface Callback<T> {
        fun onSuccess(posts: T) {}
        fun onError(e: Exception) {}
    }

}

package ru.netology.nmedia.repository

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun likeById(post: Post): Post
    suspend fun save(post: Post)
    suspend fun removeById(post: Post)
    suspend fun likeByIdLocal(id: Long)
    suspend fun saveLocal(post: Post)
    suspend fun readNewPosts()
    suspend fun newerCount(): Int
    suspend fun postsCount(): Int
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun updateUser(login: String, pass: String): AuthState
}

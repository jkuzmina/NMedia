package ru.netology.nmedia.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import ru.netology.nmedia.R
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl(private val dao: PostDao, private val application: Application): PostRepository{
    override val data: LiveData<List<Post>> = dao.getAll().map(List<PostEntity>::toDto)
    /*private val mApplication: Application
        get() {
            mApplication
        }*/

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()

    }
    /*private var postRemoved = Post(
        id = 0,
        content = "",
        author = "",
        authorAvatar = "",
        likedByMe = false,
        likes = 0,
        published = ""
    )*/
    /*override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let {
                gson.fromJson(it, typeToken.type)
            }
    }*/
    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(post: Post) {
        val postRemoved = post.copy()
        try {
            dao.removeById(post.id)
            val response = PostsApi.retrofitService.removeById(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            saveLocal(postRemoved)
            throw NetworkError
        } catch (e: Exception) {
            saveLocal(postRemoved)
            throw UnknownError
        }
    }

    override suspend fun likeByIdLocal(id: Long) {
        return dao.likeById(id)
    }

    override suspend fun saveLocal(post: Post) {
        dao.insert(PostEntity.fromDto(post))
    }

    override suspend fun likeById(post: Post) : Post {
        try {
            likeByIdLocal(post.id)
            val response = if (!post.likedByMe) {
                PostsApi.retrofitService.likeById(post.id)
            } else{
                PostsApi.retrofitService.dislikeById(post.id)
                }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            return body
        } catch (e: IOException) {
            likeByIdLocal(post.id)
            throw NetworkError
        } catch (e: Exception) {
            likeByIdLocal(post.id)
            throw UnknownError
        }
    }

    /*override fun likeById(post: Post): Post {
        val request =
            if (post.likedByMe) {
                Request.Builder()
                    .delete(gson.toJson(post.id).toRequestBody(jsonType))
                    .url("${BASE_URL}/api/posts/${post.id}/likes")
                    .build()
            } else {
                Request.Builder()
                    .post(gson.toJson(post.id).toRequestBody(jsonType))
                    .url("${BASE_URL}/api/posts/${post.id}/likes")
                    .build()
            }

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let {
                gson.fromJson(it, typePost.type)
            }
    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostsApi.retrofitService.getAll().enqueue(object : retrofit2.Callback<List<Post>> {
            override fun onResponse(
                call: retrofit2.Call<List<Post>>,
                response: retrofit2.Response<List<Post>>
            ) {
                if (!response.isSuccessful) {
                    callback.onError(java.lang.RuntimeException(errorDescription(response)))
                    return
                }

                callback.onSuccess(
                    response.body() ?: throw java.lang.RuntimeException("body is null")
                )
            }

            override fun onFailure(call: retrofit2.Call<List<Post>>, t: Throwable) {
                callback.onError(t as Exception)
            }
        })
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.save(post).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (!response.isSuccessful) {
                    callback.onError(java.lang.RuntimeException(errorDescription(response)))
                    return
                }

                callback.onSuccess(
                    response.body() ?: throw java.lang.RuntimeException("body is null")
                )
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(t as Exception)
            }
        })
    }

    override fun likeByIdAsync(post: Post, callback: PostRepository.Callback<Post>) {
        if (!post.likedByMe) {
            PostsApi.retrofitService.likeById(post.id).enqueue(object : retrofit2.Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(java.lang.RuntimeException(errorDescription(response)))
                        return
                    }

                    callback.onSuccess(
                        response.body() ?: throw java.lang.RuntimeException("body is null")
                    )
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(t as Exception)
                }
            })
        } else {
            PostsApi.retrofitService.dislikeById(post.id)
                .enqueue(object : retrofit2.Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful) {
                            callback.onError(java.lang.RuntimeException(errorDescription(response)))
                            return
                        }

                        callback.onSuccess(
                            response.body() ?: throw java.lang.RuntimeException("body is null")
                        )
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(t as Exception)
                    }
                })
        }
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        PostsApi.retrofitService.removeById(id).enqueue(object : retrofit2.Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (!response.isSuccessful) {
                    callback.onError(java.lang.RuntimeException(errorDescription(response)))
                    return
                }

                callback.onSuccess(
                    response.body() ?: throw java.lang.RuntimeException("body is null")
                )
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(t as Exception)
            }

        })
    }*/
    fun <T : Any?> errorDescription(response: Response<T>):String = application.getString(R.string.error_text, response.code().toString(), response.message())

}

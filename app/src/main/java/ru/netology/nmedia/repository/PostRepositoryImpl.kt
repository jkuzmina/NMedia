package ru.netology.nmedia.repository

import android.app.Application
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import ru.netology.nmedia.R
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit


class PostRepositoryImpl(private val application: Application): PostRepository{

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}
    private val typePost = object : TypeToken<Post>() {}
    /*private val mApplication: Application
        get() {
            mApplication
        }*/

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let {
                gson.fromJson(it, typeToken.type)
            }
    }

    override fun likeById(post: Post): Post {
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
    }
    fun <T : Any?> errorDescription(response: Response<T>):String = application.getString(R.string.error_text, response.code().toString(), response.message())

}

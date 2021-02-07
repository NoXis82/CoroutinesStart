package ru.netology


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.dto.*
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


private val BASE_URL = "http://localhost:9999"
private val gson = Gson()
private val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    })
    .connectTimeout(30, TimeUnit.SECONDS)
    .build()

fun main() {

    with(CoroutineScope(EmptyCoroutineContext)) {
        launch {
            try {
                println(Thread.currentThread().name)
                val posts = getPosts(client)
                    .map { post ->
                        async {
                            PostWithComments(post, getComments(client, post.id))
                        }
                    }.awaitAll()
                //         println(posts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        launch {
            try {
                println(Thread.currentThread().name)
                val posts = getPosts(client)
                    .map { post ->
                        async {
                            PostWithAuthors(post, getAuthor(client, post.author))
                        }
                    }.awaitAll()
                //   println(posts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        launch {
            try {
                println(Thread.currentThread().name)
                val posts = getPosts(client)
                    .map { post ->
                        async {
                            val comments = getComments(client, post.id)
                                .map { comment ->
                                    CommentsWithAuthors(comment, getAuthor(client, comment.authorId))
                                }
                            println(comments)
                        }
                    }.awaitAll()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    Thread.sleep(30_000)
}

suspend fun OkHttpClient.apiCall(url: String): Response {
    return suspendCoroutine { continuation ->
        Request.Builder()
            .url(url)
            .build()
            .let {
                client.newCall(it)
            }
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }
            })
    }
}


suspend fun <T> makeRequest(url: String, client: OkHttpClient, typeToken: TypeToken<T>): T =
    withContext(Dispatchers.IO) {
        client.apiCall(url)
            .let { response ->
                if (!response.isSuccessful) {
                    response.close()
                    throw RuntimeException(response.message)
                }
                val body = response.body ?: throw RuntimeException("response body is null")
                gson.fromJson(body.string(), typeToken.type)

            }
    }


suspend fun getPosts(client: OkHttpClient): List<Post> =
    makeRequest("$BASE_URL/api/posts", client, object : TypeToken<List<Post>>() {})

suspend fun getComments(client: OkHttpClient, id: Long): List<Comment> =
    makeRequest("$BASE_URL/api/posts/$id/comments", client, object : TypeToken<List<Comment>>() {})

suspend fun getAuthor(client: OkHttpClient, id: Long): Author =
    makeRequest("$BASE_URL/api/authors/$id", client, object : TypeToken<Author>() {})
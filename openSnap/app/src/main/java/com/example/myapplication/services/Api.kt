package com.example.myapplication.services

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.CompletableFuture

//https://pocketbase.io/docs/api-collections/#view-collection

// https://stackoverflow.com/questions/5528850/how-do-you-connect-localhost-in-the-android-emulator
// const val PocketbaseApi = "http://10.0.2.2:8090" // Local pocketbase (executable)
const val PocketbaseApi = "https://pocketbase.refoxed.com" // Using my test server

const val tagError = "Pocketbase: ERROR"
const val tagSuccess = "Pocketbase: SUCCESS"

// https://stackoverflow.com/questions/50139888/how-to-get-async-call-to-return-response-to-main-thread-using-okhttp
internal class CallbackFuture : CompletableFuture<Response?>(), Callback {
    override fun onFailure(call: Call, e: IOException) {
        super.completeExceptionally(e)
    }

    override fun onResponse(call: Call, response: Response) {
        super.complete(response)
    }
}

private fun createPostRequest(url: String, json: JSONObject): Request {
    val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    return Request.Builder().url(url).post(requestBody).build()
}

private fun createPutRequest(url: String, json: JSONObject): Request {
    val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    return Request.Builder().url(url).put(requestBody).build()
}

// https://stackoverflow.com/questions/46177133/http-request-in-android-with-kotlin
// https://mkyong.com/java/how-do-convert-java-object-to-from-json-format-gson-api/
// Programming 3, SpringData too heavy handed

suspend fun createUser(user: NewUser): Boolean {
    val client = OkHttpClient()
    val url = "$PocketbaseApi/api/collections/users/records"
    val request = createPostRequest(url, JSONObject(Gson().toJson(user)))

    try {
        val future: CallbackFuture = CallbackFuture()
        client.newCall(request).enqueue(future)
        // https://stackoverflow.com/questions/37231560/best-way-to-null-check-in-kotlin
        val response = future.get() ?: throw IllegalStateException("Most likely timeout")
        if (response.isSuccessful) {
            Log.d(tagSuccess, "${response.code}:${response.message} - ${response.body?.string()}" )
            return true
        } else {
            throw IOException("${response.code}:${response.message} - ${response.body?.string()}")
        }
    } catch (e: Exception) {
        Log.d(tagError, e.toString())
        return false
    }
}
suspend fun authWithPassword(email: String, password: String): AuthResponse? {
    val client = OkHttpClient()
    val url = "$PocketbaseApi/api/collections/users/auth-with-password"

    val authObject = JSONObject().apply {
        put("identity", email)
        put("password", password)
    }

    val request = createPostRequest(url, authObject)

    try {
        val future: CallbackFuture = CallbackFuture()
        client.newCall(request).enqueue(future)
        val response = future.get() ?: throw IllegalStateException("Most likely timeout")
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            Log.d(tagSuccess, "${response.code}:${response.message} - $responseBody")
            return Gson().fromJson(responseBody, AuthResponse::class.java)
        } else {
            throw IOException("${response.code}:${response.message} - ${response.body?.string()}")
        }
    } catch (e: Exception) {
        Log.d(tagError, Log.getStackTraceString(e))
        return null
    }
}

suspend fun getMessages(authResponse: AuthResponse): Messages? {
    val client = OkHttpClient()
    val url = "$PocketbaseApi/api/collections/messages/records?expand=user&perPage=25&sort=-created"

    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer ${authResponse.token}")
        .build()

    try {
        val future: CallbackFuture = CallbackFuture()
        client.newCall(request).enqueue(future)
        val response = future.get() ?: throw IllegalStateException("Most likely timeout")
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            Log.d("SPECIAL POCKETS", "${response.code}:${response.message} - $responseBody")
            return Gson().fromJson(responseBody, Messages::class.java)
        } else {
            throw IOException("${response.code}:${response.message} - ${response.body?.string()}")
        }
    } catch (e: Exception) {
        Log.d(tagError, Log.getStackTraceString(e))
        return null
    }
}

fun getImageURL(authResponse: AuthResponse?, message: Message): String {
    if (authResponse != null) {
        return "$PocketbaseApi/api/files/${message.collectionId}/${message.id}/${message.photo}?token=${authResponse.token}&thumb=200x200"
    } else {
        return "$PocketbaseApi/api/files/${message.collectionId}/${message.id}/${message.photo}?thumb=100x100&token="
    }
}

// https://github.com/pocketbase/pocketbase/discussions/3056
fun uploadMessage(authResponse: AuthResponse, text: String, file: File): Boolean {
    val client = OkHttpClient()
    val url = "$PocketbaseApi/api/collections/messages/records"
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("text", text)
        .addFormDataPart("user", authResponse.record.id)
        .addFormDataPart("photo", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        .build()
    Log.d("POCKETBSAE DATA", text + authResponse.record.id.toString() + file.name.toString())
    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer ${authResponse.token}")
        .post(requestBody)
        .build()

    try {
        val future: CallbackFuture = CallbackFuture()
        client.newCall(request).enqueue(future)
        val response = future.get() ?: throw IllegalStateException("Most likely timeout")
        if (response.isSuccessful) {
            Log.d(tagSuccess, "${response.code}:${response.message} - ${response.body?.string()}" )
            return true
        } else {
            throw IOException("${response.code}:${response.message} - ${response.body?.string()}")
        }
    } catch (e: Exception) {
        Log.d(tagError, e.toString())
        return false
    }

}
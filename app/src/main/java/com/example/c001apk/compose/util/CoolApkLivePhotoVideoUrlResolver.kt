package com.example.c001apk.compose.util

import com.example.c001apk.media.LivePhotoVideoUrlResolver
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

object CoolApkLivePhotoVideoUrlResolver : LivePhotoVideoUrlResolver {

    private val client = OkHttpClient.Builder()
        .addInterceptor(AddCookiesInterceptor)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    override fun resolve(imageUrl: String): String? {
        val url = "https://api.coolapk.com/v6/livePhoto/showVideo"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("picUrl", imageUrl)
            .build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return client.newCall(request).execute().use { response ->
            if (response.code !in 300..399) return@use null
            response.header("Location")
                ?.takeIf { it.startsWith("https://") || it.startsWith("http://") }
        }
    }
}

/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件为网络层新增统一策略：
 * 1. GET 请求自动重试（指数退避，网络异常 / 502/503/504）；
 * 2. 离线时回退到本地 HTTP 缓存（最多容忍 30 天过期数据）；
 * 3. API 响应无缓存头时补充短时缓存策略（15 秒）。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

internal fun isNetworkAvailable(context: Context): Boolean {
    val manager = context.getSystemService(ConnectivityManager::class.java) ?: return true
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * GET 请求重试：网络异常或网关错误（502/503/504）时最多重试 2 次，
 * 退避间隔 600ms / 1200ms。POST 等非幂等请求一律不重试，
 * 请求已取消或设备离线时立即失败。
 */
class RetryInterceptor(
    private val context: Context,
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method != "GET") {
            return chain.proceed(request)
        }
        var attempt = 0
        while (true) {
            val response = try {
                chain.proceed(request)
            } catch (e: IOException) {
                if (attempt >= MAX_ATTEMPTS || chain.call().isCanceled() ||
                    !isNetworkAvailable(context)
                ) {
                    throw e
                }
                attempt++
                SystemClock.sleep(RETRY_BACKOFF_MS shl attempt)
                continue
            }
            if (attempt >= MAX_ATTEMPTS || response.code !in RETRYABLE_CODES) {
                return response
            }
            response.close()
            attempt++
            SystemClock.sleep(RETRY_BACKOFF_MS shl attempt)
        }
    }

    companion object {
        private const val MAX_ATTEMPTS = 2
        private const val RETRY_BACKOFF_MS = 300L
        private val RETRYABLE_CODES = setOf(502, 503, 504)
    }
}

/**
 * 离线回退：无网络时强制使用本地缓存（容忍 30 天内的过期数据），
 * 缓存未命中时由 OkHttp 返回 504，走原有错误流程。
 */
class OfflineCacheInterceptor(
    private val context: Context,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!isNetworkAvailable(context)) {
            request = request.newBuilder()
                .cacheControl(OFFLINE_CACHE_CONTROL)
                .build()
        }
        return chain.proceed(request)
    }

    companion object {
        private val OFFLINE_CACHE_CONTROL = CacheControl.Builder()
            .onlyIfCached()
            .maxStale(OFFLINE_MAX_STALE_DAYS, TimeUnit.DAYS)
            .build()
        private const val OFFLINE_MAX_STALE_DAYS = 30
    }
}

/**
 * 响应缓存策略（网络拦截器）：酷安 API 响应通常不带缓存头，
 * 对 GET 200 且无 Cache-Control 的响应补充 15 秒短缓存——
 * 快速返回时可秒开，刷新最多延迟 15 秒；同时清除 Vary
 * 避免按签名头分桶导致缓存永不命中。
 */
object ApiResponseCacheInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (request.method != "GET" || response.code != 200 ||
            response.header("Cache-Control") != null ||
            request.url.host !in CACHEABLE_HOSTS
        ) {
            return response
        }
        return response.newBuilder()
            .header("Cache-Control", "private, max-age=15")
            .removeHeader("Vary")
            .build()
    }

    private val CACHEABLE_HOSTS = setOf("api.coolapk.com", "api2.coolapk.com")
}

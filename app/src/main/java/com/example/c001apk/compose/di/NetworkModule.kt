/*
 * 修改声明（UI 优化版，基于 frisk1127/c001apk-compose，AGPL-3.0）：
 * 本文件为 API 客户端接入统一网络策略：50MB HTTP 缓存、GET 请求
 * 指数退避重试、离线缓存回退与响应短缓存（详见 util/NetworkInterceptors.kt）。
 * 原作者版权与许可见 LICENSE。
 */
package com.example.c001apk.compose.di

import android.content.Context
import com.example.c001apk.compose.BuildConfig
import com.example.c001apk.compose.logic.network.ApiService
import com.example.c001apk.compose.util.AddCookiesInterceptor
import com.example.c001apk.compose.util.ApiResponseCacheInterceptor
import com.example.c001apk.compose.util.LoginCookiesInterceptor
import com.example.c001apk.compose.util.OfflineCacheInterceptor
import com.example.c001apk.compose.util.RetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Api1Service

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Api1ServiceNoRedirect

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Api2Service

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AccountService

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val API_BASE_URL = "https://api.coolapk.com"
    private const val API2_BASE_URL = "https://api2.coolapk.com"
    private const val ACCOUNT_BASE_URL = "https://account.coolapk.com"

    @Api1Service
    @Singleton
    @Provides
    fun provideApi1Service(@Api1Service retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Api1ServiceNoRedirect
    @Singleton
    @Provides
    fun provideApi1ServiceNo(@Api1ServiceNoRedirect retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Api2Service
    @Singleton
    @Provides
    fun provideApi2Service(@Api2Service retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @AccountService
    @Singleton
    @Provides
    fun provideAccountService(@AccountService retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Api1Service
    @Singleton
    @Provides
    fun provideApi1ServiceRetrofit(@Api1Service okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Api1ServiceNoRedirect
    @Singleton
    @Provides
    fun provideApi1ServiceNoRetrofit(@Api1ServiceNoRedirect okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Api2Service
    @Singleton
    @Provides
    fun provideApi2ServiceRetrofit(@Api1Service okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API2_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @AccountService
    @Singleton
    @Provides
    fun provideAccountServiceRetrofit(@AccountService okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ACCOUNT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        return Cache(
            directory = File(context.cacheDir, HTTP_CACHE_DIR),
            maxSize = HTTP_CACHE_SIZE_BYTES,
        )
    }

    @Api1Service
    @Singleton
    @Provides
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        cache: Cache,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(RetryInterceptor(context))
            .addInterceptor(AddCookiesInterceptor)
            .addInterceptor(OfflineCacheInterceptor(context))
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                )
            )
            .addNetworkInterceptor(ApiResponseCacheInterceptor)
            .followRedirects(true)
            .build()
    }

    @Api1ServiceNoRedirect
    @Singleton
    @Provides
    fun provideNoOkHttpClient(
        @ApplicationContext context: Context,
        cache: Cache,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(RetryInterceptor(context))
            .addInterceptor(AddCookiesInterceptor)
            .addInterceptor(OfflineCacheInterceptor(context))
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                )
            )
            .addNetworkInterceptor(ApiResponseCacheInterceptor)
            .followRedirects(false)
            .build()
    }

    @AccountService
    @Singleton
    @Provides
    fun provideAccountServiceOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(context))
            .addInterceptor(LoginCookiesInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                )
            )
            .build()
    }

    private const val HTTP_CACHE_DIR = "http-cache"
    private const val HTTP_CACHE_SIZE_BYTES = 50L * 1024 * 1024

}
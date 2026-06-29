package com.bariatricnepal.app.data.api

import com.bariatricnepal.app.data.local.SessionStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds a Retrofit client pointed at the clinic's WordPress site
 * (entered once by the user on the Server Setup screen) and attaches the
 * X-BN-Token session header automatically on every request once logged in.
 */
object ApiClient {

    /** Normalizes whatever the user typed ("mysite.com", "www.mysite.com/", "https://mysite.com")
     *  into a clean REST API base URL ending in a slash. */
    fun normalizeBaseUrl(input: String): String {
        var url = input.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        url = url.trimEnd('/')
        return "$url/wp-json/bariatric-nepal/v1/"
    }

    fun create(sessionStore: SessionStore): BNApiService {
        val baseUrl = sessionStore.baseUrl ?: throw IllegalStateException("Server URL not configured")

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
            sessionStore.token?.let { builder.addHeader("X-BN-Token", it) }
            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BNApiService::class.java)
    }
}

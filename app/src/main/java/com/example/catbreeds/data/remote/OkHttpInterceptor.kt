package com.example.catbreeds.data.remote

import com.example.catbreeds.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class OkHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("x-api-key", BuildConfig.API_KEY)
            .method(originalRequest.method, originalRequest.body)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
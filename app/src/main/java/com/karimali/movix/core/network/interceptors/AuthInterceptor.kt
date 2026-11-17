package com.karimali.movix.core.network.interceptors

import com.karimali.movix.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization","Bearer ${BuildConfig.TMDB_ACCESS_TOKEN}")
            .addHeader("accept","application/json")
            .build()
        return chain.proceed(request)
    }
}
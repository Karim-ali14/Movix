package com.karimali.movix.core.network.interceptors

import com.karimali.movix.core.network.AppRouter
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ContentFilterInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        val newUrl = when {
            path.endsWith(AppRouter.DISCOVER_MOVIES) -> {
                // Discover endpoint supports all mandatory filtering conditions
                request.url.newBuilder()
                    .addQueryParameter("include_adult", "false")
                    .addQueryParameter("certification_country", "US")
                    .addQueryParameter("certification.lte", "PG-13")
                    .build()
            }

            path.endsWith(AppRouter.SEARCH_MOVIES) -> {
                // Search endpoint only supports include_adult and does not support certification parameters
                request.url.newBuilder()
                    .addQueryParameter("include_adult", "false")
                    .build()
            }

            else -> {
                request.url
            }
        }

        val newRequest = request.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
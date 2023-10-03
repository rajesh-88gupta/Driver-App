package com.seentechs.newtaxidriver.common.dependencies.interceptors

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage dependencies.interceptors
 * @category AuthTokenInterceptor
 * @author Seen Technologies
 *
 */

import com.seentechs.newtaxidriver.common.configs.SessionManager

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/*****************************************************************
 * Auth Token Interceptor
 */
class AuthTokenInterceptor(private val sessionManager: SessionManager) : Interceptor {
    private var requestBuilder: Request.Builder? = null

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val original = chain.request()
            if (sessionManager.token != null) {
                // Request customization: add request headers
                requestBuilder = original.newBuilder().header("Authorization", sessionManager.token!!).method(original.method, original.body)
            } else {
                // Request customization: add request headers
                requestBuilder = original.newBuilder().method(original.method, original.body)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val request = requestBuilder!!.build()
        return chain.proceed(request)
    }
}

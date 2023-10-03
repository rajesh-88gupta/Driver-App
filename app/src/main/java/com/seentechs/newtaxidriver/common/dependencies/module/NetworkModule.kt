package com.seentechs.newtaxidriver.common.dependencies.module

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage dependencies.module
 * @category NetworkModule
 * @author Seen Technologies
 *
 */

import android.content.Context

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.dependencies.interceptors.AuthTokenInterceptor
import com.seentechs.newtaxidriver.common.dependencies.interceptors.NetworkInterceptor
import com.seentechs.newtaxidriver.home.interfaces.ApiService

import java.util.concurrent.TimeUnit

import javax.inject.Inject
import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*****************************************************************
 * NetWork Module
 */
@Module
class NetworkModule @Inject
constructor(private val mBaseUrl: String) {

    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        return loggingInterceptor
    }

    @Provides
    @Singleton
    fun providesGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(context: Context, httpLoggingInterceptor: HttpLoggingInterceptor, sessionManager: SessionManager): OkHttpClient.Builder {
        val client = OkHttpClient.Builder().connectTimeout(5, TimeUnit.MINUTES).readTimeout(5, TimeUnit.MINUTES)
        client.addInterceptor(httpLoggingInterceptor)
        client.addInterceptor(NetworkInterceptor(context))
        client.addInterceptor(AuthTokenInterceptor(sessionManager))
        //client.authenticator(new TokenRenewInterceptor(sessionManager));

        return client
    }

    @Provides
    @Singleton
    fun providesRetrofitService(okHttpClient: OkHttpClient.Builder, gson: Gson): Retrofit {
        return Retrofit.Builder().baseUrl(mBaseUrl).addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient.build()).build()
    }

    @Provides
    @Singleton
    fun providesApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

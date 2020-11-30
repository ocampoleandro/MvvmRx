package com.example.mvvmrx.network

import com.example.mvvmrx.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder {

    val webservice: WebService by lazy {
        Retrofit.Builder()
            .client(defaultHttpClient)
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build().create(WebService::class.java)
    }

    private val defaultHttpClient: OkHttpClient by lazy {
        defaultHttpBuilder.build()
    }

    private val defaultHttpBuilder: OkHttpClient.Builder
            by lazy {
                val builder = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)

                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.level = HttpLoggingInterceptor.Level.BODY
                    builder.addInterceptor(logging)
                }
                return@lazy builder
            }

}
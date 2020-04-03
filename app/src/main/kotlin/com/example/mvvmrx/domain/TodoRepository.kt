package com.example.mvvmrx.domain

import com.example.mvvmrx.network.WebService
import com.example.mvvmrx.network.RetrofitBuilder
import java.util.concurrent.TimeUnit

class TodoRepository(private val webService: WebService) {

    fun getTodos() = webService.getTodos()
        //to simulate delays in the network
        .delay(2, TimeUnit.SECONDS)

    companion object {
        val instance by lazy {
            TodoRepository(
                RetrofitBuilder.webservice
            )
        }
    }
}
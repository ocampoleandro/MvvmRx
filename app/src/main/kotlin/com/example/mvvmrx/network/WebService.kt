package com.example.mvvmrx.network

import com.example.mvvmrx.network.model.TodoDTO
import io.reactivex.Single
import retrofit2.http.GET

interface WebService {

    @GET("/todos")
    fun getTodos(): Single<List<TodoDTO>>

}
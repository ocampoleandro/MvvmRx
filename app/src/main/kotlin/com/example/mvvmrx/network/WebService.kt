package com.example.mvvmrx.network

import com.example.mvvmrx.network.model.TodoDTO
import retrofit2.http.GET

interface WebService {

    @GET("/todos")
    suspend fun todos(): List<TodoDTO>

}
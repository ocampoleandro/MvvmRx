package com.example.mvvmrx.network.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TodoDTO (
    val id: Int = 0,
    val title: String = "",
    val completed: Boolean = false
)
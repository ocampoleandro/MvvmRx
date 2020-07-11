package com.example.mvvmrx.domain.model

data class Todo(
    val id: Int,
    val title: String,
    val state: State
) {

    enum class State {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

}
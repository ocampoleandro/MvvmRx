package com.example.mvvmrx.local.model

data class TodoDB(
    val id: Int = 0,
    val title: String = "",
    val state: Int
) {

    //you should have all this from converters when using room
    companion object {
        const val NOT_STARTED = 1
        const val IN_PROGRESS = 2
        const val COMPLETED = 3
    }
}
package com.example.mvvmrx.ui

import kotlinx.coroutines.flow.Flow

/**
 * View that expose the user interactions with the UI.
 */
interface MainView {

    fun onTodoSelected(): Flow<Int>

    fun onTodoInProgessUpdated(): Flow<Int>

    fun onRetry(): Flow<Unit>

}
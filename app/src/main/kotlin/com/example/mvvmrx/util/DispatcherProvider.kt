package com.example.mvvmrx.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Use this class when you need to switch threads in coroutines.
 * This provider makes components able to be unit tested when using threads with
 * coroutines.
 */
class DispatcherProvider(
    val main: CoroutineDispatcher = Dispatchers.Main,
    val default: CoroutineDispatcher = Dispatchers.Default,
    val io: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        val instance = DispatcherProvider()
    }
}
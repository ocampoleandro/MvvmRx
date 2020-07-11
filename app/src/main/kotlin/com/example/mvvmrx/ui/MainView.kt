package com.example.mvvmrx.ui

import io.reactivex.Observable

/**
 * View that expose the user interactions with the UI.
 */
interface MainView {

    fun onTodoSelected(): Observable<Int>

    fun onTodoInProgessUpdated(): Observable<Int>

    fun onRetry(): Observable<Unit>

}
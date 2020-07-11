package com.example.mvvmrx.ui

import com.example.mvvmrx.R
import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.ui.model.TodoUI

fun Todo.toUI() = TodoUI(
    id = id,
    title = title,
    //homework: pass theme attrs instead to make it flexible for different themes ;)
    colorState = when (state) {
        Todo.State.NOT_STARTED -> R.color.notStarted
        Todo.State.IN_PROGRESS -> R.color.inProgress
        Todo.State.COMPLETED -> R.color.completed
    }
)
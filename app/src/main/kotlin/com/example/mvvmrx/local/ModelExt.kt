package com.example.mvvmrx.local

import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.local.model.TodoDB

fun Todo.toDB() = TodoDB(
    id = id,
    title = title,
    state = when (state) {
        Todo.State.NOT_STARTED -> TodoDB.NOT_STARTED
        Todo.State.IN_PROGRESS -> TodoDB.IN_PROGRESS
        Todo.State.COMPLETED -> TodoDB.COMPLETED
    }
)

fun TodoDB.toDomain() = Todo(
    id = id,
    title = title,
    state = when (state) {
        TodoDB.NOT_STARTED -> Todo.State.NOT_STARTED
        TodoDB.IN_PROGRESS -> Todo.State.IN_PROGRESS
        TodoDB.COMPLETED -> Todo.State.COMPLETED
        else -> throw IllegalStateException("invalid state: $state")
    }
)
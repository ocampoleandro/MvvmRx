package com.example.mvvmrx.network

import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.network.model.TodoDTO

fun TodoDTO.toDomain(state: Todo.State) = Todo(
    id = id,
    title = title,
    state = state
)
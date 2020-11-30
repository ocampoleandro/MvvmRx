package com.example.mvvmrx.domain

import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.local.TodoDAO
import com.example.mvvmrx.local.toDB
import com.example.mvvmrx.network.RetrofitBuilder
import com.example.mvvmrx.network.WebService
import com.example.mvvmrx.network.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class TodoRepository(
    private val webService: WebService,
    private val todoDAO: TodoDAO
) {

    suspend fun todos(): Flow<List<Todo>> = flow {
        delay(500)
        val todoDtos = webService.todos()
        todoDAO.getTodosInProgress().map { entries ->
            val inProgressIds = entries.map { it.id }
            todoDtos.map { dto ->
                val state = if (inProgressIds.contains(dto.id)) {
                    Todo.State.IN_PROGRESS
                } else {
                    if (dto.completed) {
                        Todo.State.COMPLETED
                    } else {
                        Todo.State.NOT_STARTED
                    }
                }
                dto.toDomain(state)
            }
        }.flowOn(Dispatchers.Default).collect { todos -> emit(todos) }
    }

    suspend fun update(todo: Todo) {
        todoDAO.update(todo.toDB())
    }

    companion object {
        val instance by lazy {
            TodoRepository(
                RetrofitBuilder.webservice,
                TodoDAO.instance
            )
        }
    }
}
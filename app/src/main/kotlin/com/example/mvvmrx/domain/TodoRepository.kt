package com.example.mvvmrx.domain

import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.local.TodoDAO
import com.example.mvvmrx.local.toDB
import com.example.mvvmrx.network.RetrofitBuilder
import com.example.mvvmrx.network.WebService
import com.example.mvvmrx.network.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TodoRepository(
    private val webService: WebService,
    private val todoDAO: TodoDAO
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun todos(): Flow<List<Todo>> {
        delay(500)
        val todos = webService.todos()
        return withContext(Dispatchers.Default) {
            todoDAO.getTodosInProgress().map { entries ->
                val inProgressIds = entries.map { it.id }
                todos.map { dto ->
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
            }
        }
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
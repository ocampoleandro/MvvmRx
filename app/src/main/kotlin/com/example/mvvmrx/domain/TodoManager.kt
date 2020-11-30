package com.example.mvvmrx.domain

import com.example.mvvmrx.domain.model.Todo
import kotlinx.coroutines.flow.Flow

class TodoManager(private val todoRepository: TodoRepository) {


    suspend fun getTodos(): Flow<List<Todo>> {
        return todoRepository.todos()
    }

    /**
     * Sets the [Todo] in a [Todo.State.IN_PROGRESS] state
     *
     * @exception [IllegalArgumentException] when the [Todo] is already in progress or completed
     */
    suspend fun updateInProgress(todo: Todo) {
        updateTodo(todo)
    }

    private suspend fun updateTodo(todo: Todo) {
        when (todo.state) {
            Todo.State.NOT_STARTED -> todoRepository.update(todo.copy(state = Todo.State.IN_PROGRESS))
            Todo.State.IN_PROGRESS -> todoRepository.update(todo.copy(state = Todo.State.NOT_STARTED))
            Todo.State.COMPLETED -> throw IllegalArgumentException("todo has already been completed")
        }
    }

    companion object {
        val instance by lazy { TodoManager(TodoRepository.instance) }
    }

}
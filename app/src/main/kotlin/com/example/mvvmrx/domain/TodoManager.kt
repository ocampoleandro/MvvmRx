package com.example.mvvmrx.domain

import com.example.mvvmrx.domain.model.Todo
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxObservable

class TodoManager(private val todoRepository: TodoRepository) {


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTodos(): Observable<List<Todo>> {
        return rxObservable { todoRepository.todos().collect { send(it) } }
    }

    /**
     * Sets the [Todo] in a [Todo.State.IN_PROGRESS] state
     *
     * @exception [IllegalArgumentException] when the [Todo] is already in progress or completed
     */
    fun updateInProgress(todo: Todo): Completable {
        return rxCompletable { updateTodo(todo) }
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
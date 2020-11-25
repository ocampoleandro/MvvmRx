package com.example.mvvmrx.domain

import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.local.TodoDAO
import com.example.mvvmrx.local.toDB
import com.example.mvvmrx.network.RetrofitBuilder
import com.example.mvvmrx.network.WebService
import com.example.mvvmrx.network.toDomain
import io.reactivex.Observable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.rxObservable

class TodoRepository(
    private val webService: WebService,
    private val todoDAO: TodoDAO
) {

    //ideally this will be a flowable that will emit changes from the DB
/*    fun getTodos(): Observable<List<Todo>> {
        return webService.getTodos()
            //to simulate delays in the network
            .delay(500, TimeUnit.MILLISECONDS)
            .flatMapObservable { dtos ->
                todoDAO.getTodosInProgress().map { entries ->
                    val inProgressIds = entries.map { it.id }
                    dtos.map { dto ->
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
    }*/

    @OptIn(ExperimentalCoroutinesApi::class)
    fun todos(): Observable<List<Todo>> {
        return rxObservable {
            delay(500)
            val todos = webService.todos()
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
            }.collect { send(it) }
        }
    }

    fun update(todo: Todo) {
        GlobalScope.launch(Dispatchers.Default) { todoDAO.update(todo.toDB()) }
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
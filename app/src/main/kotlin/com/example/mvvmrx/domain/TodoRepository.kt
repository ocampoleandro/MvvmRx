package com.example.mvvmrx.domain

import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.local.TodoDAO
import com.example.mvvmrx.local.toDB
import com.example.mvvmrx.network.RetrofitBuilder
import com.example.mvvmrx.network.WebService
import com.example.mvvmrx.network.toDomain
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class TodoRepository(
    private val webService: WebService,
    private val todoDAO: TodoDAO
) {

    //ideally this will be a flowable that will emit changes from the DB
    fun getTodos(): Observable<List<Todo>> {
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
    }

    fun update(todo: Todo) {
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
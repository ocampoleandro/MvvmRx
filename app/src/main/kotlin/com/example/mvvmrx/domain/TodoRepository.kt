package com.example.mvvmrx.domain

import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.local.TodoDAO
import com.example.mvvmrx.local.model.TodoDB
import com.example.mvvmrx.network.RetrofitBuilder
import com.example.mvvmrx.network.WebService
import com.example.mvvmrx.network.model.TodoDTO
import com.example.mvvmrx.network.toDomain
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class TodoRepository(
    private val webService: WebService,
    private val todoDAO: TodoDAO
) {

    //ideally this will be a flowable that will emit changes from the DB
    fun getTodos(): Observable<List<Todo>> {
        return webService.getTodos()
            //to simulate delays in the network
            .delay(2, TimeUnit.SECONDS)
            .toObservable()
            .withLatestFrom(
                todoDAO.getTodosInProgress(),
                BiFunction<List<TodoDTO>, List<TodoDB>, List<Todo>> { todoDtos, todoDbs ->
                    val inProgressIds = todoDbs.map { it.id }
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
                })
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
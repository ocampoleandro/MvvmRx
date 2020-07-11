package com.example.mvvmrx.local

import com.example.mvvmrx.local.model.TodoDB
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single

//This is pretending to be a dao for DB access.
class TodoDAO {

    private val todosInProgress: HashSet<TodoDB> = HashSet()
    private val todosInProgressRelay = BehaviorRelay.create<List<TodoDB>>()

    fun update(todo: TodoDB) {
        if (todo.state == TodoDB.IN_PROGRESS) {
            todosInProgress.add(todo)
        } else {
            todosInProgress.remove(todo)
        }
        todosInProgressRelay.accept(todosInProgress.toList())
    }

    fun getTodosInProgress(): Observable<List<TodoDB>> {
        return todosInProgressRelay.startWith(todosInProgress.toList())
    }

    companion object {
        val instance by lazy { TodoDAO() }
    }

}
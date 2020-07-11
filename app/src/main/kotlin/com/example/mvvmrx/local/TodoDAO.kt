package com.example.mvvmrx.local

import com.example.mvvmrx.local.model.TodoDB
import io.reactivex.Observable
import io.reactivex.Single

//This is pretending to be a dao for DB access.
class TodoDAO {

    private val todosInProgress: HashSet<TodoDB> = HashSet()

    fun update(todo: TodoDB) {
        if (todo.state == TodoDB.IN_PROGRESS) {
            todosInProgress.add(todo)
        } else {
            todosInProgress.remove(todo)
        }
    }

    fun getTodosInProgress(): Observable<List<TodoDB>> {
        return Observable.just(todosInProgress.toList())
    }

    companion object {
        val instance by lazy { TodoDAO() }
    }

}
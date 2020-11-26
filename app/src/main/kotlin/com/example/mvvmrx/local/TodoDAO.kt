package com.example.mvvmrx.local

import com.example.mvvmrx.local.model.TodoDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext

//This is pretending to be a dao for DB access.
class TodoDAO {

    private val todosInProgress: HashSet<TodoDB> = HashSet()
    //private val todosInProgressRelay = BehaviorRelay.create<List<TodoDB>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todosInProgressChannel = ConflatedBroadcastChannel<List<TodoDB>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun update(todo: TodoDB) {
        withContext(Dispatchers.Default) {
            if (todo.state == TodoDB.IN_PROGRESS) {
                todosInProgress.add(todo)
            } else {
                todosInProgress.remove(todo)
            }
            todosInProgressChannel.send(todosInProgress.toList())
        }
        //todosInProgressRelay.accept(todosInProgress.toList())
    }

    @OptIn(FlowPreview::class)
    fun getTodosInProgress(): Flow<List<TodoDB>> {
        return todosInProgressChannel.asFlow()
        //return todosInProgressRelay.startWith(todosInProgress.toList())
    }

    companion object {
        val instance by lazy { TodoDAO() }
    }

}
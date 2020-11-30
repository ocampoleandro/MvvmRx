package com.example.mvvmrx.local

import com.example.mvvmrx.local.model.TodoDB
import com.example.mvvmrx.util.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext

//This is pretending to be a dao for DB access.
class TodoDAO(
    private val dispatcherProvider: DispatcherProvider = DispatcherProvider.instance
) {

    private val todosInProgress: HashSet<TodoDB> = HashSet()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todosInProgressChannel = ConflatedBroadcastChannel<List<TodoDB>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun update(todo: TodoDB) {
        withContext(dispatcherProvider.default) {
            if (todo.state == TodoDB.IN_PROGRESS) {
                todosInProgress.add(todo)
            } else {
                todosInProgress.remove(todo)
            }
            todosInProgressChannel.send(todosInProgress.toList())
        }
    }

    @OptIn(FlowPreview::class)
    fun getTodosInProgress(): Flow<List<TodoDB>> {
        return todosInProgressChannel.asFlow()
    }

    companion object {
        val instance by lazy { TodoDAO() }
    }

}
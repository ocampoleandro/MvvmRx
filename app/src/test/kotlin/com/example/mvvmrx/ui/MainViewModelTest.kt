package com.example.mvvmrx.ui

import com.example.mvvmrx.domain.TodoManager
import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.util.DispatcherProvider
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineExtension::class, InstantExecutorExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainViewModelTest(
    val testCoroutineScope: TestCoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) {

    @Nested
    inner class LoadTodos {
        @Test
        fun `GIVEN vm initialization, WHEN retry is received ,THEN todos should be loaded again`() = testCoroutineScope.runBlockingTest {
            val todo1 = Todo(id = 1, title = "", state = Todo.State.NOT_STARTED)
            val todo2 = Todo(id = 2, title = "", state = Todo.State.NOT_STARTED)
            val todo3 = Todo(id = 3, title = "", state = Todo.State.NOT_STARTED)

            val todoManager: TodoManager = mock {
                onBlocking { getTodos() } doReturnConsecutively listOf(
                    flowOf(listOf(todo1, todo2)),
                    flowOf(listOf(todo1, todo2, todo3))
                )
            }

            val retryFlow = MutableSharedFlow<Unit>()
            val mainView = object : MainView {
                override fun onTodoSelected(): Flow<Int> = MutableSharedFlow()

                override fun onTodoInProgessUpdated(): Flow<Int> = MutableSharedFlow()

                override fun onRetry(): Flow<Unit> = retryFlow
            }

            val subject = MainViewModel(todoManager, dispatcherProvider)
            val testLiveDataObserver = subject.stateLiveData.test()
            subject.execute
            subject.bind(mainView, testCoroutineScope)

            testLiveDataObserver
                .assertValueHistory(
                    MainViewModel.UIModel.State.Loading,
                    MainViewModel.UIModel.State.Success(listOf(todo1.toUI(), todo2.toUI()))
                )

            retryFlow.emit(Unit)

            testLiveDataObserver
                .assertValueHistory(
                    MainViewModel.UIModel.State.Loading,
                    MainViewModel.UIModel.State.Success(listOf(todo1.toUI(), todo2.toUI())),
                    MainViewModel.UIModel.State.Loading,
                    MainViewModel.UIModel.State.Success(
                        listOf(
                            todo1.toUI(),
                            todo2.toUI(),
                            todo3.toUI()
                        )
                    )
                )
        }
    }

}
package com.example.mvvmrx.ui

import com.example.mvvmrx.domain.Todo
import com.example.mvvmrx.domain.TodoRepository
import com.github.technoir42.rxjava2.junit5.OverrideSchedulersExtension
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(OverrideSchedulersExtension::class, InstantExecutorExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainViewModelTest {

    @Nested
    inner class LoadTodos {
        @Test
        fun `GIVEN vm initialization, WHEN retry is received ,THEN todos should be loaded again`() {
            val todo1: Todo = mock {
                on { id } doReturn 1
            }
            val todo2: Todo = mock {
                on { id } doReturn 2
            }
            val todo3: Todo = mock {
                on { id } doReturn 3
            }
            val todoRepository: TodoRepository = mock {
                on { getTodos() } doReturnConsecutively listOf(
                    Single.just(listOf(todo1, todo2)),
                    Single.just(listOf(todo1, todo2, todo3))
                )
            }

            val retryStream = PublishRelay.create<Unit>()
            val mainView = object : MainView {
                override fun onTodoSelected(): Observable<Int> = PublishRelay.create()

                override fun onRetry(): Observable<Unit> = retryStream
            }

            val subject = MainViewModel(todoRepository)
            val testLiveDataObserver = subject.liveData.test()
            subject.execute
            subject.bind(mainView)

            testLiveDataObserver
                .assertValueHistory(
                    MainViewModel.UIModel.State.Loading,
                    MainViewModel.UIModel.State.Success(listOf(todo1, todo2))
                )

            retryStream.accept(Unit)

            testLiveDataObserver
                .assertValueHistory(
                    MainViewModel.UIModel.State.Loading,
                    MainViewModel.UIModel.State.Success(listOf(todo1, todo2)),
                    MainViewModel.UIModel.State.Loading,
                    MainViewModel.UIModel.State.Success(listOf(todo1, todo2, todo3))
                )
        }
    }

}
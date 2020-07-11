package com.example.mvvmrx.ui

import com.example.mvvmrx.domain.TodoManager
import com.example.mvvmrx.domain.model.Todo
import com.github.technoir42.rxjava2.junit5.OverrideSchedulersExtension
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
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
            val todo1 = Todo(id = 1, title = "", state = Todo.State.NOT_STARTED)
            val todo2 = Todo(id = 2, title = "", state = Todo.State.NOT_STARTED)
            val todo3 = Todo(id = 3, title = "", state = Todo.State.NOT_STARTED)
            val todoManager: TodoManager = mock {
                on { getTodos() } doReturnConsecutively listOf(
                    Observable.just(listOf(todo1, todo2)),
                    Observable.just(listOf(todo1, todo2, todo3))
                )
            }

            val retryStream = PublishRelay.create<Unit>()
            val mainView = object : MainView {
                override fun onTodoSelected(): Observable<Int> = PublishRelay.create()

                override fun onTodoInProgessUpdated(): Observable<Int> = PublishRelay.create()

                override fun onRetry(): Observable<Unit> = retryStream
            }

            val subject = MainViewModel(todoManager)
            val testLiveDataObserver = subject.stateLiveData.test()
            subject.execute
            subject.bind(mainView)

            testLiveDataObserver
                .assertValueHistory(
                    MainViewModel.UIModel.State.Loading,
                    MainViewModel.UIModel.State.Success(listOf(todo1.toUI(), todo2.toUI()))
                )

            retryStream.accept(Unit)

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
package com.example.mvvmrx.ui

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmrx.databinding.ActivityMainBinding
import com.example.mvvmrx.ui.model.TodoUI
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

/**
 * Class that will be responsible for reflecting the emission form the VM (states / events) in the
 * UI.
 */
class MainViewImpl(
    private val viewBinding: ActivityMainBinding,
    val openDetail: (TodoUI) -> Unit
) : MainView {

    private val todoSelectedRelay = PublishRelay.create<Int>()
    private val todoInProgressUpdatedRelay = PublishRelay.create<Int>()
    val uiModelObserver = StateObserver()
    val eventObserver = EventObserver()

    private val todoListAdapter = TodoListAdapter(
        {
            todoSelectedRelay.accept(it.id)
        },
        {
            todoInProgressUpdatedRelay.accept(it.id)
        }
    )

    init {
        viewBinding.rvTodos.apply {
            layoutManager = LinearLayoutManager(viewBinding.root.context)
            adapter = todoListAdapter
            setHasFixedSize(false)
            addItemDecoration(
                DividerItemDecoration(
                    viewBinding.root.context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    override fun onTodoSelected(): Observable<Int> = todoSelectedRelay

    override fun onTodoInProgessUpdated(): Observable<Int> = todoInProgressUpdatedRelay

    override fun onRetry(): Observable<Unit> = viewBinding.swipeRefresh.refreshes()

    inner class StateObserver : Observer<MainViewModel.UIModel.State> {
        override fun onChanged(state: MainViewModel.UIModel.State) {
            Log.d("MainViewImpl", "state: $state")
            viewBinding.swipeRefresh.isRefreshing = false
            when (state) {
                is MainViewModel.UIModel.State.Success -> {
                    todoListAdapter.submitList(state.todos)
                }
                MainViewModel.UIModel.State.Loading -> {
                    viewBinding.swipeRefresh.isRefreshing = true
                }
            }
        }
    }

    inner class EventObserver : Observer<Event<MainViewModel.UIModel.Effect>> {
        override fun onChanged(event: Event<MainViewModel.UIModel.Effect>) {
            Log.d("MainViewImpl", "event with effect: ${event.peekContent()}")
            viewBinding.swipeRefresh.isRefreshing = false
            event.getContentIfNotHandled()?.run {
                when (this) {
                    is MainViewModel.UIModel.Effect.OpenDetail -> openDetail(this.todo)
                    MainViewModel.UIModel.Effect.Error -> {
                        Toast.makeText(viewBinding.root.context, "Error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }
}
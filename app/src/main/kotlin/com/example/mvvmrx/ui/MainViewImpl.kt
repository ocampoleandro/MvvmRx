package com.example.mvvmrx.ui

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmrx.databinding.ActivityMainBinding
import com.example.mvvmrx.ui.model.TodoUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.swiperefreshlayout.refreshes

/**
 * Class that will be responsible for reflecting the emission form the VM (states / events) in the
 * UI.
 */
class MainViewImpl(
    private val viewBinding: ActivityMainBinding,
    lifecycleScope: LifecycleCoroutineScope,
    val openDetail: (TodoUI) -> Unit
) : MainView {

    private val todoSelectedSharedFlow = MutableSharedFlow<Int>()
    private val todoInProgressUpdatedSharedFlow = MutableSharedFlow<Int>()
    val uiModelObserver = StateObserver()
    val eventObserver = EventObserver()

    private val todoListAdapter = TodoListAdapter(
        {
            lifecycleScope.launch { todoSelectedSharedFlow.emit(it.id) }
        },
        {
            lifecycleScope.launch { todoInProgressUpdatedSharedFlow.emit(it.id) }
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

    override fun onTodoSelected(): Flow<Int> = todoSelectedSharedFlow

    override fun onTodoInProgessUpdated(): Flow<Int> = todoInProgressUpdatedSharedFlow

    override fun onRetry(): Flow<Unit> = viewBinding.swipeRefresh.refreshes()

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
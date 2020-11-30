package com.example.mvvmrx.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmrx.domain.TodoManager
import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.ui.model.TodoUI
import com.example.mvvmrx.util.DispatcherProvider
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Reactive VM that will connect UI with business domain. This class does not neither contain android UI components
 * nor business logic (tt does not know where the information comes from or goes).
 *
 * Logic that does not need user input, it should be done upon calling [MainViewModel.execute].
 * Therefore, every information needed will have to be passed in the constructor.
 *
 * For logic that needs user interaction, then [MainViewModel.bind] should be used to connect VM
 * with View.
 *
 */
class MainViewModel(
    private val todoManager: TodoManager,
    private val dispatcherProvider: DispatcherProvider = DispatcherProvider.instance
) : ViewModel() {

    //list of disposables that will matter as long as the VM is alive. This will survive configuration changes.
    private val vmScopeCompositeDisposable = CompositeDisposable()

    private val _stateLiveData = MutableLiveData<UIModel.State>()
    private val _effectLiveData = MutableLiveData<Event<UIModel.Effect>>()

    //action that indicates we want to list todos.
    private val listTodoActionFlow = MutableSharedFlow<Unit>()
    private val updateTodoAction = MutableSharedFlow<Todo>()

    //public containers exposed to be consumed by the View.
    val stateLiveData: LiveData<UIModel.State> = _stateLiveData
    val effectLiveData: LiveData<Event<UIModel.Effect>> = _effectLiveData

    //used to cache the latest list retrieved from the server.
    @Volatile
    private var todoListCache: List<Todo> = emptyList()

    @OptIn(FlowPreview::class)
    val execute: Unit by lazy {
        //we place the list of todos here so we keep running this after a configuration change.
        viewModelScope.launch {
            listTodoActionFlow.collect {
                viewModelScope.launch {
                    listTodos().collect {
                        subscribe(it)
                    }
                }
            }

        }

        viewModelScope.launch {
            updateTodoAction.collect { todo ->
                //here we may throw an exception, which means a bug in the application.
                //this is up to you to catch this exception, show a nice ui effect and LOG the exception.
                //or simply crash the app.
                viewModelScope.launch {
                    todoManager.updateInProgress(todo)
                }
            }
        }

        //initial list of todos
        viewModelScope.launch {
            listTodoActionFlow.emit(Unit)
        }

        Unit
    }

    fun bind(mainView: MainView, lifecycleScope: CoroutineScope) {
        lifecycleScope.launch(dispatcherProvider.default) {
            mainView.onTodoSelected().collect { todoId ->
                val todo = todoListCache.find { it.id == todoId }!!
                subscribe(UIModel.Effect.OpenDetail(todo.toUI()))
            }
        }

        lifecycleScope.launch(dispatcherProvider.default) {
            mainView.onTodoInProgessUpdated().collect { todoId ->
                val todo = todoListCache.find { it.id == todoId }!!
                updateTodoAction.emit(todo)
            }
        }

        lifecycleScope.launch {
            mainView.onRetry().collect {
                listTodoActionFlow.emit(Unit)
            }
        }
    }

    private suspend fun listTodos(): Flow<UIModel> = withContext(dispatcherProvider.default) {
        todoManager.getTodos().onEach { todoListCache = it }
            .map { it.map { todo -> todo.toUI() } }
            .map {
                UIModel.State.Success(it)
            }
            .onStart<UIModel> {
                emit(UIModel.State.Loading)
            }
            .catch { UIModel.Effect.Error }
    }

    private fun subscribe(uiModel: UIModel) {
        when (uiModel) {
            is UIModel.State -> _stateLiveData.postValue(uiModel)
            is UIModel.Effect -> _effectLiveData.postValue(Event(uiModel))
        }
    }

    override fun onCleared() {
        vmScopeCompositeDisposable.clear()
        super.onCleared()
    }

    sealed class UIModel {
        sealed class State : UIModel() {
            object Loading : State()
            data class Success(val todos: List<TodoUI>) : State()
        }

        sealed class Effect : UIModel() {
            data class OpenDetail(val todo: TodoUI) : Effect()
            object Error : Effect()
        }
    }

}
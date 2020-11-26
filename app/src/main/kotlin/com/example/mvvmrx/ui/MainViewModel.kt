package com.example.mvvmrx.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mvvmrx.domain.TodoManager
import com.example.mvvmrx.domain.model.Todo
import com.example.mvvmrx.ui.model.TodoUI
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

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
class MainViewModel(private val todoManager: TodoManager) : ViewModel() {

    //list of disposables that will matter as long as the VM is alive. This will survive configuration changes.
    private val vmScopeCompositeDisposable = CompositeDisposable()

    private val _stateLiveData = MutableLiveData<UIModel.State>()
    private val _effectLiveData = MutableLiveData<Event<UIModel.Effect>>()
    //action that indicates we want to list todos.
    private val listTodoActionRelay: Relay<Unit> = PublishRelay.create()

    //public containers exposed to be consumed by the View.
    val stateLiveData: LiveData<UIModel.State> = _stateLiveData
    val effectLiveData: LiveData<Event<UIModel.Effect>> = _effectLiveData

    //used to cache the latest list retrieved from the server.
    private val todoRelay = BehaviorRelay.create<List<Todo>>()

    val execute: Unit by lazy {
        //we place the list of todos here so we keep running this after a configuration change.
        vmScopeCompositeDisposable.add(
            listTodoActionRelay.switchMap {
                listTodos()
            }.subscribe { uiModel -> subscribe(uiModel) }
        )

        //initial list of todos
        listTodoActionRelay.accept(Unit)

        Unit
    }

    fun bind(mainView: MainView): CompositeDisposable {
        val viewScopeCompositeDisposable = CompositeDisposable()

        viewScopeCompositeDisposable.add(
            mainView.onTodoSelected().flatMapSingle { todoId ->
                todoRelay.map { todos -> todos.find { it.id == todoId }!! }
                    .firstOrError()
                    .map { todo -> UIModel.Effect.OpenDetail(todo.toUI()) }
            }.subscribe { uiModel ->
                _effectLiveData.postValue(
                    Event(
                        uiModel
                    )
                )
            }
        )

        //if we make the update in of the state in a DB, server, etc, then we should follow the same
        //approach as in the list of todos -> create a relay which will sent an action that will start the logic of updating the state.
        viewScopeCompositeDisposable.add(
            mainView.onTodoInProgessUpdated().flatMapCompletable { todoId ->
                todoRelay.map { todos -> todos.find { it.id == todoId }!! }
                    .firstOrError()
                    .flatMapCompletable {
                        //here we may throw an exception, which means a bug in the application.
                        //this is up to you to catch this exception, show a nice ui effect and LOG the exception.
                        //or simply crash the app.
                        todoManager.updateInProgress(it)
                    }
            }.subscribe()
        )

        viewScopeCompositeDisposable.add(
            mainView.onRetry()
                .subscribe { listTodoActionRelay.accept(Unit) }
        )
        return viewScopeCompositeDisposable
    }

    private fun listTodos(): Observable<UIModel> {
        return todoManager.getTodos()
            .doAfterNext { todoRelay.accept(it) }
            .map { it.map { todo -> todo.toUI() } }
            .map { UIModel.State.Success(it) as UIModel }
            .startWith(UIModel.State.Loading)
            .onErrorReturnItem(UIModel.Effect.Error)
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
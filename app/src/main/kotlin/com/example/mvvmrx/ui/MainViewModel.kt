package com.example.mvvmrx.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mvvmrx.domain.Todo
import com.example.mvvmrx.domain.TodoRepository
import com.jakewharton.rxrelay2.BehaviorRelay
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
class MainViewModel(private val todoRepository: TodoRepository) : ViewModel() {

    //list of disposables that will matter as long as the VM is alive. This will survive configuration changes.
    private val vmScopeCompositeDisposable = CompositeDisposable()

    private val _liveData = MutableLiveData<UIModel.State>()
    private val _effectLiveData = MutableLiveData<Event<UIModel.Effect>>()

    //public containers exposed to be consumed by the View.
    val liveData: LiveData<UIModel.State> = _liveData
    val effectLiveData: LiveData<Event<UIModel.Effect>> = _effectLiveData

    //used to cache the latest list retrieved from the server.
    private val todoRelay = BehaviorRelay.create<List<Todo>>()

    val execute: Unit by lazy {
        vmScopeCompositeDisposable.add(listTodos().subscribe { uiModel -> subscribe(uiModel) })
        Unit
    }

    fun bind(mainView: MainView): CompositeDisposable {
        val viewScopeCompositeDisposable = CompositeDisposable()

        viewScopeCompositeDisposable.add(
            mainView.onTodoSelected().flatMap { todoId ->
                todoRelay.map { todos -> todos.find { it.id == todoId }!! }
                    .map { todo ->
                        UIModel.Effect.OpenDetail(
                            todo
                        )
                    }
            }.subscribe { uiModel ->
                _effectLiveData.postValue(
                    Event(
                        uiModel
                    )
                )
            }
        )

        viewScopeCompositeDisposable.add(
            mainView.onRetry().switchMap { listTodos() }
                .subscribe { uiModel -> subscribe(uiModel) }
        )
        return viewScopeCompositeDisposable
    }

    private fun listTodos(): Observable<UIModel> {
        return todoRepository.getTodos()
            .doOnSuccess { todoRelay.accept(it) }
            .map { UIModel.State.Success(it) as UIModel }
            .toObservable()
            .startWith(UIModel.State.Loading)
            .onErrorReturnItem(UIModel.Effect.Error)
    }

    private fun subscribe(uiModel: UIModel) {
        when (uiModel) {
            is UIModel.State -> _liveData.postValue(uiModel)
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
            data class Success(val todos: List<Todo>) : State()
        }

        sealed class Effect : UIModel() {
            data class OpenDetail(val todo: Todo) : Effect()
            object Error : Effect()
        }
    }

}
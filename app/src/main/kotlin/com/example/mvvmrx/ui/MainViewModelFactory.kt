package com.example.mvvmrx.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mvvmrx.domain.TodoRepository

class MainViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModel = MainViewModel(TodoRepository.instance)
        viewModel.execute
        return viewModel as T
    }

}
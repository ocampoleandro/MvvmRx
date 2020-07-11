package com.example.mvvmrx.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mvvmrx.databinding.ActivityMainBinding
import io.reactivex.disposables.CompositeDisposable

/**
 * In this case, activities and fragments will behave as containers of views. The only logic that
 * will reside in such components is the one that is not UI specific, ex: navigation, receiving
 * results, etc.
 *
 * For UI logic, check viewImpl, ex: [MainViewImpl]
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModelFactory() }
    //list of disposables that will matter as long as the VIEW is alive
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //init UI.
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewImpl = MainViewImpl(binding) {
            val bundle = Bundle()
            bundle.putParcelable(DetailActivity.KEY_TODO, it)
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtras(bundle)
            }
            startActivity(intent)
        }
        //bind between view model and UI. This is were we connect both, so the UI react to emissions (UI states or events)
        //and vm reacts to actions.
        compositeDisposable.add(viewModel.bind(viewImpl))

        //we observe UI states
        viewModel.stateLiveData.observe(this, viewImpl.uiModelObserver)
        //we observe events
        viewModel.effectLiveData.observe(this, viewImpl.eventObserver)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}
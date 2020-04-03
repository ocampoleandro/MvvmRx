package com.example.mvvmrx.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mvvmrx.domain.Todo
import com.example.mvvmrx.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        //deal with the double bang ;)
        val todo: Todo = intent.extras!!.getParcelable(
            KEY_TODO
        )!!
        viewBinding.tvTitle.text = todo.title
    }


    companion object {
        const val KEY_TODO = "key.todo"
    }
}
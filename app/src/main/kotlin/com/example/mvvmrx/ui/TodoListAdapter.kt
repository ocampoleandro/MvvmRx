package com.example.mvvmrx.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmrx.databinding.ListTodoItemBinding
import com.example.mvvmrx.ui.model.TodoUI

class TodoListAdapter(
    val elementClicked: (Int) -> Unit
) : ListAdapter<TodoUI, TodoListAdapter.TodoViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val viewBinding =
            ListTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = getItem(position)
        holder.bind(todo)
        holder.itemView.setOnClickListener { elementClicked(todo.id) }
    }

    class TodoViewHolder(private val viewBinding: ListTodoItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(todo: TodoUI) {
            viewBinding.tvTitle.text = todo.title
            viewBinding.ivCompleted.setBackgroundColor(ContextCompat.getColor(viewBinding.ivCompleted.context, todo.colorState))
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TodoUI>() {
            override fun areItemsTheSame(oldItem: TodoUI, newItem: TodoUI): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TodoUI, newItem: TodoUI): Boolean {
                return oldItem == newItem
            }
        }
    }

}
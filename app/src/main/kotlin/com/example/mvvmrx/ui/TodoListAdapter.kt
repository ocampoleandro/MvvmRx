package com.example.mvvmrx.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmrx.R
import com.example.mvvmrx.databinding.ListTodoItemBinding
import com.example.mvvmrx.domain.Todo

class TodoListAdapter(
    val elementClicked: (Int) -> Unit
) : ListAdapter<Todo, TodoListAdapter.TodoViewHolder>(
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
        fun bind(todo: Todo) {
            viewBinding.tvTitle.text = todo.title
            viewBinding.tvCompleted.text = if (todo.completed) {
                viewBinding.root.context.getText(R.string.list_todo_completed)
            } else {
                viewBinding.root.context.getText(R.string.list_todo_completed_not)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Todo>() {
            override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem == newItem
            }
        }
    }

}
package com.app.realmtodoapp.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.realmtodoapp.data.remote.TodosListResponse
import com.app.realmtodoapp.databinding.ItemTodoBinding

class TodoAdapter(
    private var todos: ArrayList<TodosListResponse.Todo>,
    private val onEditClick: (TodosListResponse.Todo,Int) -> Unit,
    private val onDeleteClick: (TodosListResponse.Todo,Int) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        Log.d("TodoAdapter==", "onBindViewHolder: ${todos[position]}")
        val todo = todos[position]
             holder.bind(todo,position)


    }

    override fun getItemCount(): Int = todos.size

    inner class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(todo: TodosListResponse.Todo,position: Int) {
            binding.tvTodoTitle.text = todo.todo
            binding.btnEdit.setOnClickListener {
                onEditClick(todo,position)

            }
            binding.btnDelete.setOnClickListener { onDeleteClick(todo,position) }
        }
    }


    fun updateTodoItem(updatedTodo: TodosListResponse.Todo) {
        val index = todos.indexOfFirst { it.id == updatedTodo.id }
        if (index != -1) {
            todos[index] = updatedTodo
            notifyItemChanged(index)
        }
    }

    fun removeTodoItem(index: Int) {
        if (index >= 0 && index < todos.size) {
            todos.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, todos.size)
        }
    }

}

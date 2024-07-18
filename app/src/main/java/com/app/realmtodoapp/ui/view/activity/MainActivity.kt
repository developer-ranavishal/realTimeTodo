package com.app.realmtodoapp.ui.view.activity
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.app.realmtodoapp.R
import com.app.realmtodoapp.data.remote.TodosListResponse
import com.app.realmtodoapp.databinding.ActivityMainBinding
import com.app.realmtodoapp.databinding.DialogAddTodoBinding
import com.app.realmtodoapp.databinding.DialogUpdateTodoBinding
import com.app.realmtodoapp.extensions.showToast
import com.app.realmtodoapp.ui.adapter.TodoAdapter
import com.app.realmtodoapp.ui.view.base.BaseActivity
import com.app.realmtodoapp.ui.viewmodel.TodoViewModel
import com.app.realmtodoapp.utils.Resource
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val todoViewModel : TodoViewModel by viewModel()
    private lateinit var adapter: TodoAdapter
    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun doWorkHere() {
        observeToDoList()

    }

    override fun onInternetAvailable() {
        todoViewModel.getAllUserTodoList()
    }

    override fun onInternetUnavailable() {
        showSnackBar("Please Connect to Internet!")
    }

    private fun setTodoListAdapter(todoList: ArrayList<TodosListResponse.Todo>){
        adapter = TodoAdapter(todoList, onEditClick = { todo ,index->
            // Handle edit action
            showUpdateDialog(todo,index)

        }, onDeleteClick = { todo,index ->
            // Handle delete action
            todoViewModel.deleteTodoFromFirebase(index)
        })
        binding?.recyclerView?.adapter = adapter
    }

    private fun observeToDoList() {
        todoViewModel.todos.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showProgressDialog()
                is Resource.Success -> {
                    hideProgressDialog()
                    resource.data?.let { list ->
                        setTodoListAdapter(ArrayList(list.todos))
                    }
                }
                is Resource.Error -> {
                    hideProgressDialog()
                    showSnackBar(resource.message ?: "An error occurred")
                }

            }
        }

        todoViewModel.deletedTodoIndex.observe(this) { index ->
            index?.let {
                adapter.removeTodoItem(index)
            }
        }
    }


    private fun showUpdateDialog(todo: TodosListResponse.Todo, index: Int) {
        val dialogBinding = DialogUpdateTodoBinding.inflate(LayoutInflater.from(this),null,false)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Update Todo")
            .setView(dialogBinding.root)
            .create()
        dialogBinding.btnUpdate.setOnClickListener {
            val updatedTitle = dialogBinding.etTodoTitle.text.toString().trim()
            if (updatedTitle.isNotEmpty()) {
                todo.todo = updatedTitle
                todoViewModel.updateTodoInFirebase(index,todo)
                dialog.dismiss()
            } else {
                showToast("Title cannot be empty")
            }
        }

        dialog.show()

        dialogBinding.etTodoTitle.setText(todo.todo)
    }

    private fun showAddDialog(todo: TodosListResponse.Todo) {
        val dialogBinding = DialogAddTodoBinding.inflate(LayoutInflater.from(this),null,false)
        val dialog = AlertDialog.Builder(this, R.style.AlertStyle)
            .setTitle("Add New Todo")
            .setView(dialogBinding.root)
            .create()
        dialogBinding.btnAdd.setOnClickListener {
            val addNewTodo = dialogBinding.etTodoNew.text.toString().trim()
            if (addNewTodo.isNotEmpty()) {
                todo.todo = addNewTodo
            } else {
                showToast("Todo cannot be empty")
            }
        }

        dialog.show()

    }



}
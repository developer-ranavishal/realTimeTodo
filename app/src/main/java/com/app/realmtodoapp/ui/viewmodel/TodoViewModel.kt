package com.app.realmtodoapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.realmtodoapp.data.remote.ApiService
import com.app.realmtodoapp.data.remote.TodosListResponse
import com.app.realmtodoapp.utils.Resource
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TodoViewModel(private val apiService: ApiService, private val firebaseDatabase: FirebaseDatabase) : ViewModel() {

    // LiveData to hold the resource state of todos
    private val _todos = MutableLiveData<Resource<TodosListResponse?>>()
    val todos: LiveData<Resource<TodosListResponse?>> = _todos

    // LiveData to hold the resource state of todos
    private val _updatedTodo = MutableLiveData<TodosListResponse.Todo?>()
    val updatedTodo: MutableLiveData<TodosListResponse.Todo?> = _updatedTodo


    private val _deletedTodoIndex = MutableLiveData<Int?>()
    val deletedTodoIndex: LiveData<Int?> = _deletedTodoIndex


    /**
     * Fetches todos from Firebase database.
     * If todos exist, posts [Resource.Success] with the fetched data.
     * If no todos exist, fetches from API and stores in Firebase.
     */
    private fun fetchTodosFromFirebase() {
        val database = firebaseDatabase.getReference("todos")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val todosList = snapshot.getValue(TodosListResponse::class.java)
                    _todos.postValue(Resource.Success(todosList))
                    Log.d(TAG, "Data fetched from Firebase: $todosList")
                } else {
                    // If no data exists in Firebase, fetch from API and store in Firebase
                    fetchFromApiAndStoreInFirebase()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _todos.postValue(Resource.Error(error.message))
                Log.e(TAG, "Error fetching data from Firebase: ${error.message}")
            }
        })
    }

    /**
     * Fetches todos from API and stores them in Firebase database.
     * Posts [Resource.Loading] while fetching, [Resource.Success] after successful fetch and store,
     * or [Resource.Error] on failure.
     */
    private fun fetchFromApiAndStoreInFirebase() {
        val handler = CoroutineExceptionHandler { _, exception ->
            _todos.postValue(Resource.Error(exception.message ?: "Unknown error"))
            Log.e(TAG, "Error fetching data from API: ${exception.message}")
        }

        viewModelScope.launch(handler) {
            _todos.postValue(Resource.Loading())
            try {
                // Fetch todos from API
                val response = withContext(Dispatchers.IO) { apiService.getTodosList() }
                val todos = response.body()

                // Store todos in Firebase
                val database = firebaseDatabase.getReference("todos")
                todos?.let { database.setValue(it) }

                // Post success state with fetched todos
                _todos.postValue(Resource.Success(todos))
                Log.d(TAG, "Data fetched from API and stored in Firebase: $todos")
            } catch (e: Exception) {
                // Post error state on exception
                _todos.postValue(Resource.Error(e.message ?: "Unknown error"))
                Log.e(TAG, "Error fetching data from API: ${e.message}")
            }
        }
    }

    /**
     * Initiates fetching todos from Firebase.
     * Useful when refreshing or re-fetching data.
     */
    fun getAllUserTodoList() {
        fetchTodosFromFirebase()
    }



    /**
     * Adds a new todo to the Firebase database.
     * Posts [Resource.Loading] while adding, [Resource.Success] after successful addition,
     * or [Resource.Error] on failure.
     */
    fun addNewTodo(newTodo: String) {
        val database = firebaseDatabase.getReference("todos")

        // Retrieve existing todos to determine the highest id and existing userIds
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var highestId = -1
                val existingUserIds = mutableSetOf<Int>()

                // Loop through existing todos to find highest id and collect userIds
                for (todoSnapshot in snapshot.children) {
                    val todo = todoSnapshot.getValue(TodosListResponse.Todo::class.java)
                    todo?.let {
                        if (it.id > highestId) highestId = it.id
                        existingUserIds.add(it.userId)
                    }
                }

                // Generate new id and unique userId
                val newId = highestId + 1
                val newUserId = generateUniqueUserId(existingUserIds)

                // Create new todo object
                val newTodoObject = TodosListResponse.Todo(
                    id = newId,
                    todo = newTodo,
                    userId = newUserId,
                    completed = false
                )

                // Add the new todo to Firebase
                database.child(newId.toString()).setValue(newTodoObject)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Successfully added new todo, fetch updated todos
                            fetchTodosFromFirebase()
                        } else {
                            // Handle error adding new todo
                            _todos.postValue(Resource.Error(task.exception?.message ?: "Unknown error"))
                            Log.e(TAG, "Error adding new todo: ${task.exception?.message}")
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                _todos.postValue(Resource.Error(error.message))
                Log.e(TAG, "Error fetching data from Firebase: ${error.message}")
            }
        })
    }


    /**
     * Generates a unique userId that is not present in the existingUserIds set.
     */
    private fun generateUniqueUserId(existingUserIds: Set<Int>): Int {
        var newUserId: Int
        do {
            newUserId = (0..1000).random()
        } while (existingUserIds.contains(newUserId))
        return newUserId
    }


    /**
     * Deletes a todo from the Firebase database.
     * Posts [Resource.Loading] while deleting, [Resource.Success] after successful deletion,
     * or [Resource.Error] on failure.
     */
    fun deleteTodoFromFirebase(index: Int) {
        firebaseDatabase.getReference("todos/todos").child(index.toString()).removeValue().addOnSuccessListener {
            // Refetch the list after deletion to ensure consistency
            _deletedTodoIndex.postValue(index)
            getAllUserTodoList()
        }.addOnFailureListener {
            _todos.postValue(Resource.Error(it.message ?: "An error occurred while deleting"))
        }
    }



    /**
     * Updates an existing todo in the Firebase database.
     * Posts [Resource.Loading] while updating, [Resource.Success] after successful update,
     * or [Resource.Error] on failure.
     */
    fun updateTodoInFirebase(index: Int, updatedTodo: TodosListResponse.Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firebaseDatabase.getReference("todos/todos").child(index.toString()).setValue(updatedTodo).await()
                // Post value to indicate update success and update UI
                getAllUserTodoList() // Refetch the list after updating to ensure consistency
            } catch (e: Exception) {
                _todos.postValue(Resource.Error(e.message ?: "An error occurred while updating"))
            }
        }
    }




    companion object {
        private const val TAG = "TodoViewModel"
    }
}

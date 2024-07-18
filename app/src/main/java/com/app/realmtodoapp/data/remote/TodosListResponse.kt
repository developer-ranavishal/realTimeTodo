package com.app.realmtodoapp.data.remote

data class TodosListResponse(
    val limit: Int = 0,
    val skip: Int = 0,
    var todos: ArrayList<Todo> = arrayListOf(),
    val total: Int = 0
) {
    data class Todo(
        val completed: Boolean = false,
        var id: Int = 0,
        var todo: String = "",
        val userId: Int = 0
    )
}
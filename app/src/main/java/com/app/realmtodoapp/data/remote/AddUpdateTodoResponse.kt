package com.app.realmtodoapp.data.remote

data class AddUpdateTodoResponse(
    val completed: Boolean = false,
    val id: String = "",
    val todo: String = "",
    val userId: Int = 0
)
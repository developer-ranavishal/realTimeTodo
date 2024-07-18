package com.app.realmtodoapp.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("todos")
    suspend fun getTodosList() : Response<TodosListResponse>
    @PUT("todos/{id}")
    suspend fun updateTodoItem(@Path("id") id: Int, @Body todo: TodosListResponse.Todo): Response<TodosListResponse.Todo>
}

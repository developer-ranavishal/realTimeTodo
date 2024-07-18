package com.app.realmtodoapp.di
import com.app.realmtodoapp.data.remote.ApiService
import com.app.realmtodoapp.ui.viewmodel.TodoViewModel
import com.google.firebase.database.FirebaseDatabase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val appModule = module {

    //Provide Retrofit Instance
    single {
        Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Provide API service
    single { get<Retrofit>().create(ApiService::class.java) }

    // Provide FirebaseDatabase with persistence enabled
    single {
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
        }
    }

    // Provide ViewModel
    viewModel {
        TodoViewModel(get(),get()) // Pass ApiService as a parameter
    }

}
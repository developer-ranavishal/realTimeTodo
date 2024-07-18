package com.app.realmtodoapp.ui.view.activity

import android.app.Application
import com.app.realmtodoapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AppController : Application() {

        override fun onCreate() {
            super.onCreate()
            startKoin {
                androidLogger()
                androidContext(this@AppController)
                modules(appModule)
            }
        }

}


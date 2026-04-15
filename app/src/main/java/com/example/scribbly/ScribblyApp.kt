package com.example.scribbly

import android.app.Application

class ScribblyApp : Application() {
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}

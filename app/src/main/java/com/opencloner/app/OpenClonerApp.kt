package com.opencloner.app

import android.app.Application
import android.util.Log

class OpenClonerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i(TAG, "OpenCloner Application initialized.")
    }

    companion object {
        private const val TAG = "OpenClonerApp"
        lateinit var instance: OpenClonerApp
            private set
    }
}

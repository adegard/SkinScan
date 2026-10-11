package com.example.skinscan

import android.app.Application
import com.example.skinscan.di.AppContainer

class SkinScanApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

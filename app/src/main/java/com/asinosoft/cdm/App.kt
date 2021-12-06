package com.asinosoft.cdm

import android.app.Application
import com.asinosoft.cdm.dialer.NotificationManager
import timber.log.Timber

class App : Application() {
    val notification by lazy { NotificationManager(this) }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

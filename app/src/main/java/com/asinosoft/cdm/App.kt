package com.asinosoft.cdm

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

import java.util.*


class App : Application() {

    var intent: Intent? = null

    companion object{
        lateinit var INSTANCE : App
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

    }
}
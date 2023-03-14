package com.asinosoft.cdm

import android.app.Application
import androidx.room.Room
import com.asinosoft.cdm.api.Config
import com.asinosoft.cdm.api.ConfigImpl
import com.asinosoft.cdm.dialer.NotificationManager
import com.google.firebase.FirebaseApp
import com.yandex.mobile.ads.common.MobileAds
import timber.log.Timber
import java.util.*

class App : Application() {
    companion object {
        lateinit var instance: App
    }

    val config: Config by lazy { ConfigImpl(this) }
    val database: Database by lazy {
        Room.databaseBuilder(this, Database::class.java, "cache").build()
    }
    val notification by lazy { NotificationManager(this) }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        FirebaseApp.initializeApp(this)
        instance = this

        if ("ru" == Locale.getDefault().language) {
            initializeYandexAds()
        }
    }

    private fun initializeYandexAds() {
        MobileAds.initialize(this) {
            Timber.i("Yandex Mobile Ads initialized")
        }
    }
}

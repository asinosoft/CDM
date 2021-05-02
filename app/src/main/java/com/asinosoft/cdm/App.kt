package com.asinosoft.cdm

import android.app.Application
import com.asinosoft.cdm.api.CallHistoryRepository
import com.asinosoft.cdm.api.CallHistoryRepositoryImpl
import com.asinosoft.cdm.api.ContactRepository
import com.asinosoft.cdm.api.ContactRepositoryImpl

class App : Application() {

    companion object {
        lateinit var INSTANCE: App
        lateinit var contactRepository: ContactRepository
        lateinit var callHistoryRepository: CallHistoryRepository
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        contactRepository = ContactRepositoryImpl(this)
        callHistoryRepository = CallHistoryRepositoryImpl(this.contentResolver)
    }
}

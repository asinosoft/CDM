package com.asinosoft.cdm.dialer

import android.telecom.Call
import android.telecom.InCallService
import com.asinosoft.cdm.App
import com.asinosoft.cdm.activities.OngoingCallActivity
import com.asinosoft.cdm.helpers.id
import timber.log.Timber

class CallService : InCallService() {
    companion object {
        var instance: CallService? = null
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Timber.d("Call # %d | state → %s", call.id(), getCallStateText(state))

            (application as App).notification.update(call)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        calls?.firstOrNull()?.let { call ->
            onCallAdded(call)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    override fun onCallAdded(call: Call) {
        Timber.i("Новый звонок → %s", call.details.handle.schemeSpecificPart)
        call.registerCallback(callback)
        (application as App).notification.show(calls)
        startActivity(OngoingCallActivity.intent(this, call))
    }

    override fun onCallRemoved(call: Call) {
        Timber.i("Звонок завершён → %s", call.details.handle.schemeSpecificPart)
        call.unregisterCallback(callback)
        (application as App).notification.show(calls)
    }

    fun getCallById(id: Int): Call? =
        calls.find { call -> call.id() == id }
}

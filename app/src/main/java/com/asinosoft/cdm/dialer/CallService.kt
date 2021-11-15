package com.asinosoft.cdm.dialer

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.asinosoft.cdm.activities.OngoingCallActivity
import timber.log.Timber

class CallService : InCallService() {
    private val notification by lazy { NotificationManager(this) }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state != Call.STATE_DISCONNECTED) {
                notification.show(call, state)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        CallManager.callService = this
        calls?.firstOrNull()?.let { call ->
            onCallAdded(call)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CallManager.callService = null
    }

    override fun onCallAdded(call: Call) {
        Timber.i("Новый звонок → %s", call.details.handle)
        CallManager.setCall(call)
        call.registerCallback(callback)
        notification.show(call, call.state)

        Intent(this, OngoingCallActivity::class.java).let { activity ->
            activity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(activity)
        }
    }

    override fun onCallRemoved(call: Call) {
        Timber.i("Звонок завершён → %s", call.details.handle)
        CallManager.resetCall()
        call.unregisterCallback(callback)
        notification.hide()
    }
}

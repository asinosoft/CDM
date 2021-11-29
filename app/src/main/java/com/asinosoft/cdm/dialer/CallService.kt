package com.asinosoft.cdm.dialer

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.asinosoft.cdm.activities.OngoingCallActivity

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
        calls?.firstOrNull()?.let { call ->
            onCallAdded(call)
        }
    }

    override fun onCallAdded(call: Call) {
        Log.i("CDM|CallService", "add → ${call.details.handle}")
        CallManager.setCall(call)
        call.registerCallback(callback)
        notification.show(call, call.state)

        Intent(this, OngoingCallActivity::class.java).let { activity ->
            activity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            startActivity(activity)
        }
    }

    override fun onCallRemoved(call: Call) {
        Log.i("CDM|CallService", "remove → ${call.details.handle}")
        CallManager.resetCall()
        call.unregisterCallback(callback)
        notification.hide()
    }
}

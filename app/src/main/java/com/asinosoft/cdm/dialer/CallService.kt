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
                notification.show(state)
            }
        }
    }

    override fun onCreate() {
        Log.d("CDM|CallService::onCreate", "")
        super.onCreate()
        CallManager.registerCallback(callback)
    }

    override fun onCallAdded(call: Call) {
        Log.i("CDM|CallService", "Call added: ${call.details.handle}")
        CallManager.setCall(this, call)
        Intent(this, OngoingCallActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
        notification.show(call.state)
    }

    override fun onCallRemoved(call: Call) {
        Log.i("CDM|CallService", "Call removed: ${call.details.handle}")
        CallManager.resetCall()
        notification.hide()
    }

    override fun onDestroy() {
        Log.d("CDM|CallService::onDestroy", "")
        super.onDestroy()
        CallManager.unregisterCallback(callback)
        notification.hide()
    }
}

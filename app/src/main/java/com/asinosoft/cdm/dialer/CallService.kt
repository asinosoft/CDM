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

    override fun onCallAdded(call: Call) {
        Log.i("CallService", "Call added: ${call.details.handle}")
        CallManager.setCall(this, call)
        CallManager.registerCallback(callback)
        Intent(this, OngoingCallActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
        notification.show(call.state)
    }

    override fun onCallRemoved(call: Call) {
        Log.i("CallService", "Call removed: ${call.details.handle}")
        CallManager.resetCall(this)
        CallManager.unregisterCallback(callback)
        notification.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        CallManager.unregisterCallback(callback)
        notification.hide()
    }
}

package com.asinosoft.cdm.dialer

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.asinosoft.cdm.activities.OngoingCallActivity

class CallService : InCallService() {

    override fun onCallAdded(call: Call) {
        Log.i("CallService", "Call added: ${call.details.handle}")
        CallManager.call = call
        Intent(this, OngoingCallActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
    }

    override fun onCallRemoved(call: Call) {
        Log.i("CallService", "Call removed: ${call.details.handle}")
        CallManager.resetCall()
    }
}

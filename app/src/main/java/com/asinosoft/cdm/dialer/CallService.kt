package com.asinosoft.cdm.dialer

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.asinosoft.cdm.OngoingCallActivity

class CallService: InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        var intent = Intent(this, OngoingCallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setData(call.details.handle)
        startActivity(intent)
        OngoingCall.call = call
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        OngoingCall.call = null
    }

}
package com.asinosoft.cdm.dialer

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.asinosoft.cdm.OngoingCallActivity

class CallService: InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        OngoingCallActivity.start(this, call)
        OngoingCall.call = call
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        OngoingCall.call = null
    }

}
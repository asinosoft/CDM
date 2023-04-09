package com.asinosoft.cdm.dialer

import android.net.Uri
import android.telecom.Call
import android.telecom.InCallService
import com.asinosoft.cdm.App
import com.asinosoft.cdm.activities.OngoingCallActivity
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.helpers.callState
import com.asinosoft.cdm.helpers.phone
import timber.log.Timber

class CallService : InCallService() {
    companion object {
        var instance: CallService? = null
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Timber.d("Call # %s | state → %s", call.phone, getCallStateText(state))

            App.instance.notification.update(call)
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
        App.instance.notification.show(calls)
        startActivity(OngoingCallActivity.intent(this, call))
        if (calls.size >= 2) {
            Analytics.logDoubleCall()
        }
        if (Call.STATE_RINGING == call.callState) {
            Analytics.logIncomingCall()
        }
    }

    override fun onCallRemoved(call: Call) {
        Timber.i("Звонок завершён → %s", call.details.handle.schemeSpecificPart)
        call.unregisterCallback(callback)
        App.instance.notification.show(calls)
    }

    fun getCall(phone: Uri?): Call? =
        calls.find { call -> call.details.handle == phone }

    fun getNextCall(): Call? =
        calls.find { call -> Call.STATE_DISCONNECTING != call.callState && Call.STATE_DISCONNECTED != call.callState }
}

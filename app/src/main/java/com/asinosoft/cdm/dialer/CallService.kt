package com.asinosoft.cdm.dialer

import android.telecom.Call
import android.telecom.InCallService
import com.asinosoft.cdm.App
import com.asinosoft.cdm.activities.OngoingCallActivity
import timber.log.Timber

class CallService : InCallService() {
    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state != Call.STATE_DISCONNECTED) {
                (application as App).notification.show(call, state)
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
        (application as App).notification.show(call, call.state)

        startActivity(OngoingCallActivity.intent(this))
    }

    override fun onCallRemoved(call: Call) {
        Timber.i("Звонок завершён → %s", call.details.handle)
        CallManager.resetCall()
        call.unregisterCallback(callback)
        (application as App).notification.hide()
    }
}

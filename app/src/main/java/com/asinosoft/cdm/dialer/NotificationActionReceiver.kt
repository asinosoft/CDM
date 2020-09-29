package com.asinosoft.cdm.dialer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asinosoft.cdm.OngoingCallActivity

class NotificationActionReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        when(action){
            ACCEPT_CALL -> OngoingCall.answer()
            DECLINE_CALL -> OngoingCall.reject()
        }
    }

}
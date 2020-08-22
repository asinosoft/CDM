package com.asinosoft.cdm.dialer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asinosoft.cdm.OngoingCallActivity

class NotificationActionReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action

        if (action == OngoingCallActivity().ACTION_ANSWER) {
            // If the user pressed "Answer" from the notification
            OngoingCall.answer()
        }
        else if (action == OngoingCallActivity().ACTION_HANGUP) {
            // If the user pressed "Hang up" from the notification
            OngoingCall.reject()
        }
    }

}
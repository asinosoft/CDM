package com.asinosoft.cdm.dialer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.asinosoft.cdm.activities.OngoingCallActivity
import org.jetbrains.anko.audioManager

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        var isSpeakerOn = false
        var isMicrophoneOn = true

        val action: String? = intent.action
        when (action) {
            ACCEPT_CALL -> CallManager.accept()
            DECLINE_CALL -> CallManager.reject()
            MUTE_CALL -> {
                isMicrophoneOn = !isMicrophoneOn
                context.getSystemService(InCallService::class.java)?.setMuted(!isMicrophoneOn)
            }
            SPEAKER_CALL -> {
                isSpeakerOn = !isSpeakerOn
                OngoingCallActivity().audioManager.isSpeakerphoneOn = isSpeakerOn
                val newRoute =
                    if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
                context.getSystemService(InCallService::class.java)?.setAudioRoute(newRoute)
            }
        }
    }
}

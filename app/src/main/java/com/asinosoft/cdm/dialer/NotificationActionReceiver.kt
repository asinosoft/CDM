package com.asinosoft.cdm.dialer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.CallAudioState
import com.asinosoft.cdm.OngoingCallActivity
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
                OngoingCallActivity().audioManager.isMicrophoneMute = !isMicrophoneOn
                CallManager.inCallService?.setMuted(!isMicrophoneOn)
            }
            SPEAKER_CALL -> {
                isSpeakerOn = !isSpeakerOn
                OngoingCallActivity().audioManager.isSpeakerphoneOn = isSpeakerOn
                val newRoute =
                    if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
                CallManager.inCallService?.setAudioRoute(newRoute)
            }
        }
    }

}
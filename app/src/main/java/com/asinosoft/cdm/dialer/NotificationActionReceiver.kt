package com.asinosoft.cdm.dialer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.CallAudioState
import com.asinosoft.cdm.activities.OngoingCallActivity
import com.asinosoft.cdm.helpers.accept
import com.asinosoft.cdm.helpers.audioManager
import com.asinosoft.cdm.helpers.reject

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        var isSpeakerOn = false
        var isMicrophoneOn = true

        when (intent.action) {
            ACCEPT_CALL -> {
                CallService.instance?.getCall(intent.data)?.let { call ->
                    call.accept()
                    context.startActivity(OngoingCallActivity.intent(context, call))
                }
            }
            DECLINE_CALL -> {
                CallService.instance?.getCall(intent.data)?.reject()
            }
            MUTE_CALL -> {
                isMicrophoneOn = !isMicrophoneOn
                CallService.instance?.setMuted(!isMicrophoneOn)
            }
            SPEAKER_CALL -> {
                isSpeakerOn = !isSpeakerOn
                OngoingCallActivity().audioManager.isSpeakerphoneOn = isSpeakerOn
                val newRoute =
                    if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
                CallService.instance?.setAudioRoute(newRoute)
            }
        }
    }
}

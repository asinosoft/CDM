package com.asinosoft.cdm.dialer

import android.app.NotificationManager
import android.content.Context
import android.telecom.Call
import com.asinosoft.cdm.R
import java.util.*

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

private const val PATH = "com.asinosoft.cdm.dialer.action."
const val ACCEPT_CALL = PATH + "accept_call"
const val DECLINE_CALL = PATH + "decline_call"
const val MUTE_CALL = PATH + "mute_call"
const val SPEAKER_CALL = PATH + "speaker_call"

fun Int.getFormattedDuration(): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}

fun Context.getCallStateText(callState: Int): CharSequence =
    when (callState) {
        Call.STATE_RINGING -> getText(R.string.state_call_ringing)
        Call.STATE_DIALING -> getText(R.string.status_call_dialing)
        Call.STATE_ACTIVE -> getText(R.string.status_call_active)
        Call.STATE_HOLDING -> getText(R.string.status_call_holding)
        else -> getText(R.string.status_call_disconnected)
    }

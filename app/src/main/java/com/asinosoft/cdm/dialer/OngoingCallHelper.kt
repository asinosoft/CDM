package com.asinosoft.cdm.dialer

import android.content.Context
import android.telecom.Call
import com.asinosoft.cdm.R
import java.util.*

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
        Call.STATE_ACTIVE -> getText(R.string.status_call_active)
        Call.STATE_CONNECTING -> getText(R.string.status_call_connecting)
        Call.STATE_DIALING -> getText(R.string.status_call_dialing)
        Call.STATE_DISCONNECTED -> getText(R.string.status_call_disconnected)
        Call.STATE_DISCONNECTING -> getText(R.string.status_call_disconnecting)
        Call.STATE_HOLDING -> getText(R.string.status_call_holding)
        Call.STATE_NEW -> getText(R.string.status_call_new)
        Call.STATE_PULLING_CALL -> getText(R.string.status_call_pulling_call)
        Call.STATE_RINGING -> getText(R.string.state_call_ringing)
        Call.STATE_SELECT_PHONE_ACCOUNT -> getText(R.string.status_call_select_phone_account)
        Call.STATE_SIMULATED_RINGING -> getText(R.string.status_call_ringing)
        else -> "# $callState"
    }

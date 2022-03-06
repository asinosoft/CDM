package com.asinosoft.cdm.helpers

import android.os.Build
import android.telecom.Call
import android.telecom.VideoProfile

val Call.id: Int
    get() = details.handle.hashCode()

val Call.phone: String
    get() = details.handle.schemeSpecificPart

val Call.callState: Int
    get() = if (Build.VERSION.SDK_INT >= 31) {
        details.state
    } else {
        state
    }

fun Call.accept() = answer(VideoProfile.STATE_AUDIO_ONLY)

fun Call.reject() =
    if (Call.STATE_RINGING == callState) {
        reject(false, null)
    } else {
        disconnect()
    }

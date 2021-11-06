package com.asinosoft.cdm.helpers

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.telephony.TelephonyManager

inline val Context.audioManager: AudioManager
    get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager

inline val Context.telecomManager: TelecomManager
    get() = getSystemService(Context.TELECOM_SERVICE) as TelecomManager

inline val Context.telephonyManager: TelephonyManager
    get() = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

inline val Context.vibrator: android.os.Vibrator
    get() = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator

fun Context.runOnUiThread(f: Context.() -> Unit) {
    if (Looper.getMainLooper() === Looper.myLooper()) f() else ContextHelper.handler.post { f() }
}

private object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
}

fun Context.hasNavBar(): Boolean {
    val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && resources.getBoolean(id)
}

fun Context.navBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

package com.asinosoft.cdm.helpers

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.Vibrator
import android.telecom.TelecomManager
import android.telephony.TelephonyManager

inline val Context.audioManager: AudioManager
    get() = getSystemService(AudioManager::class.java)

inline val Context.keyguardManager: KeyguardManager
    get() = getSystemService(KeyguardManager::class.java)

inline val Context.notificationManager: NotificationManager
    get() = getSystemService(NotificationManager::class.java) as NotificationManager

inline val Context.powerManager: PowerManager
    get() = getSystemService(PowerManager::class.java)

inline val Context.telecomManager: TelecomManager
    get() = getSystemService(TelecomManager::class.java)

inline val Context.telephonyManager: TelephonyManager
    get() = getSystemService(TelephonyManager::class.java)

inline val Context.vibrator: Vibrator
    get() = getSystemService(Vibrator::class.java)

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
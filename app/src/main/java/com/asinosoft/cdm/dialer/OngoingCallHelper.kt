package com.asinosoft.cdm.dialer

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.telecom.Call
import timber.log.Timber

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

private const val PATH = "com.asinosoft.cdm.dialer.action."
const val ACCEPT_CALL = PATH + "accept_call"
const val DECLINE_CALL = PATH + "decline_call"

fun Int.asString(): String = when (this) {
    Call.STATE_NEW -> "Новый"
    Call.STATE_RINGING -> "Входящий вызов"
    Call.STATE_DIALING -> "Соединение"
    Call.STATE_ACTIVE -> "Текущий звонок"
    Call.STATE_HOLDING -> "Удержание"
    Call.STATE_DISCONNECTED -> "Звонок завершен"
    Call.STATE_CONNECTING -> "Соединение"
    Call.STATE_DISCONNECTING -> "Отключение"
    Call.STATE_SELECT_PHONE_ACCOUNT -> "SELECT_PHONE_ACCOUNT"
    else -> {
        Timber.w("Unknown state ${this}")
        "UNKNOWN"
    }
}

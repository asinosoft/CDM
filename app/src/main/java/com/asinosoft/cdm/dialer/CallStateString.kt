package com.asinosoft.cdm.dialer

import android.telecom.Call
import timber.log.Timber

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
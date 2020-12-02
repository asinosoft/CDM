package com.asinosoft.cdm.dialer

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.view.View
import androidx.core.content.ContextCompat
import org.jetbrains.anko.telecomManager
import timber.log.Timber
import java.util.*

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun Cursor.getStringValue(key: String) = getString(getColumnIndex(key))

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




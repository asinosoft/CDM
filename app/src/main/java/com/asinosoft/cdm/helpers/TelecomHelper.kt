package com.asinosoft.cdm.helpers

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager.SIM_STATE_READY
import androidx.core.content.ContextCompat
import com.asinosoft.cdm.data.SimSlot
import timber.log.Timber
import java.io.FileNotFoundException

fun Context.isDefaultDialer(): Boolean =
    if (Build.VERSION.SDK_INT >= 29) {
        roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
    } else {
        telecomManager.defaultDialerPackage == packageName
    }

/**
 * Предлагает пользователю установить приложение дозвонщиком по-умолчанию
 */
fun Context.setDefaultDialer(): Intent {
    Timber.d("setDefaultDialer → %s", packageName)
    return if (Build.VERSION.SDK_INT >= 29) {
        roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
    } else {
        Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            .putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            )
    }
}

@SuppressLint("MissingPermission")
fun Context.getAvailableSimSlots(): List<SimSlot> {
    return telecomManager.callCapablePhoneAccounts.mapIndexed { index, account ->
        val phoneAccount = telecomManager.getPhoneAccount(account)

        SimSlot(
            index + 1,
            phoneAccount.accountHandle,
            SIM_STATE_READY,
            phoneAccount.label.toString(),
            phoneAccount.address.schemeSpecificPart
        )
    }
}

fun Context.getSimSlot(handle: PhoneAccountHandle): SimSlot? {
    return getAvailableSimSlots().find { slot -> handle == slot.handle }
}

@SuppressLint("MissingPermission")
fun Context.areMultipleSimsAvailable(): Boolean {
    return try {
        telecomManager.callCapablePhoneAccounts.size > 1
    } catch (ignored: Exception) {
        Timber.e(ignored)
        false
    }
}

fun Context.loadUriAsBitmap(uri: Uri): Bitmap? {
    return try {
        contentResolver.openAssetFileDescriptor(uri, "r")?.let {
            BitmapFactory.decodeStream(it.createInputStream())
        }
    } catch (ignored: FileNotFoundException) {
        Timber.e(ignored)
        null
    }
}

fun Context.loadResourceAsBitmap(resourceId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, resourceId)

    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

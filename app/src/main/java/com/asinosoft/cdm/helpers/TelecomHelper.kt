package com.asinosoft.cdm.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.telephony.TelephonyManager.SIM_STATE_READY
import android.util.Log
import androidx.core.content.ContextCompat
import com.asinosoft.cdm.data.SimSlot

fun Context.isDefaultDialer(): Boolean {
    Log.d("CDM|default dialer", telecomManager.defaultDialerPackage)
    return packageName == telecomManager.defaultDialerPackage
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
        false
    }
}

fun Context.loadUriAsBitmap(uri: Uri): Bitmap? {
    return contentResolver.openAssetFileDescriptor(uri, "r")?.let {
        BitmapFactory.decodeStream(it.createInputStream())
    }
}

fun Context.loadResourceAsBitmap(resourceId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, resourceId)

    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    drawable.draw(canvas)
    return bitmap
}

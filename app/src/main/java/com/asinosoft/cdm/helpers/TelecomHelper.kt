package com.asinosoft.cdm.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.telephony.TelephonyManager.SIM_STATE_READY
import android.util.Log
import com.asinosoft.cdm.data.SimSlot
import org.jetbrains.anko.telecomManager

fun Context.isDefaultDialer(): Boolean {
    Log.d("TelecomManager::isDefaultDialer", telecomManager.defaultDialerPackage)
    return packageName == telecomManager.defaultDialerPackage
}

@SuppressLint("MissingPermission")
fun Context.getAvailableSimSlots(): List<SimSlot> {
    return telecomManager.callCapablePhoneAccounts.mapIndexed { index, account ->
        val phoneAccount = telecomManager.getPhoneAccount(account)
        var label = phoneAccount.label.toString()
        var address = phoneAccount.address.toString()
        if (address.startsWith("tel:") && address.substringAfter("tel:").isNotEmpty()) {
            address = Uri.decode(address.substringAfter("tel:"))
            label += " ($address)"
        }

        SimSlot(
            index + 1,
            phoneAccount.accountHandle,
            SIM_STATE_READY,
            label,
            address.substringAfter("tel:")
        )
    }
}

@SuppressLint("MissingPermission")
fun Context.areMultipleSimsAvailable(): Boolean {
    return try {
        telecomManager.callCapablePhoneAccounts.size > 1
    } catch (ignored: Exception) {
        false
    }
}

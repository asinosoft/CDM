package com.asinosoft.cdm.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import com.asinosoft.cdm.detail_contact.StHelper
import timber.log.Timber

class PhoneItem(
    val number: String,
    val phoneType: Int = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
) : ContactItem(Type.PHONE, number) {
    val prettyNumber by lazy { StHelper.convertNumber(number) ?: number }

    fun call(context: Context) {
        Timber.i("callPhone: %s", number)
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        context.startActivity(intent)
    }

    fun sms(context: Context) {
        Timber.i("sendMsg: %s", number)
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))
        context.startActivity(intent)
    }
}

package com.asinosoft.cdm.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

class EmailItem(
    val emailType: Int,
    val email: String
) : ContactItem(Type.EMAIL, email) {

    fun send(context: Context) {
        Timber.i("sendEmail: %s", email)
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
        context.startActivity(intent)
    }
}

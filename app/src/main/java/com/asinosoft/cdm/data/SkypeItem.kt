package com.asinosoft.cdm.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

class SkypeItem(
    private val login: String
) : ContactItem(Type.SKYPE, login) {

    fun call(context: Context) {
        Timber.i("skypeCall: %s", login)
        val intent = Intent("android.intent.action.VIEW", Uri.parse("skype:$login"))
        context.startActivity(intent)
    }

    fun chat(context: Context) {
        Timber.i("skypeCall: %s", login)
        val intent = Intent("android.intent.action.VIEW", Uri.parse("skype:$login?chat"))
        context.startActivity(intent)
    }
}

package com.asinosoft.cdm.data

import android.content.Context
import android.content.Intent
import android.net.Uri

class ViberItem(
    number: String,
    private val id: String
) : ContactItem(Type.VIBER, number) {

    fun chat(context: Context) {
        val intent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message"
            )
            .setPackage("com.viber.voip")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun call(context: Context) {
        val intent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .setDataAndType(
                Uri.parse("content://com.android.contacts/data/$id"),
                "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call"
            )
            .setPackage("com.viber.voip")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

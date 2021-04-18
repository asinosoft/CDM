package com.asinosoft.cdm.data

import android.content.Context
import android.content.Intent
import android.net.Uri

class WhatsAppItem(number: String) : ContactItem(Type.WHATSAPP, number) {
    var chatId: String? = null
    var audioId: String? = null
    var videoId: String? = null

    fun chat(context: Context) {
        val intent = Intent().setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://com.android.contacts/data/$chatId"),
            "vnd.android.cursor.item/vnd.com.whatsapp.profile"
        )
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    }

    fun audio(context: Context) {
        val intent = Intent().setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://com.android.contacts/data/$audioId"),
            "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
        )
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    }

    fun video(context: Context) {
        val intent = Intent().setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://com.android.contacts/data/$videoId"),
            "vnd.android.cursor.item/vnd.com.whatsapp.video.call"
        )
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    }
}

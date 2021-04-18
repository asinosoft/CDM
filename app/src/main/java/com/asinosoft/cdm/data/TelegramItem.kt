package com.asinosoft.cdm.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

class TelegramItem(
    value: String,
    val chatId: String
) : ContactItem(Type.TELEGRAM, value) {

    fun chat(context: Context) {
        Timber.i("openTelegramNow: %s", chatId)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://com.android.contacts/data/$chatId"),
            "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

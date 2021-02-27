package com.asinosoft.cdm.api

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import java.io.IOException
import java.util.*

/**
 * Класс потокового парсера истории.
 */
object CursorApi {
    // Список колонок, получаемых из истории звонков
    private val projection = arrayOf(
        CallLog.Calls.CACHED_NAME,
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION,
        CallLog.Calls.CACHED_PHOTO_ID
    )

    /**
     * Получение истории последних звонков
     */
    fun getHistoryListLatest(context: Context) = context.contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        projection,
        null,
        null,
        "${CallLog.Calls.DATE} DESC"
    )?.let {
        // По каждому контакту показываем только последний звонок
        HistoryItemCursorAdapter(context, it).getAll().distinctBy { c -> c.contactID }
    }

    /**
     * Получение истории звонков по конкретноку контакту
     */
    fun getContactCallLog(context: Context, phone: String) = context.contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        projection,
        "${CallLog.Calls.NUMBER} = ?",
        arrayOf(phone),
        "${CallLog.Calls.DATE} DESC"
    )?.let {
        HistoryItemCursorAdapter(context, it).getAll()
    }

    fun getDisplayPhoto(contactId: Long, context: Context): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val displayPhotoUri = Uri.withAppendedPath(
            contactUri,
            ContactsContract.Contacts.Photo.DISPLAY_PHOTO
        )
        try {
            val fd = context.contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
            return BitmapFactory.decodeStream(fd!!.createInputStream())
        } catch (e: IOException) {
            return null
        }

    }
}

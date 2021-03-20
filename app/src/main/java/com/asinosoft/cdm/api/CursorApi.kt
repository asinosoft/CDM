package com.asinosoft.cdm.api

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import com.asinosoft.cdm.HistoryItem
import java.io.IOException

/**
 * Класс потокового парсера истории.
 */
object CursorApi {
    // Список колонок, получаемых из истории звонков
    private val projection = arrayOf(
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION
    )

    // База контактов для поиска по номеру телефона
    private val contactRepository = ContactRepository()

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
        // По каждому контакту показываем только последний звонок (первый, с учетом сортировки DESC)
        HistoryItemCursorAdapter(it, contactRepository).getUnique { item ->
            when (item.contact.id) {
                0L -> item.numberContact
                else -> item.contact.id.toString()
            }
        }
    }

    /**
     * Получение истории звонков по конкретноку контакту
     */
    fun getContactCallLog(context: Context, phone: String): List<HistoryItem> {
        return context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            "${CallLog.Calls.NUMBER} = ?",
            arrayOf(phone),
            "${CallLog.Calls.DATE} DESC"
        )?.let {
            HistoryItemCursorAdapter(it, contactRepository).getAll()
        } ?: ArrayList()
    }

    fun getDisplayPhoto(contactId: Long, context: Context): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val displayPhotoUri = Uri.withAppendedPath(
            contactUri,
            ContactsContract.Contacts.Photo.DISPLAY_PHOTO
        )
        return try {
            val fd = context.contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
            BitmapFactory.decodeStream(fd!!.createInputStream())
        } catch (e: IOException) {
            null
        }

    }
}

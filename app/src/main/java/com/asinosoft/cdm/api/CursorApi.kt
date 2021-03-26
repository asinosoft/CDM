package com.asinosoft.cdm.api

import android.content.Context
import android.provider.CallLog
import com.asinosoft.cdm.HistoryItem

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
    fun getHistoryListLatest(context: Context): List<HistoryItem> = context.contentResolver.query(
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
    } ?: ArrayList()

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
}

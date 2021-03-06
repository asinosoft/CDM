package com.asinosoft.cdm.api

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import androidx.core.database.getStringOrNull
import com.asinosoft.cdm.Funcs
import com.asinosoft.cdm.HistoryItem
import java.util.*
import kotlin.collections.HashSet

/**
 * Адаптер для преобразования записей истории звонков в HistoryItem
 */
class HistoryItemCursorAdapter(
    val context: Context,
    val cursor: Cursor
) {
    private val dateFormat = java.text.SimpleDateFormat("dd.MM", Locale.getDefault())
    private val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

    private val colNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER)
    private val colType = cursor.getColumnIndex(CallLog.Calls.TYPE)
    private val colDate = cursor.getColumnIndex(CallLog.Calls.DATE)
    private val colName = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
    private val colDuration = cursor.getColumnIndex(CallLog.Calls.DURATION)

    // Соответствие контактов и номеров телефононов
    private val phoneNumberToContactId = HashMap<String, String>()

    fun getAll(): ArrayList<HistoryItem> {
        val result = ArrayList<HistoryItem>()
        while (cursor.moveToNext()) {
            result.add(getOne())
        }
        return result
    }

    fun <T> getUnique(distinctBy: (HistoryItem) -> T): ArrayList<HistoryItem> {
        val set = HashSet<T>()
        val result = ArrayList<HistoryItem>()
        while (cursor.moveToNext()) {
            val item = getOne()
            if (set.add(distinctBy(item))) {
                result.add(getOne())
            }
        }
        return result
    }

    private fun getOne(): HistoryItem {
        val phoneNumber = cursor.getString(colNumber)
        val name = cursor.getStringOrNull(colName)
        val date = cursor.getLong(colDate)
        val contactID = getContactId(phoneNumber)

        return HistoryItem(
            numberContact = phoneNumber,
            typeCall = cursor.getInt(colType),
            time = timeFormat.format(date),
            nameContact = when {
                name.isNullOrEmpty() -> phoneNumber
                else -> name
            },
            contactID = contactID,
            duration = cursor.getString(colDuration),
            date = dateFormat.format(date)
        )
    }

    private fun getContactId(phoneNumber: String) =
        phoneNumberToContactId.getOrPut(phoneNumber, { Funcs.getContactID(context, phoneNumber) ?: "" })
}

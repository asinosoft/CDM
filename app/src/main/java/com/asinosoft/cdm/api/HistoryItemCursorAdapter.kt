package com.asinosoft.cdm.api

import android.database.Cursor
import android.provider.CallLog
import com.asinosoft.cdm.HistoryItem
import com.asinosoft.cdm.detail_contact.Contact
import com.asinosoft.cdm.detail_contact.StHelper
import java.util.*
import kotlin.collections.HashSet

/**
 * Адаптер для преобразования записей истории звонков в HistoryItem
 */
class HistoryItemCursorAdapter(
    private val cursor: Cursor,
    private val contactRepository: ContactRepository
) {
    private val dateFormat = java.text.SimpleDateFormat("dd.MM", Locale.getDefault())
    private val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

    private val colNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER)
    private val colType = cursor.getColumnIndex(CallLog.Calls.TYPE)
    private val colDate = cursor.getColumnIndex(CallLog.Calls.DATE)
    private val colDuration = cursor.getColumnIndex(CallLog.Calls.DURATION)

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
        val date = cursor.getLong(colDate)

        return HistoryItem(
            numberContact = phoneNumber,
            typeCall = cursor.getInt(colType),
            time = timeFormat.format(date),
            duration = cursor.getString(colDuration),
            date = dateFormat.format(date),
            contact = contactRepository.contactPhones[phoneNumber] ?: Contact(0, phoneNumber)
        )
    }
}

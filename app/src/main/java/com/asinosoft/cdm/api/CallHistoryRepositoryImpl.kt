package com.asinosoft.cdm.api

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.helpers.StHelper
import java.util.*
import kotlin.collections.ArrayList

/**
 * Доступ к истории звонков
 */
class CallHistoryRepositoryImpl(private val contactRepository: ContactRepository) :
    CallHistoryRepository {
    // Список колонок, получаемых из истории звонков
    private val projection = arrayOf(
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION
    )

    override fun getLatestHistory(
        context: Context,
        before: Date,
        limit: Int,
        filter: CallHistoryFilter
    ): List<CallHistoryItem> {
        return context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            "${CallLog.Calls.DATE} < ?",
            arrayOf(before.time.toString()),
            "${CallLog.Calls.DATE} DESC"
        )?.use {
            // По каждому контакту показываем только последний звонок (первый, с учетом сортировки DESC)
            HistoryItemCursorAdapter(it).getFiltered(limit, filter)
        } ?: ArrayList()
    }

    override fun getNewestHistory(context: Context, after: Date): List<CallHistoryItem> {
        return context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            "${CallLog.Calls.DATE} > ?",
            arrayOf(after.time.toString()),
            "${CallLog.Calls.DATE} DESC"
        )?.use {
            // По каждому контакту показываем только последний звонок (первый, с учетом сортировки DESC)
            HistoryItemCursorAdapter(it).getFiltered(Int.MAX_VALUE, CallHistoryFilter())
        } ?: ArrayList()
    }

    override fun getHistoryByContact(context: Context, contact: Contact): List<CallHistoryItem> {
        if (contact.phones.isEmpty()) {
            return listOf()
        }

        // Делаем выборку истории звонков по всем номерам телефонов контакта
        val selection = generateSequence { "${CallLog.Calls.NUMBER} = ?" }.take(contact.phones.size)
            .joinToString(" OR ")
        return context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            contact.phones.map { it.value }.toTypedArray(),
            "${CallLog.Calls.DATE} DESC"
        )?.use {
            HistoryItemCursorAdapter(it).getAll()
        } ?: listOf()
    }

    override fun getHistoryByPhone(context: Context, phone: String): List<CallHistoryItem> {
        return context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            "${CallLog.Calls.NUMBER} = ?",
            arrayOf(phone),
            "${CallLog.Calls.DATE} DESC"
        )?.use {
            HistoryItemCursorAdapter(it).getAll()
        } ?: ArrayList()
    }

    inner class HistoryItemCursorAdapter(
        private val cursor: Cursor
    ) {
        private val dateFormat = java.text.SimpleDateFormat("dd.MM", Locale.getDefault())
        private val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

        private val colNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER)
        private val colType = cursor.getColumnIndex(CallLog.Calls.TYPE)
        private val colDate = cursor.getColumnIndex(CallLog.Calls.DATE)
        private val colDuration = cursor.getColumnIndex(CallLog.Calls.DURATION)

        fun getAll(): List<CallHistoryItem> {
            val result = java.util.ArrayList<CallHistoryItem>()
            while (cursor.moveToNext()) {
                result.add(getOne())
            }
            return result
        }

        fun getFiltered(limit: Int, filter: CallHistoryFilter): List<CallHistoryItem> {
            val result = java.util.ArrayList<CallHistoryItem>()
            while (cursor.moveToNext()) {
                val item = getOne()
                if (filter.filter(item)) {
                    result.add(getOne())
                }

                if (result.size >= limit) {
                    break
                }
            }
            return result
        }

        private fun getOne(): CallHistoryItem {
            val phoneNumber = cursor.getString(colNumber)
            val date = cursor.getLong(colDate)

            return CallHistoryItem(
                phone = phoneNumber,
                prettyPhone = StHelper.convertNumber(phoneNumber),
                timestamp = Date(date),
                date = dateFormat.format(date),
                time = timeFormat.format(date),
                typeCall = cursor.getInt(colType),
                duration = cursor.getLong(colDuration),
                contact = contactRepository.getContactByPhone(phoneNumber)
                    ?: Contact(0, phoneNumber).apply {
                        actions.add(Action(0, Action.Type.PhoneCall, phoneNumber, ""))
                    }
            )
        }
    }
}

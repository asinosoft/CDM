package com.asinosoft.cdm.api

import android.content.ContentResolver
import android.database.Cursor
import android.provider.CallLog
import com.asinosoft.cdm.App
import com.asinosoft.cdm.detail_contact.Contact
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Класс потокового парсера истории.
 */
class CallHistoryRepositoryImpl(private val contentResolver: ContentResolver) : CallHistoryRepository {
    // Список колонок, получаемых из истории звонков
    private val projection = arrayOf(
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION
    )

    override fun getLatestHistory(): List<CallHistoryItem> {
        return contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )?.let {
            // По каждому контакту показываем только последний звонок (первый, с учетом сортировки DESC)
            HistoryItemCursorAdapter(it).getUnique { item ->
                when (item.contact.id) {
                    0L -> item.phone
                    else -> item.contact.id.toString()
                }
            }
        } ?: ArrayList()
    }

    override fun getHistoryByContactId(contactId: Long): List<CallHistoryItem> {
        return App.contactRepository.getContactById(contactId)?.let {
            if (it.mPhoneNumbers.isEmpty()) {
                return listOf()
            }

            val selection = generateSequence { "${CallLog.Calls.NUMBER} = ?" }.take(it.mPhoneNumbers.size).joinToString(" OR ")
            return contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                convertNumbersToInternationalFormat(it.mPhoneNumbers).toTypedArray(),
                "${CallLog.Calls.DATE} DESC"
            )?.let {
                HistoryItemCursorAdapter(it).getAll()
            } ?: listOf()
        } ?: listOf()
    }

    override fun getHistoryByPhone(phone: String): List<CallHistoryItem> {
        return contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            "${CallLog.Calls.NUMBER} = ?",
            arrayOf(phone),
            "${CallLog.Calls.DATE} DESC"
        )?.let {
            HistoryItemCursorAdapter(it).getAll()
        } ?: ArrayList()
    }

    /**
     * Преобразование номеров телефонов в формат, используемый в истории звонков
     */
    private fun convertNumbersToInternationalFormat(phoneNumbers: List<String>): List<String> {
        return phoneNumbers.map {
            PhoneNumberUtil.getInstance().format(
                PhoneNumberUtil.getInstance().parse(it, "RU"),
                PhoneNumberUtil.PhoneNumberFormat.E164
            )
        }
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

        fun getAll(): java.util.ArrayList<CallHistoryItem> {
            val result = java.util.ArrayList<CallHistoryItem>()
            while (cursor.moveToNext()) {
                result.add(getOne())
            }
            return result
        }

        fun <T> getUnique(distinctBy: (CallHistoryItem) -> T): java.util.ArrayList<CallHistoryItem> {
            val set = HashSet<T>()
            val result = java.util.ArrayList<CallHistoryItem>()
            while (cursor.moveToNext()) {
                val item = getOne()
                if (set.add(distinctBy(item))) {
                    result.add(getOne())
                }
            }
            return result
        }

        private fun getOne(): CallHistoryItem {
            val phoneNumber = cursor.getString(colNumber)
            val date = cursor.getLong(colDate)

            return CallHistoryItem(
                phone = phoneNumber,
                date = dateFormat.format(date),
                time = timeFormat.format(date),
                typeCall = cursor.getInt(colType),
                duration = cursor.getLong(colDuration),
                contact = App.contactRepository.getContactByPhone(phoneNumber) ?: Contact(
                    0,
                    phoneNumber
                )
            )
        }
    }
}

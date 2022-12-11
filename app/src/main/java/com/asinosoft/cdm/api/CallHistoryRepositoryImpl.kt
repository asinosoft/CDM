package com.asinosoft.cdm.api

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.helpers.DateHelper
import com.asinosoft.cdm.helpers.StHelper
import com.asinosoft.cdm.helpers.telecomManager
import java.util.*

/**
 * Доступ к истории звонков
 */
class CallHistoryRepositoryImpl(private val contactRepository: ContactRepository) :
    CallHistoryRepository {
    // Список колонок, получаемых из истории звонков
    private val projection = arrayOf(
        CallLog.Calls._ID,
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION,
        CallLog.Calls.COUNTRY_ISO,
        CallLog.Calls.PHONE_ACCOUNT_ID,
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
            HistoryItemCursorAdapter(context, it).getFiltered(limit, filter)
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
            HistoryItemCursorAdapter(context, it).getFiltered(Int.MAX_VALUE, CallHistoryFilter())
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
            HistoryItemCursorAdapter(context, it).getAll()
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
            HistoryItemCursorAdapter(context, it).getAll()
        } ?: ArrayList()
    }

    override fun purgeCallHistory(context: Context) {
        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, "true", null)
    }

    override fun purgeContactHistory(context: Context, contact: Contact) {
        val phones = contact.phones.map { it.value }.toTypedArray()
        if (phones.isEmpty()) {
            return
        }

        val placeholders = "?".repeat(phones.size)

        context.contentResolver.delete(
            CallLog.Calls.CONTENT_URI,
            "_id IN (SELECT _id FROM calls WHERE " + CallLog.Calls.NUMBER + " IN (" + placeholders + "))",
            phones
        )
    }

    override fun deleteCallHistoryItem(context: Context, call: CallHistoryItem) {
        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, "_id = ?", arrayOf(call.id.toString()))
    }

    @SuppressLint("MissingPermission")
    inner class HistoryItemCursorAdapter(
        context: Context,
        private val cursor: Cursor
    ) {
        private val colId = cursor.getColumnIndex(CallLog.Calls._ID)
        private val colNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER)
        private val colType = cursor.getColumnIndex(CallLog.Calls.TYPE)
        private val colDate = cursor.getColumnIndex(CallLog.Calls.DATE)
        private val colDuration = cursor.getColumnIndex(CallLog.Calls.DURATION)
        private val colCountry = cursor.getColumnIndex(CallLog.Calls.COUNTRY_ISO)
        private val colPhoneAccount = cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)

        private val accounts: List<String> by lazy {
            if (PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
                context.telecomManager.callCapablePhoneAccounts.map { it.id }
            } else {
                emptyList()
            }
        }

        fun getAll(): List<CallHistoryItem> {
            val result = ArrayList<CallHistoryItem>()
            while (cursor.moveToNext()) {
                result.add(getOne())
            }
            return result
        }

        fun getFiltered(limit: Int, filter: CallHistoryFilter): List<CallHistoryItem> {
            val result = ArrayList<CallHistoryItem>()
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
            val date = Date(cursor.getLong(colDate))
            val country = cursor.getString(colCountry)
            val phoneAccount = cursor.getString(colPhoneAccount)

            return CallHistoryItem(
                id = cursor.getLong(colId),
                phone = phoneNumber,
                prettyPhone = StHelper.convertNumber(phoneNumber, country),
                timestamp = date,
                date = DateHelper.shortDate(date),
                time = DateHelper.time(date),
                typeCall = cursor.getInt(colType),
                duration = cursor.getLong(colDuration),
                contact = contactRepository.getContactByPhone(phoneNumber)
                    ?: Contact(0, phoneNumber).apply {
                        actions.add(Action(0, Action.Type.PhoneCall, phoneNumber, ""))
                    },
                sim = 1 + accounts.indexOf(phoneAccount)
            )
        }
    }
}

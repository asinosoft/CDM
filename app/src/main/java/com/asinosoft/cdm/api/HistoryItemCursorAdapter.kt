package com.asinosoft.cdm.api

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import com.asinosoft.cdm.Funcs
import com.asinosoft.cdm.HistoryItem
import java.util.*

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
    private var colPhoto = cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_ID)

    fun getAll() : ArrayList<HistoryItem> {
        val result = ArrayList<HistoryItem>()
        while(cursor.moveToNext()) {
            result.add(getOne())
        }
        return result
    }

    private fun getOne() = HistoryItem(
        numberContact = cursor.getString(colNumber),
        typeCall = cursor.getInt(colType),
        time = timeFormat.format(cursor.getLong(colDate)),
        nameContact = cursor.getString(colName),
        contactID = Funcs.getContactID(context, cursor.getString(colNumber)) ?: "",
        duration = cursor.getString(colDuration),
        _PhotoID = cursor.getIntOrNull(colPhoto),
        date = dateFormat.format(cursor.getLong(colDate))
    )

}
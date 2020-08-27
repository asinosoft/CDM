package com.asinosoft.cdm.api

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.graphics.drawable.toDrawable
import com.asinosoft.cdm.Funcs
import com.asinosoft.cdm.HistoryItem
import com.asinosoft.cdm.Metoths.Companion.containsNumber
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Класс потокового парсера истории.
 */
class CursorApi {

    companion object {

        @SuppressLint("MissingPermission")
        suspend fun getHistoryListLatest(
            context: Context,
            count: Int = -1,
            onNext: (HistoryItem) -> Unit = {},
            numFilter: String? = null,
            nextForce: Boolean = false,
            numUnique: Boolean = false
        ) =
            withContext(Dispatchers.Default) {
                val list = ArrayList<HistoryItem>()
                val proj = arrayOf(
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls._ID,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.CACHED_PHOTO_ID
                )
                context.contentResolver.query(CallLog.Calls.CONTENT_URI, proj, null, null, null)
                    ?.let { cursor ->
                        val number = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                        val type = cursor.getColumnIndex(CallLog.Calls.TYPE)
                        val date = cursor.getColumnIndex(CallLog.Calls.DATE)
                        val name = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                        val id = cursor.getColumnIndex(CallLog.Calls._ID)
                        val dur = cursor.getColumnIndex(CallLog.Calls.DURATION)
                        var photo = cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_ID)
                        var i = count
                        val b = getSortedCursor(cursor)
                        if (b) {
                            cursor.moveToLast()
                            cursor.moveToNext()
                        } else {
                            cursor.moveToFirst()
                            cursor.moveToPrevious()
                        }

                        while ((if (b) {
                                cursor.moveToPrevious() && !cursor.isBeforeFirst
                            } else {
                                cursor.moveToNext() && !cursor.isAfterLast
                            }) && if (i == -1) true else --i >= 0
                        ) {
                            var num = cursor.getString(number)
                            if (num == "" || (num != numFilter && numFilter != null) || (numUnique && list.containsNumber(num))) continue

                            var callDayTime: Long = cursor.getLong(date)
                            var date = Date(callDayTime)
                            var sdf =
                                java.text.SimpleDateFormat(
                                    "HH:mm",
                                    Locale.getDefault(Locale.Category.DISPLAY)
                                )
                            var formattedDate = sdf.format(date)
                            sdf =
                                java.text.SimpleDateFormat(
                                    "dd.MM",
                                    Locale.getDefault(Locale.Category.DISPLAY)
                                )
                            var historyItem = HistoryItem(
                                numberContact = num,
                                typeCall = cursor.getInt(type),
                                time = formattedDate,
//                                image = ContextCompat.getDrawable(context, R.drawable.contact_unfoto)!!,
                                nameContact = cursor.getString(name) ?: num,
                                contactID = Funcs.getContactID(context, num) ?: "",
                                _ID = cursor.getLongOrNull(id),
                                duration = cursor.getString(dur),
                                _PhotoID = cursor.getIntOrNull(photo),
                                //date =  sdf.format(date)
                                date = if (sdf.format(date) == sdf.format(Calendar.getInstance().time)) "Сегодня" else sdf.format(
                                    date
                                )
                            )
                                onNext(historyItem)
//            Log.d("History: ", "$i = ${historyCell.nameContact} / ${historyCell.date}")
                            list.add(historyItem)
                        }
                    }
                return@withContext list//if(b) list.reversed() as ArrayList<HistoryCell> else list
            }



        suspend fun getCallLogs(
            context: Context,
            count: Int = -1
        ) =
            withContext(Dispatchers.IO) {
                val list = ArrayList<Deferred<HistoryItem>>()
                val proj = arrayOf(
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls._ID,
                    CallLog.Calls.DURATION
                )
                context.contentResolver.query(CallLog.Calls.CONTENT_URI, proj, null, null, null)
                    ?.let { cursor ->
                        val number = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                        val type = cursor.getColumnIndex(CallLog.Calls.TYPE)
                        val date = cursor.getColumnIndex(CallLog.Calls.DATE)
                        val name = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                        val id = cursor.getColumnIndex(CallLog.Calls._ID)
                        val dur = cursor.getColumnIndex(CallLog.Calls.DURATION)
                        var i = count
                        val b = getSortedCursor(cursor)
                        if (b) {
                            cursor.moveToLast()
                            cursor.moveToNext()
                        } else {
                            cursor.moveToFirst()
                            cursor.moveToPrevious()
                        }

                        while ((if (b) {
                                cursor.moveToPrevious() && !cursor.isBeforeFirst
                            } else {
                                cursor.moveToNext() && !cursor.isAfterLast
                            }) && if (i == -1) true else --i >= 0
                        ) {
                            var num = cursor.getString(number)
                            if (num == "") continue
                            list.add(getDifferCursor(cursor,
                                context,
                                number,
                                type,
                                date,
                                name,
                                id,
                                dur))
                        }
                    }
                return@withContext list
            }

        private suspend fun getDifferCursor(
            cursor: Cursor,
            context: Context,
            number: Int,
            type: Int,
            date: Int,
            name: Int,
            id: Int,
            dur: Int
        ): Deferred<HistoryItem> {
            return coroutineScope {
                return@coroutineScope async {
                    var num = cursor.getString(number)
                    var callDayTime: Long = cursor.getLong(date)
                    var date = Date(callDayTime)
                    var sdf =
                        java.text.SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault(Locale.Category.DISPLAY)
                        )
                    var formattedDate = sdf.format(date)
                    sdf =
                        java.text.SimpleDateFormat(
                            "dd.MM",
                            Locale.getDefault(Locale.Category.DISPLAY)
                        )
                    return@async HistoryItem(
                        numberContact = num,
                        typeCall = cursor.getInt(type),
                        time = formattedDate,
//                        image = context.getDrawable(R.drawable.contact_unfoto)!!,
                        nameContact = cursor.getString(name) ?: num,
                        contactID = Funcs.getContactID(context, num) ?: "",
                        duration = cursor.getString(dur),
                        //date =  sdf.format(date)
                        date = if (sdf.format(date) == sdf.format(Calendar.getInstance().time)) "Сегодня" else sdf.format(
                            date
                        )
                    )
                }
            }
        }

        private fun getSortedCursor(cursor: Cursor): Boolean {
            val list = ArrayList<Long>()
            with(cursor) {
                moveToFirst()
                while (moveToNext() && list.count() < 2) {
                    val temp = getLong(getColumnIndex(CallLog.Calls.DATE))
                    if (temp < 0) continue
                    list.add(getLong(getColumnIndex(CallLog.Calls.DATE)))
                }
                moveToFirst()
            }
            return if (list.count() > 1)
             (list[0] < list[1])
            else true
        }

        fun getPhotoFromID(id: Int, context: Context): Bitmap? {
            val c: Cursor? = context.contentResolver
                .query(ContactsContract.Data.CONTENT_URI, arrayOf(
                    ContactsContract.CommonDataKinds.Photo.PHOTO
                ), ContactsContract.Data._ID + "=?", arrayOf(
                    id.toString()
                ), null)
            var imageBytes: ByteArray? = null
            if (c != null) {
                if (c.moveToFirst()) {
                    imageBytes = c.getBlob(0)
                }
                c.close()
            }
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes!!.size)
        }

        fun getDisplayPhoto(contactId: Long, context: Context): Bitmap? {
            val contactUri =
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val displayPhotoUri = Uri.withAppendedPath(contactUri,
                ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
            try {
                val fd = context.contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
                return BitmapFactory.decodeStream(fd!!.createInputStream())
            } catch (e: IOException) {
                return null
            }

        }
    }
}

private fun java.util.ArrayList<HistoryItem>.containsWith(historyItem: HistoryItem): Boolean {
    forEach {
        if (it.nameContact == historyItem.nameContact && it.numberContact == historyItem.numberContact) return true
    }
    return false
}

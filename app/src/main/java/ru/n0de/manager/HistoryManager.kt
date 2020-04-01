package ru.n0de.manager

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

class HistoryManager(activity: Activity) {

    var activity = activity
    val list = ArrayList<HistoryCell>()

    private fun getHistoryListLatest(Count: Int): ArrayList<HistoryCell> {
        val managedCursor = activity.managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null)
        val number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
        val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
        val date = managedCursor.getColumnIndex(CallLog.Calls.DATE)
        val name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val id = managedCursor.getColumnIndex(CallLog.Calls._ID)
        val dur = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
        var i = Count
        val b = getSortedCursor(managedCursor)
        if (b) {
            managedCursor.moveToLast()
            managedCursor.moveToNext()
        }else {
            managedCursor.moveToFirst()
            managedCursor.moveToPrevious()
        }

        while ((if (b){ managedCursor.moveToPrevious() && !managedCursor.isBeforeFirst} else {managedCursor.moveToNext() && !managedCursor.isAfterLast}) && --i >= 0) {
            var num = managedCursor.getString(number)
            if (num == "") continue
            //val cir = getCirWithNum(num)
            var callDayTime = managedCursor.getLong(date)
            var date = Date(callDayTime)
            var sdf =
                java.text.SimpleDateFormat("HH:mm", Locale.getDefault(Locale.Category.DISPLAY))
            var formattedDate = sdf.format(date)
            sdf = java.text.SimpleDateFormat("dd.MM", Locale.getDefault(Locale.Category.DISPLAY))
            var historyCell = HistoryCell(
                number = num,
                type = managedCursor.getInt(type),
                time = formattedDate,
                image = activity.getDrawable(R.drawable.contact_unfoto)!!,
                name = managedCursor.getString(name) ?: num,
                ContactID = Funcs.getContactID(activity, num) ?: "",
                duration = managedCursor.getString(dur),
                //date =  sdf.format(date)
                date = if (sdf.format(date) == sdf.format(Calendar.getInstance().time)) "Сегодня" else sdf.format(
                    date
                )
            )
            Log.d("History: ", "$i = ${historyCell.nameContact} / ${historyCell.date}")
            list.add(historyCell)
        }
        return list//if(b) list.reversed() as ArrayList<HistoryCell> else list
    }

    private fun getSortedCursor(managedCursor: Cursor): Boolean {
        val list = ArrayList<Long>()
        with(managedCursor) {
            moveToFirst()
            while (moveToNext() && list.count() < 2) {
                val temp = getLong(getColumnIndex(CallLog.Calls.DATE))
                if(temp < 0) continue
                list.add(getLong(getColumnIndex(CallLog.Calls.DATE)))
            }
            moveToFirst()
        }
        return (list[0] < list[1])
    }

}
package com.asinosoft.cdm

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.FragmentActivity
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class DetailHistory : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_history_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HistoryListFrame())
                .commitNow()
//            window.statusBarColor = Color.BLACK
//            window.navigationBarColor = Color.BLACK
        }
    }


    private fun getHistoryListLatest(Count:Int, Number: String): ArrayList<HistoryCell> {
        val list = ArrayList<HistoryCell>()
        val managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null)
        val number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
        val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
        val date = managedCursor.getColumnIndex(CallLog.Calls.DATE)
        val name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val id = managedCursor.getColumnIndex(CallLog.Calls._ID)
        val dur = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
        var i = if(Count == -1) managedCursor.count else Count
        managedCursor.moveToLast()
        while (managedCursor.moveToPrevious() && --i >= 0 && !managedCursor.isBeforeFirst) {
            var num = managedCursor.getString(number)
            if (num != Number) continue
            //val cir = getCirWithNum(num)
            //Log.d("getCirWithNum: ", "cir = $cir")
            var callDayTime = managedCursor.getLong(date)
            var date = Date(callDayTime)
            var sdf = java.text.SimpleDateFormat("HH:mm", Locale.getDefault(Locale.Category.DISPLAY))
            var formattedDate = sdf.format(date)
            sdf = java.text.SimpleDateFormat("dd.MM", Locale.getDefault(Locale.Category.DISPLAY))
            var historyCell = HistoryCell(
                number = num,
                type = managedCursor.getInt(type),
                time = formattedDate,
                image = getDrawable(R.drawable.contact_unfoto)!!,
                name = managedCursor.getString(name) ?: num,
                ContactID = Funcs.getContactID(this, num) ?: "",
                duration = managedCursor.getString(dur),
                //date = sdf.format(date)
                date = if (sdf.format(date) == sdf.format(Calendar.getInstance().time)) "Сегодня" else sdf.format(date)
            )
            list.add(historyCell)
        }
        return list
    }

    fun getDateNow(patern:String = "dd.MM"){

    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    override fun onStart() {
        super.onStart()
        val num = intent.getStringExtra(Keys.ListCells)
        val list = getHistoryListLatest(100, num ?: "")
        list.forEach {
            if(it.contactID != "") it.image = getPhotoSaffety(it.contactID.toLong())
        }
        (supportFragmentManager.fragments[0] as HistoryListFrame).setParamWithDetail(list)
    }

    private fun getPhotoSaffety(id: Long): Drawable {
        val photo = getPhotoNow(id)
        return if (photo != null) BitmapDrawable(photo) else getDrawable(R.drawable.contact_unfoto) as Drawable
    }

    private fun getPhotoNow(id: Long): Bitmap? = openDisplayPhoto(contactId = id)

    private fun openDisplayPhoto(contactId: Long): Bitmap? {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val displayPhotoUri = Uri.withAppendedPath(contactUri,
            ContactsContract.Contacts.Photo.DISPLAY_PHOTO
        )
        try {
            val fd = contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
            return BitmapFactory.decodeStream(fd!!.createInputStream())
        } catch (e: IOException) {
            return null
        }

    }

}

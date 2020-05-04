package com.asinosoft.cdm

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager


class HistoryDetailFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryDetailFragment()
    }

    private lateinit var viewModel: HistoryDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HistoryDetailViewModel::class.java)

        setData()
    }

    private fun setData() {
        val num = getNum(this.context!!)
        val list = getHistoryListLatest(100, num ?: "")
        list.forEach {
            if(it.contactID != "") it.image = getPhotoSaffety(it.contactID.toLong())
        }

        val recyclerView = this.view!!.findViewById<RecyclerView>(R.id.recyclerView)
        var lim = LinearLayoutManager(this.context!!)
        lim.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = lim
        recyclerView.adapter = AdapterHistory(list, null, false)
    }

    private fun getNum(context: Context): String {
        val sharedpreferences = activity!!.getSharedPreferences(Keys.SharedNum, Context.MODE_PRIVATE)
        return sharedpreferences.getString("TAG_NUM", "")!!
    }

    private fun getHistoryListLatest(Count:Int, Number: String): ArrayList<HistoryItem> {
        val list = ArrayList<HistoryItem>()
        val managedCursor = activity!!.managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null)
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
            var historyItem = HistoryItem(
                numberContact = num,
                typeCall = managedCursor.getInt(type),
                time = formattedDate,
                image = getDrawable(this.context!!, R.drawable.contact_unfoto)!!,
                nameContact = managedCursor.getString(name) ?: num,
                contactID = Funcs.getContactID(this.context!!, num) ?: "",
                duration = managedCursor.getString(dur),
                //date = sdf.format(date)
                date = if (sdf.format(date) == sdf.format(Calendar.getInstance().time)) "Сегодня" else sdf.format(date)
            )
            list.add(historyItem)
        }
        return list
    }

    private fun getPhotoSaffety(id: Long): Drawable {
        val photo = getPhotoNow(id)
        return if (photo != null) BitmapDrawable(photo) else getDrawable(this.context!!, R.drawable.contact_unfoto) as Drawable
    }

    private fun getPhotoNow(id: Long): Bitmap? = openDisplayPhoto(contactId = id)

    private fun openDisplayPhoto(contactId: Long): Bitmap? {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val displayPhotoUri = Uri.withAppendedPath(contactUri,
            ContactsContract.Contacts.Photo.DISPLAY_PHOTO
        )
        try {
            val fd = activity!!.contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
            return BitmapFactory.decodeStream(fd!!.createInputStream())
        } catch (e: IOException) {
            return null
        }

    }

}

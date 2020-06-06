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
import com.asinosoft.cdm.adapters.AdapterCallLogs
import com.asinosoft.cdm.api.CursorApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.runOnUiThread


class HistoryDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setData()
    }

    private fun setData() {
        val num = getNum(this.context!!)
        var list = ArrayList<HistoryItem>()

        val recyclerView = this.view!!.findViewById<RecyclerView>(R.id.recyclerView)
        var lim = LinearLayoutManager(this.context!!)
        lim.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = lim
        recyclerView.adapter = AdapterCallLogs(list,false, context!!)
        GlobalScope.launch(Dispatchers.IO) { (recyclerView.adapter as AdapterCallLogs).items = CursorApi.getHistoryListLatest(context!!, numFilter = getNum(context!!))
        context?.runOnUiThread {recyclerView.adapter?.notifyDataSetChanged()}}
    }

    private fun getNum(context: Context): String {
        val sharedpreferences = activity!!.getSharedPreferences(Keys.SharedNum, Context.MODE_PRIVATE)
        return sharedpreferences.getString("TAG_NUM", "")!!
    }

}

package com.asinosoft.cdm

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.adapters.AdapterCallLogs
import com.asinosoft.cdm.api.CursorApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.runOnUiThread

/**
 * Фрагмент вкладки "Истории" в детальной информации по элементу истории
 */
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
        GlobalScope.launch(Dispatchers.IO) {
            CursorApi.getContactCallLog(context!!, num)?.let {
                context?.runOnUiThread {
                    (recyclerView.adapter as AdapterCallLogs).setList(it)
                }
            }
        }
    }

    private fun getNum(context: Context): String {
        val sharedpreferences = activity!!.getSharedPreferences(Keys.SharedNum, Context.MODE_PRIVATE)
        return sharedpreferences.getString("TAG_NUM", "")!!
    }

}

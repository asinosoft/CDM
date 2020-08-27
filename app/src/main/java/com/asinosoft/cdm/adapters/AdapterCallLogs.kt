package com.asinosoft.cdm.adapters

import android.content.Context
import android.content.Intent
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.*
import com.asinosoft.cdm.api.CursorApi
import com.asinosoft.cdm.api.CursorApi.Companion.getDisplayPhoto
import com.asinosoft.cdm.api.CursorApi.Companion.getPhotoFromID
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.zerobranch.layout.SwipeLayout
import kotlinx.coroutines.*
import org.jetbrains.anko.runOnUiThread


class AdapterCallLogs(var items: ArrayList<HistoryItem>, val onClick: Boolean = true, val context: Context, var onAdd: (Int) -> Unit = {}): RecyclerView.Adapter<AdapterCallLogs.HolderHistory>() {

    private var pos = 0
    private var job: Job? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory{
        return HolderHistory(CalllogObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    fun setList(list: ArrayList<HistoryItem>){
        this.items = list
        notifyDataSetChanged()
    }

    fun addItem(item: HistoryItem){
        items.add(item)
        notifyItemInserted(itemCount - 1)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        holder.bind(items[position])
    }

    private fun openDetail(item: HistoryItem) {
        val intent = Intent(this.context, DetailHistoryActivity::class.java)
        intent.putExtra(Keys.number, item.numberContact)
        intent.putExtra(Keys.id, item.contactID)
        context?.startActivity(intent)
    }

    suspend fun addItemByCorutine(it: HistoryItem, i: Int = -1) {
        withContext(Dispatchers.IO){
            items.add(it)
            withContext(Dispatchers.Main){
                notifyItemInserted(itemCount - 1)
            }
        }
    }


    inner class HolderHistory(val v: CalllogObjectBinding) : RecyclerView.ViewHolder(v.root) {

        fun bind(item: HistoryItem) {

            with(v) {
                if (!item.contactID.isNullOrBlank())
                item.contactID.toLong()?.let {
                    if (it > 0) getDisplayPhoto(it, context!!)?.let { bitmap ->
                        imageContact.setImageBitmap(bitmap)
                    }
                }
//                imageContact.setImageDrawable(item.image)
                name.text = item.nameContact
                number.text = "${item.numberContact}, ${Metoths.getFormatedTime(item.duration)}"
                timeContact.text = item.time
                dateContact.text = item.date

                when (item.typeCall) {
                    CallLog.Calls.OUTGOING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_made_24)
                    CallLog.Calls.INCOMING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_received_24)
                    CallLog.Calls.MISSED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_missed_24)
                    CallLog.Calls.BLOCKED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_canceled_24)
                }

                swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                    override fun onOpen(direction: Int, isContinuous: Boolean) {
                        when (direction) {
                            SwipeLayout.RIGHT -> {
                                Metoths.callPhone(item.numberContact, context!!)
                            }
                            SwipeLayout.LEFT -> {
                                Metoths.openWhatsApp(item.numberContact, context!!)
                            }
                            else -> Log.e(
                                "AdapterHistory.kt: ",
                                "SwipeLayout direction UNKNOWN = $direction"
                            )
                        }
                        swipeLayout.close()
                    }

                    override fun onClose() {
                    }

                })
                if (onClick) dragLayout.setOnClickListener { openDetail(item) }

            }
        }
    }

}


package com.asinosoft.cdm.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.*
import com.asinosoft.cdm.api.CursorApi.Companion.getDisplayPhoto
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.zerobranch.layout.SwipeLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.image


class AdapterCallLogs(
    var items: ArrayList<HistoryItem>,
    val onClick: Boolean = true,
    val context: Context,
    var onAdd: (Int) -> Unit = {}
) : RecyclerView.Adapter<AdapterCallLogs.HolderHistory>() {

    private var pos = 0
    private var job: Job? = null
    private val buffer = ArrayList<HistoryItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        return HolderHistory(CalllogObjectBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    fun setList(list: ArrayList<HistoryItem>) {
        this.items = list
        notifyDataSetChanged()
    }

    fun addItem(item: HistoryItem) {
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

    suspend fun addBuffer(item: HistoryItem) {
        withContext(Dispatchers.IO) {
            buffer.add(item)
        }
    }

    fun upIntoBuffer() {
        val lastIndex = items.lastIndex
        items.addAll(buffer)
        notifyItemRangeInserted(lastIndex, items.lastIndex)
        buffer.clear()
    }

    suspend fun addItemByCorutine(it: HistoryItem, i: Int = -1) {
        withContext(Dispatchers.IO) {
            items.add(it)
            withContext(Dispatchers.Main) {
                notifyItemInserted(itemCount - 1)
            }
        }
    }


    inner class HolderHistory(val v: CalllogObjectBinding) : RecyclerView.ViewHolder(v.root) {

        val redIcon = 0..19
        val bluIcon = 20..39
        val orangIcon = 40..59
        val greenIcon = 60..79
        val filIcon = 80..99

        @SuppressLint("ResourceAsColor", "RestrictedApi")
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

                val number1 = item.numberContact

                if (number1.substring(number1.length - 2).toInt() in redIcon) {
                    imageContact.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red_icon)
                }
                if (number1.substring(number1.length - 2).toInt() in bluIcon) {
                    imageContact.backgroundTintList = ContextCompat.getColorStateList(context, R.color.blue_icon)
                }
                if (number1.substring(number1.length - 2).toInt() in orangIcon) {
                    imageContact.backgroundTintList = ContextCompat.getColorStateList(context, R.color.orange_icon)
                }
                if (number1.substring(number1.length - 2).toInt() in greenIcon) {
                    imageContact.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_icon)
                }
                if (number1.substring(number1.length - 2).toInt() in filIcon) {
                    imageContact.backgroundTintList = ContextCompat.getColorStateList(context, R.color.fiol_icon)
                }

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


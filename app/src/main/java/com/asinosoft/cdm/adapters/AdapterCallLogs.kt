package com.asinosoft.cdm.adapters

import android.content.Context
import android.content.Intent
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.*
import com.asinosoft.cdm.Metoths.Companion.setColoredText
import com.asinosoft.cdm.api.CursorApi.getDisplayPhoto
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.asinosoft.cdm.globals.Globals
import com.zerobranch.layout.SwipeLayout
import kotlinx.coroutines.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.runOnUiThread
import kotlin.coroutines.CoroutineContext


class AdapterCallLogs(
    var items: ArrayList<HistoryItem>,
    val onClick: Boolean = true,
    val context: Context,
    var onAdd: (Int) -> Unit = {}
) : RecyclerView.Adapter<AdapterCallLogs.HolderHistory>(), CoroutineScope {

    private var nums = ""
    private var regex: Regex? = null
    private var jobFilter: Job = Job()
    val buffer = ArrayList<HistoryItem>()
    private var listBackup = items

    init {
        Globals.adapterLogs = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        return HolderHistory(
            CalllogObjectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun setList(list: List<HistoryItem>) {
        this.items = ArrayList(list)
        listBackup = ArrayList(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        holder.bind(items[position], position)
    }

    private fun openDetail(item: HistoryItem) {
        val intent = Intent(this.context, DetailHistoryActivity::class.java)
        intent.putExtra(Keys.number, item.numberContact)
        intent.putExtra(Keys.id, item.contact.id)
        context.startActivity(intent)
    }

    fun upIntoBuffer() {
        val lastIndex = items.lastIndex
        items.addAll(buffer)
        notifyItemRangeInserted(lastIndex, items.lastIndex)
        notifyDataSetChanged()
        buffer.clear()
    }

    private suspend fun List<HistoryItem>.filtered(nums: String): ArrayList<HistoryItem> =
        coroutineScope {
            val r = ArrayList<HistoryItem>()
            this@filtered.forEach { item ->
                if (item.numberContact.isNullOrEmpty()) return@forEach
                if (item.contact.name.contains(nums, true)) {
                    r.add(item)
                    return@forEach
                }
            }
            return@coroutineScope r
        }

    fun setFilter(nums: String = "") {
        this.nums = nums
        regex = Regex(Metoths.getPattern(nums.replace("1", ""), context), RegexOption.IGNORE_CASE)
        jobFilter = launch {
            runBlocking {
                items = listBackup.filtered(nums)
                Log.d(
                    "AdapterContact",
                    "Contacts filtered! -> Contacts = ${items.size}; Nums = $nums"
                )

                context.runOnUiThread {
                    notifyDataSetChanged()
                }
            }
        }
    }


    inner class HolderHistory(val v: CalllogObjectBinding) : RecyclerView.ViewHolder(v.root) {

        private fun setIcons(
            settings: Settings,
            imageLeftAction: CircularImageView,
            imageRightAction: CircularImageView
        ) {
            imageLeftAction.imageResource =
                if (settings.leftButton == Actions.WhatsApp) R.drawable.telephony_call_192
                else R.drawable.whatsapp_192
            imageRightAction.imageResource =
                if (settings.rightButton == Actions.WhatsApp) R.drawable.telephony_call_192
                else R.drawable.whatsapp_192
        }

        fun bind(item: HistoryItem, pos: Int) {

            with(v) {
                item.contact.id.let {
                    if (it > 0) getDisplayPhoto(it, context)?.let { bitmap ->
                        imageContact.setImageBitmap(bitmap)
                    }
                }
                imageContact.setImageDrawable(item.contact.photo)
                name.text = item.contact.name
                number.text = "${item.numberContact}, ${Metoths.getFormatedTime(item.duration)}"
                timeContact.text = item.time
                dateContact.text = item.date
                val settings = Loader.loadContactSettings(item.numberContact)
                imageLeftAction.imageResource =
                    if (settings.leftButton == Actions.WhatsApp) R.drawable.telephony_call_192
                    else R.drawable.whatsapp_192
                imageRightAction.imageResource =
                    if (settings.rightButton == Actions.WhatsApp) R.drawable.telephony_call_192
                    else R.drawable.whatsapp_192

                setColors(true)

                when (item.typeCall) {
                    CallLog.Calls.OUTGOING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_made_24)
                    CallLog.Calls.INCOMING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_received_24)
                    CallLog.Calls.MISSED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_missed_24)
                    CallLog.Calls.BLOCKED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_canceled_24)
                }

                dragLayout.setOnTouchListener { view, motionEvent ->
                    if(motionEvent.action == ACTION_DOWN){
                        val settings = Loader.loadContactSettings(item.numberContact)
                        setIcons(settings, imageLeftAction, imageRightAction)
                    }
                    false
                }

                swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                    override fun onOpen(direction: Int, isContinuous: Boolean) {
                        val settings = Loader.loadContactSettings(item.numberContact)
                        setIcons(settings, imageLeftAction, imageRightAction)
                        when (direction) {
                            SwipeLayout.RIGHT -> {
                                if (settings.rightButton != Actions.WhatsApp)
                                    Metoths.callPhone(item.numberContact, context)
                                else Metoths.openWhatsApp(item.numberContact, context)
                            }
                            SwipeLayout.LEFT -> {
                                if (settings.leftButton != Actions.WhatsApp)
                                    Metoths.callPhone(item.numberContact, context)
                                else Metoths.openWhatsApp(item.numberContact, context)
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

        /**
         * Установка выделения текста по номеру телефона.
         * @param name Установка выделения текста также по имени
         */
        private fun setColors(name: Boolean = false) {
            if (nums.isNotEmpty() && v.number.text.contains(nums)) v.number.setColoredText(nums)
            if (name) regex?.find(v.name.text.toString())?.let {
                v.name.setColoredText(it.value)
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + Job()

}


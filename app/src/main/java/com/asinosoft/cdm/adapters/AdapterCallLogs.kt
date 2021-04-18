package com.asinosoft.cdm.adapters

import android.content.Context
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.asinosoft.cdm.*
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.data.PhoneItem
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.zerobranch.layout.SwipeLayout
import org.jetbrains.anko.imageResource
import timber.log.Timber
import java.security.InvalidParameterException

/**
 * Адаптер списка последних звонков, который показывается в активности "Просмотр контакта"
 */
class AdapterCallLogs(
    private val context: Context,
    private val favorites: ViewBinding
) : RecyclerView.Adapter<AdapterCallLogs.HolderHistory>() {
    companion object {
        const val TYPE_FAVORITES = 1
        const val TYPE_CALL_ITEM = 2
    }

    private var items: List<CallHistoryItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        val view = when (viewType) {
            TYPE_FAVORITES -> favorites
            TYPE_CALL_ITEM -> CalllogObjectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            else -> throw InvalidParameterException("Unknown viewType=$viewType")
        }
        return HolderHistory(view)
    }

    /**
     * Заменяет список звонков
     */
    fun setList(list: List<CallHistoryItem>) {
        items = list
        Timber.d("AdapterCallLogs получил %d звонков/контактов", list.size)
    }

    override fun getItemCount() = items.size + 1

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_FAVORITES
            else -> TYPE_CALL_ITEM
        }
    }

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        if (position > 0) {
            holder.bind(items[position - 1])
        }
    }

    inner class HolderHistory(val v: ViewBinding) : RecyclerView.ViewHolder(v.root) {

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

        fun bind(item: CallHistoryItem) {
            with(v as CalllogObjectBinding) {
                imageContact.setImageDrawable(item.contact.getPhoto())
                name.text = item.contact.name
                number.text = "${item.prettyPhone}, ${Metoths.getFormattedTime(item.duration)}"
                timeContact.text = item.time
                dateContact.text = item.date
                val settings = Loader.loadContactSettings(item.phone)
                imageLeftAction.imageResource =
                    if (settings.leftButton == Actions.WhatsApp) R.drawable.telephony_call_192
                    else R.drawable.whatsapp_192
                imageRightAction.imageResource =
                    if (settings.rightButton == Actions.WhatsApp) R.drawable.telephony_call_192
                    else R.drawable.whatsapp_192

                when (item.typeCall) {
                    CallLog.Calls.OUTGOING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_made_24)
                    CallLog.Calls.INCOMING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_received_24)
                    CallLog.Calls.MISSED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_missed_24)
                    CallLog.Calls.BLOCKED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_canceled_24)
                }

                dragLayout.setOnTouchListener { view, motionEvent ->
                    if (motionEvent.action == ACTION_DOWN) {
                        val settings = Loader.loadContactSettings(item.phone)
                        setIcons(settings, imageLeftAction, imageRightAction)
                    }
                    false
                }

                swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                    override fun onOpen(direction: Int, isContinuous: Boolean) {
                        setIcons(settings, imageLeftAction, imageRightAction)
                        when (direction) {
                            SwipeLayout.RIGHT -> {
                                if (settings.rightButton != Actions.WhatsApp)
                                    callPhone(item)
                                else
                                    openWhatsAppChat(item)
                            }
                            SwipeLayout.LEFT -> {
                                if (settings.leftButton != Actions.WhatsApp)
                                    callPhone(item)
                                else
                                    openWhatsAppChat(item)
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

                dragLayout.setOnClickListener {
                    Metoths.openDetailContact(
                        item.phone,
                        item.contact,
                        context
                    )
                }
            }
        }
    }

    private fun callPhone(item: CallHistoryItem) {
        PhoneItem(item.phone).call(context)
    }

    private fun openWhatsAppChat(item: CallHistoryItem) {
        item.contact.whatsapps.firstOrNull()?.chat(context)
    }
}

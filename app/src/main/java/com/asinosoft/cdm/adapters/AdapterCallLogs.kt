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
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.zerobranch.layout.SwipeLayout
import org.jetbrains.anko.imageResource
import timber.log.Timber

/**
 * Адаптер списка последних звонков, который показывается в активности "Просмотр контакта"
 */
class AdapterCallLogs(
    val context: Context,
) : RecyclerView.Adapter<AdapterCallLogs.HolderHistory>() {
    private val VISIBLE_ITEMS_LIMIT = 21

    private var items: List<CallHistoryItem> = listOf()
    private var hiddenItems: List<CallHistoryItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        return HolderHistory(
            CalllogObjectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    /**
     * Заменяет список звонков
     */
    fun setList(list: List<CallHistoryItem>) {
        // Список звонков разделяется на две части: одна показывается сразу, а вторая - только после нажатия на кнопку "Показать ещё"
        // Это сделано, чтобы ускорить отрисовку при запуске активности
        // А тормозит она потому, что там RecyclerView растянуют на всю высоту внутри NestedScrollView
        // FIXME: Чтобы решить эту проблему, нужно показывать блок избранных контактов, как (не)обычный элемент внутри списка звонков
        items = list.take(VISIBLE_ITEMS_LIMIT)
        hiddenItems = list.subList(VISIBLE_ITEMS_LIMIT, list.size)
        notifyDataSetChanged()
        Timber.d("AdapterCallLogs получил %d звонков/контактов", list.size)
    }

    /**
     * Добавление вкрытых звонков в список (реакция на кнопку "Показать ещё")
     */
    fun showHiddenItems() {
        val positionStart = items.size
        val itemCount = hiddenItems.size
        items = items.plus(hiddenItems)
        hiddenItems = listOf()
        notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        holder.bind(items[position])
    }

    private fun openDetail(item: CallHistoryItem) {
        val intent = Intent(this.context, DetailHistoryActivity::class.java)
        intent.putExtra(Keys.number, item.phone)
        intent.putExtra(Keys.id, item.contact.id)
        context.startActivity(intent)
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

        fun bind(item: CallHistoryItem) {
            with(v) {
                imageContact.setImageDrawable(item.contact.getPhoto())
                name.text = item.contact.name
                number.text = "${item.phone}, ${Metoths.getFormattedTime(item.duration)}"
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
                        val settings = Loader.loadContactSettings(item.phone)
                        setIcons(settings, imageLeftAction, imageRightAction)
                        when (direction) {
                            SwipeLayout.RIGHT -> {
                                if (settings.rightButton != Actions.WhatsApp)
                                    Metoths.callPhone(item.phone, context)
                                else Metoths.openWhatsApp(item.phone, context)
                            }
                            SwipeLayout.LEFT -> {
                                if (settings.leftButton != Actions.WhatsApp)
                                    Metoths.callPhone(item.phone, context)
                                else Metoths.openWhatsApp(item.phone, context)
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

                dragLayout.setOnClickListener { openDetail(item) }
            }
        }
    }
}

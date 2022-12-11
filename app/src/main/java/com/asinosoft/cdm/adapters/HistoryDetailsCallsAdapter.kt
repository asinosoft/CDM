package com.asinosoft.cdm.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.Config
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.ContactCallItemBinding
import com.asinosoft.cdm.helpers.DateHelper
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.StHelper
import com.asinosoft.cdm.helpers.getThemeColor
import com.yandex.mobile.ads.banner.AdSize
import com.zerobranch.layout.SwipeLayout
import java.util.*
import com.google.android.gms.ads.AdRequest as GoogleAds
import com.yandex.mobile.ads.common.AdRequest as YandexAds

/**
 * Адаптер списка звонков, который показывается в активности "Просмотр контакта"
 */
class HistoryDetailsCallsAdapter(
    private val config: Config,
    private val context: Context,
    private val calls: List<CallHistoryItem>,
    private val onDeleteCallRecord: (call: CallHistoryItem) -> Unit,
    private val onPurgeContactHistory: (contact: Contact) -> Unit
) :
    RecyclerView.Adapter<HistoryDetailsCallsAdapter.HolderHistory>() {

    private val today: Date = StHelper.today()
    private val yesterday: Date = Date(today.time - 86400)

    private var popupCall: CallHistoryItem? = null
    private var popupColor: Int = context.getThemeColor(android.R.attr.listDivider)
    private var backgroundColor: Int = context.getThemeColor(R.attr.backgroundColor)

    override fun getItemCount() = calls.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactCallItemBinding.inflate(inflater, parent, false)
        binding.yandexAds.apply {
            setAdUnitId(context.getString(R.string.yandex_ads_unit_id))
            setAdSize(AdSize.flexibleSize(320, 50))
        }
        return HolderHistory(binding)
    }

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        bindCallHistoryItem(holder.v, calls[position])
        bindAdvertiser(holder.v, position == (calls.size - 1).coerceAtMost(2))
    }

    private fun bindAdvertiser(v: ContactCallItemBinding, visible: Boolean) {
        if (!visible) {
            v.yandexAds.visibility = View.GONE
            v.googleAds.visibility = View.GONE
        } else if ("ru" == Locale.getDefault().language) {
            v.yandexAds.apply {
                loadAd(YandexAds.Builder().build())
                visibility = View.VISIBLE
            }
        } else {
            v.googleAds.apply {
                loadAd(GoogleAds.Builder().build())
                visibility = View.VISIBLE
            }
        }
    }

    private fun bindCallHistoryItem(v: ContactCallItemBinding, call: CallHistoryItem) {
        v.number.text = call.prettyPhone
        v.duration.text = Metoths.getFormattedTime(call.duration)
        v.time.text = call.time
        v.date.text = formatDate(call.timestamp)

        val directActions = config.getContactSettings(call.contact)
        v.imageLeftAction.setImageResource(Action.resourceByType(directActions.left.type))
        v.imageRightAction.setImageResource(Action.resourceByType(directActions.right.type))

        when (call.typeCall) {
            CallLog.Calls.OUTGOING_TYPE -> v.type.setImageResource(R.drawable.ic_call_outgoing)
            CallLog.Calls.INCOMING_TYPE -> v.type.setImageResource(R.drawable.ic_call_incoming)
            CallLog.Calls.MISSED_TYPE -> v.type.setImageResource(R.drawable.ic_call_missed)
            CallLog.Calls.BLOCKED_TYPE -> v.type.setImageResource(R.drawable.ic_call_blocked)
            CallLog.Calls.REJECTED_TYPE -> v.type.setImageResource(R.drawable.ic_call_rejected)
        }

        when (call.sim) {
            1 -> v.sim.setImageResource(R.drawable.ic_sim1)
            2 -> v.sim.setImageResource(R.drawable.ic_sim2)
            3 -> v.sim.setImageResource(R.drawable.ic_sim3)
            else -> v.sim.visibility = View.GONE
        }

        v.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onOpen(direction: Int, isContinuous: Boolean) {
                when (direction) {
                    SwipeLayout.RIGHT -> {
                        Analytics.logContactHistorySwipeRight()
                        directActions.right.perform(context)
                    }
                    SwipeLayout.LEFT -> {
                        Analytics.logContactHistorySwipeLeft()
                        directActions.left.perform(context)
                    }
                }

                // bugfix: Если сделать действие-влево, то потом эта строка уже вправо не двигается пока немного влево сдвинешь и отпустишь.
                notifyDataSetChanged()
            }

            override fun onClose() {
            }
        })

        v.dragLayout.setOnLongClickListener {
            showPopup(it, call)
            true
        }

        v.dragLayout.setBackgroundColor(
            if (call == popupCall) popupColor else backgroundColor
        )
    }

    private fun showPopup(view: View, call: CallHistoryItem) {
        popupCall = call
        notifyItemChanged(calls.indexOf(popupCall))

        val popup = PopupMenu(view.context, view, Gravity.END)
        popup.inflate(R.menu.contact_history_context_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_call_item -> onDeleteCallRecord(call)
                R.id.purge_contact_history -> onPurgeContactHistory(call.contact)
            }
            true
        }
        popup.setOnDismissListener {
            popupCall?.let {
                notifyItemChanged(calls.indexOf(popupCall))
            }
            popupCall = null
        }
        popup.show()
    }

    private fun formatDate(date: Date): String {
        if (date.after(today)) {
            return context.getString(R.string.date_today) + ','
        } else if (date.after(yesterday)) {
            return context.getString(R.string.date_yesterday) + ','
        } else {
            return DateHelper.shortDate(date) + ','
        }
    }

    inner class HolderHistory(val v: ContactCallItemBinding) : RecyclerView.ViewHolder(v.root)
}

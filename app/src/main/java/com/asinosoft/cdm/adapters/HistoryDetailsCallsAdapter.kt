package com.asinosoft.cdm.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.Config
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.databinding.AdvertiserBinding
import com.asinosoft.cdm.databinding.ContactCallItemBinding
import com.asinosoft.cdm.helpers.DateHelper
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.StHelper
import com.google.android.gms.ads.AdRequest
import com.zerobranch.layout.SwipeLayout
import java.util.*

/**
 * Адаптер списка звонков, который показывается в активности "Просмотр контакта"
 */
class HistoryDetailsCallsAdapter(
    private val config: Config,
    private val context: Context,
    private val calls: List<CallHistoryItem>
) :
    RecyclerView.Adapter<HistoryDetailsCallsAdapter.HolderHistory>() {

    companion object {
        const val TYPE_CALL = 0
        const val TYPE_ADVERTISER = 1
    }

    private val today: Date = StHelper.today()
    private val yesterday: Date = Date(today.time - 86400)

    override fun getItemCount() = 1 + calls.size

    override fun getItemViewType(position: Int): Int {
        if (calls.size < 3) {
            return if (position >= calls.size) TYPE_ADVERTISER else TYPE_CALL
        } else {
            return if (position == 3) TYPE_ADVERTISER else TYPE_CALL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        val binding = when (viewType) {
            TYPE_CALL -> ContactCallItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            TYPE_ADVERTISER -> AdvertiserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            else -> throw Exception("Invalid view type: $viewType")
        }
        return HolderHistory(binding)
    }

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        when (val binding = holder.v) {
            is ContactCallItemBinding -> bindCallHistoryItem(
                binding,
                if (position < 3) calls[position] else calls[position - 1]
            )
            is AdvertiserBinding -> bindAdvertiser(binding)
        }
    }

    private fun bindAdvertiser(v: AdvertiserBinding) {
        val adRequest = AdRequest.Builder().build()
        v.adView.loadAd(adRequest)
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

    inner class HolderHistory(val v: ViewBinding) : RecyclerView.ViewHolder(v.root)
}

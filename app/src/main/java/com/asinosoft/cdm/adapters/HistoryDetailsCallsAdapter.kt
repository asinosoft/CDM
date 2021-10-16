package com.asinosoft.cdm.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.databinding.ContactCallItemBinding
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.StHelper
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.zerobranch.layout.SwipeLayout
import org.jetbrains.anko.imageResource
import java.util.*

/**
 * Адаптер списка звонков, который показывается в активности "Просмотр контакта"
 */
class HistoryDetailsCallsAdapter(
    private val context: Context,
    private val calls: List<CallHistoryItem>
) :
    RecyclerView.Adapter<HistoryDetailsCallsAdapter.HolderHistory>() {

    private val prettyDateFormat = java.text.SimpleDateFormat("dd MMMM", Locale.getDefault())
    private val today: Date = StHelper.today()
    private val yesterday: Date = Date(today.time - 86400)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        return HolderHistory(
            ContactCallItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = calls.size

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        bindCallHistoryItem(holder.v, calls[position])
    }

    private fun bindCallHistoryItem(v: ContactCallItemBinding, call: CallHistoryItem) {
        v.number.text = context.resources.getString(
            R.string.call_contact_number,
            call.prettyPhone,
            Metoths.getFormattedTime(call.duration)
        )
        v.time.text = call.time
        v.date.text = formatDate(call.timestamp)

        val directActions = Loader.loadContactSettings(context, call.contact)
        v.imageLeftAction.imageResource = Action.resourceByType(directActions.left.type)
        v.imageRightAction.imageResource = Action.resourceByType(directActions.right.type)

        when (call.typeCall) {
            CallLog.Calls.OUTGOING_TYPE -> v.type.setImageResource(R.drawable.baseline_call_made_24)
            CallLog.Calls.INCOMING_TYPE -> v.type.setImageResource(R.drawable.baseline_call_received_24)
            CallLog.Calls.MISSED_TYPE -> v.type.setImageResource(R.drawable.baseline_call_missed_24)
            CallLog.Calls.BLOCKED_TYPE -> v.type.setImageResource(R.drawable.baseline_call_canceled_24)
        }

        v.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onOpen(direction: Int, isContinuous: Boolean) {
                when (direction) {
                    SwipeLayout.RIGHT -> {
                        Firebase.analytics.logEvent("contact_history_swipe_right", Bundle.EMPTY)
                        directActions.right.perform(context)
                    }
                    SwipeLayout.LEFT -> {
                        Firebase.analytics.logEvent("contact_history_swipe_left", Bundle.EMPTY)
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
            return prettyDateFormat.format(date) + ','
        }
    }

    inner class HolderHistory(val v: ContactCallItemBinding) : RecyclerView.ViewHolder(v.root)
}

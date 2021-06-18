package com.asinosoft.cdm.adapters

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
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.asinosoft.cdm.helpers.Metoths
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.zerobranch.layout.SwipeLayout
import org.jetbrains.anko.imageResource

/**
 * Адаптер списка звонков, который показывается в активности "Просмотр контакта"
 */
class HistoryDetailsCallsAdapter(
    private val context: Context,
    private val calls: List<CallHistoryItem>
) :
    RecyclerView.Adapter<HistoryDetailsCallsAdapter.HolderHistory>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        return HolderHistory(
            CalllogObjectBinding.inflate(
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

    private fun bindCallHistoryItem(v: CalllogObjectBinding, call: CallHistoryItem) {
        v.imageContact.setImageDrawable(call.contact.getPhoto(context))
        v.name.text = call.contact.name
        v.number.text = "${call.prettyPhone}, ${Metoths.getFormattedTime(call.duration)}"
        v.timeContact.text = call.time
        v.dateContact.text = call.date

        val directActions = Loader.loadContactSettings(context, call.contact)
        v.imageLeftAction.imageResource = Action.resourceByType(directActions.left.type)
        v.imageRightAction.imageResource = Action.resourceByType(directActions.right.type)

        when (call.typeCall) {
            CallLog.Calls.OUTGOING_TYPE -> v.typeCall.setImageResource(R.drawable.baseline_call_made_24)
            CallLog.Calls.INCOMING_TYPE -> v.typeCall.setImageResource(R.drawable.baseline_call_received_24)
            CallLog.Calls.MISSED_TYPE -> v.typeCall.setImageResource(R.drawable.baseline_call_missed_24)
            CallLog.Calls.BLOCKED_TYPE -> v.typeCall.setImageResource(R.drawable.baseline_call_canceled_24)
        }

        v.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
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

    inner class HolderHistory(val v: CalllogObjectBinding) : RecyclerView.ViewHolder(v.root)
}

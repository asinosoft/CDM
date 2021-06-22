package com.asinosoft.cdm.adapters

import android.content.Context
import android.os.Bundle
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.asinosoft.cdm.helpers.Metoths
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.zerobranch.layout.SwipeLayout
import org.jetbrains.anko.imageResource
import java.security.InvalidParameterException

/**
 * Адаптер списка последних звонков, который показывается в активности "Просмотр контакта"
 */
class CallsAdapter(
    private val context: Context,
    private val favorites: ViewBinding,
    private val handler: Handler
) : RecyclerView.Adapter<CallsAdapter.HolderHistory>() {
    companion object {
        const val TYPE_FAVORITES = 1
        const val TYPE_CALL_ITEM = 2
    }

    private var calls: List<CallHistoryItem> = listOf()

    interface Handler {
        fun onClickContact(contact: Contact)
        fun onClickPhone(phone: String)
    }

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
    fun setList(newList: List<CallHistoryItem>) {
        val oldList = calls
        this.calls = newList
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = 1 + oldList.size

            override fun getNewListSize(): Int = 1 + newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return /* избранные контакты */ (0 == oldItemPosition && 0 == newItemPosition) ||
                        /* звонки */ oldItemPosition > 0 && newItemPosition > 0 &&
                        oldList[oldItemPosition - 1] == newList[newItemPosition - 1]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return /* избранные контакты */ (0 == oldItemPosition && 0 == newItemPosition) ||
                        /* звонки */ oldItemPosition > 0 && newItemPosition > 0 &&
                        oldList[oldItemPosition - 1] == newList[newItemPosition - 1]
            }
        }).dispatchUpdatesTo(this)
    }

    override fun getItemCount() = calls.size + 1

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_FAVORITES
            else -> TYPE_CALL_ITEM
        }
    }

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        when (holder.v) {
            is CalllogObjectBinding -> bindCallHistoryItem(holder.v, calls[position - 1])
        }
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
                        Firebase.analytics.logEvent("history_swipe_right", Bundle.EMPTY)
                        directActions.right.perform(context)
                    }
                    SwipeLayout.LEFT -> {
                        Firebase.analytics.logEvent("history_swipe_left", Bundle.EMPTY)
                        directActions.left.perform(context)
                    }
                }

                // bugfix: Если сделать действие-влево, то потом эта строка уже вправо не двигается пока немного влево сдвинешь и отпустишь.
                notifyDataSetChanged()
            }

            override fun onClose() {
            }
        })

        v.dragLayout.setOnClickListener {
            if (0L == call.contact.id) {
                handler.onClickPhone(call.phone)
            } else {
                handler.onClickContact(call.contact)
            }
        }
    }

    inner class HolderHistory(val v: ViewBinding) : RecyclerView.ViewHolder(v.root)
}

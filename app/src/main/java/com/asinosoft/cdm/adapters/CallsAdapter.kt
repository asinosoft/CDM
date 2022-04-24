package com.asinosoft.cdm.adapters

import android.content.Context
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.Config
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.ItemCallBinding
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.StHelper
import com.zerobranch.layout.SwipeLayout
import java.security.InvalidParameterException

/**
 * Адаптер списка последних звонков, который показывается в активности "Просмотр контакта"
 */
class CallsAdapter(
    private val config: Config,
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
            TYPE_CALL_ITEM -> ItemCallBinding.inflate(
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
            is ItemCallBinding -> bindCallHistoryItem(holder.v, calls[position - 1])
        }
    }

    private fun bindCallHistoryItem(v: ItemCallBinding, call: CallHistoryItem) {
        v.topDivider.isVisible = config.listDivider && config.favoritesFirst
        v.bottomDivider.isVisible = config.listDivider && !config.favoritesFirst
        v.imageContact.setImageDrawable(call.contact.getAvatar(context))
        config.favoritesBorderColor?.let { v.imageContact.borderColor = it }
        v.name.text = call.contact.name
        v.number.text = call.prettyPhone
        v.duration.text = Metoths.getFormattedTime(call.duration)
        v.dateContact.text = call.date
        if (call.timestamp.after(StHelper.today()))
            v.timeContact.text = call.time
        else
            v.timeContact.visibility = View.INVISIBLE

        val directActions = config.getContactSettings(call.contact)
        v.imageLeftAction.setImageResource(Action.resourceByType(directActions.left.type))
        v.imageRightAction.setImageResource(Action.resourceByType(directActions.right.type))

        v.typeCall.setImageResource(
            when (call.typeCall) {
                CallLog.Calls.OUTGOING_TYPE -> R.drawable.ic_call_outgoing
                CallLog.Calls.INCOMING_TYPE -> R.drawable.ic_call_incoming
                CallLog.Calls.MISSED_TYPE -> R.drawable.ic_call_missed
                CallLog.Calls.BLOCKED_TYPE -> R.drawable.ic_call_blocked
                CallLog.Calls.REJECTED_TYPE -> R.drawable.ic_call_rejected
                else -> R.drawable.ic_call_missed
            }
        )

        when (call.sim) {
            1 -> v.sim.setImageResource(R.drawable.sim1)
            2 -> v.sim.setImageResource(R.drawable.sim2)
            3 -> v.sim.setImageResource(R.drawable.sim3)
            else -> v.sim.visibility = View.GONE
        }

        v.typeCall.contentDescription = context.resources.getString(
            when (call.typeCall) {
                CallLog.Calls.OUTGOING_TYPE -> R.string.call_type_outgoing
                CallLog.Calls.INCOMING_TYPE -> R.string.call_type_incoming
                CallLog.Calls.MISSED_TYPE -> R.string.call_type_missed
                CallLog.Calls.BLOCKED_TYPE -> R.string.call_type_blocked
                CallLog.Calls.REJECTED_TYPE -> R.string.call_type_rejected
                else -> R.string.call_type_other
            }
        )

        v.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
            override fun onOpen(direction: Int, isContinuous: Boolean) {
                when (direction) {
                    SwipeLayout.RIGHT -> {
                        Analytics.logHistorySwipeRight()
                        performSwipeAction(directActions.right, call)
                    }
                    SwipeLayout.LEFT -> {
                        Analytics.logHistorySwipeLeft()
                        performSwipeAction(directActions.left, call)
                    }
                }

                // bugfix: Если сделать действие-влево, то потом эта строка уже вправо не двигается пока немного влево сдвинешь и отпустишь.
                notifyDataSetChanged()
            }

            override fun onClose() {
            }
        })

        v.dragLayout.setOnClickListener {
            Analytics.logCallHistoryClick()
            if (0L == call.contact.id) {
                handler.onClickPhone(call.phone)
            } else {
                handler.onClickContact(call.contact)
            }
        }
    }

    inner class HolderHistory(val v: ViewBinding) : RecyclerView.ViewHolder(v.root)

    private fun performSwipeAction(action: Action, item: CallHistoryItem) {
        if (action.type == Action.Type.PhoneCall) {
            // Звонок делаем по тому телефону, который в истории, а не который в настройках контакта!
            Action(0, Action.Type.PhoneCall, item.phone, "").perform(context)
        } else {
            action.perform(context)
        }
    }
}

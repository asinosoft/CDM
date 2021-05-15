package com.asinosoft.cdm.adapters

import android.content.Context
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.asinosoft.cdm.views.CircularImageView
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.DirectActions
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.zerobranch.layout.SwipeLayout
import org.jetbrains.anko.imageResource
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

    private var calls: List<CallHistoryItem> = listOf()

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
        if (position > 0) {
            holder.bind(calls[position - 1])
        }
    }

    inner class HolderHistory(val v: ViewBinding) : RecyclerView.ViewHolder(v.root) {

        private fun setIcons(
            directActions: DirectActions,
            imageLeftAction: CircularImageView,
            imageRightAction: CircularImageView
        ) {
            imageLeftAction.imageResource = Action.resourceByType(directActions.left.type)
            imageRightAction.imageResource = Action.resourceByType(directActions.right.type)
        }

        fun bind(item: CallHistoryItem) {
            with(v as CalllogObjectBinding) {
                imageContact.setImageDrawable(item.contact.getPhoto(context))
                name.text = item.contact.name
                number.text = "${item.prettyPhone}, ${Metoths.getFormattedTime(item.duration)}"
                timeContact.text = item.time
                dateContact.text = item.date

                when (item.typeCall) {
                    CallLog.Calls.OUTGOING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_made_24)
                    CallLog.Calls.INCOMING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_received_24)
                    CallLog.Calls.MISSED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_missed_24)
                    CallLog.Calls.BLOCKED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_canceled_24)
                }

                dragLayout.setOnTouchListener { view, motionEvent ->
                    if (motionEvent.action == ACTION_DOWN) {
                        val directActions = Loader.loadContactSettings(context, item.contact)
                        setIcons(directActions, imageLeftAction, imageRightAction)
                    }
                    false
                }

                swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                    override fun onOpen(direction: Int, isContinuous: Boolean) {
                        val directActions = Loader.loadContactSettings(context, item.contact)
                        when (direction) {
                            SwipeLayout.RIGHT -> directActions.right.perform(context)
                            SwipeLayout.LEFT -> directActions.left.perform(context)
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
}

package com.asinosoft.cdm.adapters

import android.content.Context
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.*
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.DirectActions
import com.asinosoft.cdm.databinding.CalllogObjectBinding
import com.zerobranch.layout.SwipeLayout
import org.jetbrains.anko.imageResource

/**
 * Адаптер списка звонков, который показывается в активности "Просмотр контакта"
 */
class HistoryDetailsCallsAdapter(val context: Context) :
    RecyclerView.Adapter<HistoryDetailsCallsAdapter.HolderHistory>() {
    private var items = ArrayList<CallHistoryItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory {
        return HolderHistory(
            CalllogObjectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun setList(list: List<CallHistoryItem>) {
        items = ArrayList(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        holder.bind(items[position])
    }

    inner class HolderHistory(val v: CalllogObjectBinding) : RecyclerView.ViewHolder(v.root) {

        private fun setIcons(
            directActions: DirectActions,
            imageLeftAction: CircularImageView,
            imageRightAction: CircularImageView
        ) {
            imageLeftAction.imageResource = Action.resourceByType(directActions.left.type)
            imageRightAction.imageResource = Action.resourceByType(directActions.right.type)
        }

        fun bind(item: CallHistoryItem) {
            with(v) {
                imageContact.setImageDrawable(item.contact.getPhoto(context))
                name.text = item.contact.name
                number.text = "${item.phone}, ${Metoths.getFormattedTime(item.duration)}"
                timeContact.text = item.time
                dateContact.text = item.date
                val directActions = Loader.loadContactSettings(context, item.contact)
                setIcons(directActions, imageLeftAction, imageRightAction)

                when (item.typeCall) {
                    CallLog.Calls.OUTGOING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_made_24)
                    CallLog.Calls.INCOMING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_received_24)
                    CallLog.Calls.MISSED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_missed_24)
                    CallLog.Calls.BLOCKED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_canceled_24)
                }

                dragLayout.setOnTouchListener { view, motionEvent ->
                    if (motionEvent.action == ACTION_DOWN) {
                        setIcons(directActions, imageLeftAction, imageRightAction)
                    }
                    false
                }

                swipeLayout.setOnActionsListener(
                    object : SwipeLayout.SwipeActionsListener {
                        override fun onOpen(direction: Int, isContinuous: Boolean) {
                            setIcons(directActions, imageLeftAction, imageRightAction)
                            when (direction) {
                                SwipeLayout.RIGHT -> directActions.right.perform(context)
                                SwipeLayout.LEFT -> directActions.left.perform(context)
                                else -> Log.e(
                                    "AdapterHistory.kt: ",
                                    "SwipeLayout direction UNKNOWN = $direction"
                                )
                            }

                            swipeLayout.close()
                        }

                        override fun onClose() {
                        }
                    }
                )
            }
        }
    }
}

package com.asinosoft.cdm.adapters

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.startActivity
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
import com.asinosoft.cdm.helpers.*
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import com.zerobranch.layout.SwipeLayout
import java.security.InvalidParameterException

/**
 * Адаптер списка последних звонков, который показывается в активности "Просмотр контакта"
 */
class CallsAdapter(
    private val config: Config,
    private val context: Context,
    private val favorites: ViewBinding,
    private val onClickContact: (contact: Contact) -> Unit,
    private val onClickPhone: (phone: String) -> Unit,
    private val onDeleteCallRecord: (call: CallHistoryItem) -> Unit,
    private val onPurgeContactHistory: (contact: Contact) -> Unit,
    private val onPurgeCallHistory: () -> Unit,
) : RecyclerView.Adapter<CallsAdapter.HolderHistory>() {
    companion object {
        const val TYPE_FAVORITES = 1
        const val TYPE_CALL_ITEM = 2
    }

    private var calls: List<CallHistoryItem> = listOf()

    private var popupCall: CallHistoryItem? = null
    private var popupColor: Int = context.getThemeColor(android.R.attr.listDivider)
    private var backgroundColor: Int = context.getThemeColor(R.attr.backgroundColor)

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
        v.imageContact.setImageDrawable(
            if (null == call.contact)
                AvatarHelper.generate(context, call.phone, AvatarHelper.IMAGE)
            else
                call.contact.getAvatar(context, AvatarHelper.SHORT)
        )
        config.favoritesBorderColor?.let { v.imageContact.borderColor = it }
        v.name.text = call.contact?.name ?: call.phone

        if (null == call.contact) {
            v.number.setText(R.string.unsaved)
        } else {
            v.number.text = call.prettyPhone
        }

        v.dateContact.visibility = View.INVISIBLE // нужно убрать dateContact из layout
        if (call.timestamp.after(StHelper.today()))
            v.timeContact.text = call.time
        else
            v.timeContact.text = call.date

        val directActions =
            if (null == call.contact)
                config.getDefaultSettings(call.phone)
            else
                config.getContactSettings(call.contact)

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
            1 -> v.sim.setImageResource(R.drawable.ic_sim1)
            2 -> v.sim.setImageResource(R.drawable.ic_sim2)
            3 -> v.sim.setImageResource(R.drawable.ic_sim3)
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
                        context.vibrator.vibrateSafety(Keys.VIBRO, 255)
                        performSwipeAction(directActions.right, call)
                    }
                    SwipeLayout.LEFT -> {
                        Analytics.logHistorySwipeLeft()
                        context.vibrator.vibrateSafety(Keys.VIBRO, 255)
                        performSwipeAction(directActions.left, call)
                    }
                }

                // bugfix: Если сделать действие-влево, то потом эта строка уже вправо не двигается пока немного влево сдвинешь и отпустишь.
                notifyDataSetChanged()
            }

            override fun onClose() {
            }
        })

        v.imageContact.setOnClickListener {
            if (null == call.contact) {
                addNewContact(call.prettyPhone)
            } else {
                Analytics.logCallHistoryClick()
                onClickContact(call.contact)
            }
        }

        v.dragLayout.setOnClickListener {
            Analytics.logCallHistoryClick()
            if (null == call.contact) {
                onClickPhone(call.phone)
            } else {
                onClickContact(call.contact)
            }
        }

        v.dragLayout.setOnLongClickListener {
            showPopup(it, call)
            true
        }

        v.dragLayout.setBackgroundColor(
            if (call == popupCall) popupColor else backgroundColor
        )
    }

    private fun addNewContact(prettyPhone: CharSequence) {

        Intent().apply {
            action = Intent.ACTION_INSERT_OR_EDIT
            type = "vnd.android.cursor.item/contact"
            putExtra("phone", prettyPhone)
            launchActivityIntent(this)
        }
    }

    private fun launchActivityIntent(intent: Intent) {
        try {
            startActivity(context, intent, Bundle.EMPTY)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.add_error, Toast.LENGTH_LONG)
                .show()
        }
    }

    inner class HolderHistory(val v: ViewBinding) : RecyclerView.ViewHolder(v.root)

    private fun performSwipeAction(action: Action, item: CallHistoryItem) {
        when (action.type) {
            // Звонок делаем по тому телефону, который в истории, а не который в настройках контакта!
            Action.Type.PhoneCall ->
                Action(0, Action.Type.PhoneCall, item.phone, "")
                    .perform(context)
            Action.Type.WhatsAppChat ->
                // если контакт не записан, вызываем чат по номеру а не по id
                Action(action.id, Action.Type.WhatsAppChat, item.phone, "")
                    .perform(context)
            else -> action.perform(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showPopup(view: View, call: CallHistoryItem) {
        popupCall = call
        notifyItemChanged(1 + calls.indexOf(popupCall))

        val popup = PopupMenu(view.context, view, Gravity.END)
        popup.inflate(R.menu.call_history_context_menu)

        if (!isMultiSim(view.context)) {
            popup.menu.removeGroup(R.id.call_sim_group)
        }

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.call_sim1 -> call(
                    call,
                    context.telecomManager.callCapablePhoneAccounts.first()
                )
                R.id.call_sim2 -> call(call, context.telecomManager.callCapablePhoneAccounts.last())
                R.id.copy_number -> copyNumber(call)
                R.id.delete_call_item -> onDeleteCallRecord(call)
                R.id.purge_contact_history -> if (null != call.contact) onPurgeContactHistory(call.contact)
                R.id.purge_call_history -> onPurgeCallHistory()
            }
            true
        }
        popup.setOnDismissListener {
            popupCall?.let {
                notifyItemChanged(1 + calls.indexOf(popupCall))
            }
            popupCall = null
        }

        popup.setForceShowIcon(true)
        popup.show()
        context.vibrator.vibrateSafety(2, 255)
    }

    @SuppressLint("MissingPermission")
    private fun call(item: CallHistoryItem, handle: PhoneAccountHandle) {
        val phone = Uri.fromParts("tel", item.phone, null)

        Bundle().apply {
            putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
            putBoolean(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, false)
            putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
        }.let { context.telecomManager.placeCall(phone, it) }
    }

    private fun copyNumber(call: CallHistoryItem) {
        val myClipboard =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip: ClipData = ClipData.newPlainText("simple text", call.phone)
        myClipboard?.setPrimaryClip(clip)
        Toast.makeText(context, R.string.copied, Toast.LENGTH_LONG)
            .show()
        Analytics.logKeyboardCopy()
    }

    private fun isMultiSim(context: Context): Boolean =
        PackageManager.PERMISSION_GRANTED == checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        )
            && context.telecomManager.callCapablePhoneAccounts.count() >= 2
}

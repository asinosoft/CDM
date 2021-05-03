package com.asinosoft.cdm.detail_contact

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.asinosoft.cdm.Loader
import com.asinosoft.cdm.Metoths.Companion.Direction
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.CallHistoryRepositoryImpl
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions

/**
 * Данные для окна Просмотр контакта
 */
class DetailHistoryViewModel : ViewModel() {
    private lateinit var contact: Contact
    private lateinit var actions: DirectActions

    val callHistory: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
    val availableActions: MutableLiveData<List<Action.Type>> = MutableLiveData()
    val directActions: MutableLiveData<DirectActions> = MutableLiveData()

    fun initialize(context: Context, contactId: Long, phoneNumber: String) {
        val contactRepository = ContactRepositoryImpl(context)
        contact = contactRepository.getContactById(contactId)
            ?: Contact(0, phoneNumber).apply {
                actions.add(Action(0, Action.Type.PhoneCall, phoneNumber, ""))
            }

        callHistory.value =
            if (0L != contact.id) {
                CallHistoryRepositoryImpl(contactRepository).getHistoryByContact(context, contact)
            } else {
                CallHistoryRepositoryImpl(contactRepository).getHistoryByPhone(
                    context,
                    contact.name
                )
            }

        actions = Loader.loadContactSettings(context, contact)
        directActions.value = actions
        availableActions.value = getAvailableActions()
    }

    fun getPhoneNumber() = contact.name

    fun getContact(): Contact = contact

    fun getContactPhoto(context: Context) = contact.getPhoto(context)

    private fun getContactAction(direction: Direction): Action {
        return when (direction) {
            Direction.LEFT -> actions.left
            Direction.RIGHT -> actions.right
            Direction.TOP -> actions.top
            Direction.DOWN -> actions.down
            else -> throw Exception("Unknown direction: $direction")
        }
    }

    fun setContactAction(direction: Direction, action: Action) {
        when (direction) {
            Direction.LEFT -> actions.left = action
            Direction.RIGHT -> actions.right = action
            Direction.TOP -> actions.top = action
            Direction.DOWN -> actions.down = action
            else -> throw Exception("Unknown direction: $direction")
        }
        directActions.value = actions
        availableActions.value = getAvailableActions()
    }

    fun swapContactAction(one: Direction, another: Direction) {
        getContactAction(one).apply {
            setContactAction(one, getContactAction(another)).also {
                setContactAction(another, this)
            }
        }
    }

    fun saveContactSettings(context: Context) {
        Loader.saveContactSettings(context, contact, actions)
    }

    fun getGlobalSettings(context: Context) = Loader.loadSettings(context)

    private fun getAvailableActions(): List<Action.Type> =
        contact.actions
            .filter {
                it != actions.left && it != actions.right && it != actions.top && it != actions.down
            }
            .map { it.type }.distinct()
}

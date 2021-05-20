package com.asinosoft.cdm.viewmodels

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.CallHistoryRepositoryImpl
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.api.Loader
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions
import com.asinosoft.cdm.helpers.Metoths.Companion.Direction
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Данные для окна Просмотр контакта
 */
class DetailHistoryViewModel : ViewModel() {
    private lateinit var contact: Contact
    private lateinit var actions: DirectActions

    val callHistory: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
    val availableActions: MutableLiveData<List<Action.Type>> = MutableLiveData()
    val directActions: MutableLiveData<DirectActions> = MutableLiveData()

    fun initialize(context: Context, phoneNumber: String) {
        contact = Contact(0, phoneNumber)

        val contactRepository = ContactRepositoryImpl(context)
        callHistory.value = CallHistoryRepositoryImpl(contactRepository).getHistoryByPhone(
            context,
            contact.name
        )
    }

    fun initialize(context: Context, contactId: Long) {
        contact = ContactRepositoryImpl(context).getContactById(contactId)!!

        val contactRepository = ContactRepositoryImpl(context)
        callHistory.value =
            CallHistoryRepositoryImpl(contactRepository).getHistoryByContact(context, contact)

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
        Firebase.analytics.logEvent(
            "contact_set_action",
            Bundle().apply {
                putString("direction", direction.name)
                putString("action", action.type.name)
            }
        )
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

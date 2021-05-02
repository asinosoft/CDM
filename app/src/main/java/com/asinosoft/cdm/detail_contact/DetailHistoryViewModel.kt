package com.asinosoft.cdm.detail_contact

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.asinosoft.cdm.App
import com.asinosoft.cdm.Loader
import com.asinosoft.cdm.Metoths.Companion.Direction
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions

/**
 * Данные для окна Просмотр контакта
 */
class DetailHistoryViewModel : ViewModel() {
    private lateinit var contact: Contact
    private lateinit var actions: DirectActions

    val callHistory: MutableLiveData<List<CallHistoryItem>> by lazy {
        MutableLiveData<List<CallHistoryItem>>()
    }

    fun initialize(contact: Contact) {
        this.contact = contact
        this.actions = contact.directActions
        callHistory.value =
            if (0L != contact.id) App.callHistoryRepository.getHistoryByContactId(contact.id)
            else App.callHistoryRepository.getHistoryByPhone(contact.name)
    }

    fun getPhoneNumber() = contact.name

    fun getContact(): Contact = contact

    fun getAvailableActions(): List<Action.Type> = contact.actions.map { it.type }.distinct()

    fun getContactPhoto() = contact.getPhoto()

    fun getContactActions(): DirectActions = actions

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
    }

    fun swapContactAction(one: Direction, another: Direction) {
        getContactAction(one).apply {
            setContactAction(one, getContactAction(another)).also {
                setContactAction(another, this)
            }
        }
    }

    fun saveContactSettings() {
        Loader.saveContactSettings(contact, actions)
    }

    fun getGlobalSettings() = Loader.loadSettings()
}

package com.asinosoft.cdm.viewmodels

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Данные для окна Просмотр контакта
 */
class DetailHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var _contact: Contact
    private lateinit var _actions: DirectActions
    private var haveUnsavedChanges: Boolean = false

    val contact: MutableLiveData<Contact> = MutableLiveData()
    val callHistory: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
    val directActions: MutableLiveData<DirectActions> = MutableLiveData()
    val availableActions: MutableLiveData<List<Action.Type>> = MutableLiveData()

    fun initialize(context: Context, contactId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val contactRepository = ContactRepositoryImpl(context)

            contactRepository.getContactById(contactId)?.let {
                _contact = it
                _actions = Loader.loadContactSettings(context, it)

                contact.postValue(_contact)
                directActions.postValue(_actions)
                availableActions.postValue(getAvailableActions())
                haveUnsavedChanges = false

                val calls =
                    CallHistoryRepositoryImpl(contactRepository).getHistoryByContact(context, it)
                callHistory.postValue(calls)
            }
        }
    }

    fun getContactAction(direction: Direction): Action {
        return when (direction) {
            Direction.LEFT -> _actions.left
            Direction.RIGHT -> _actions.right
            Direction.TOP -> _actions.top
            Direction.DOWN -> _actions.down
            else -> throw Exception("Unknown direction: $direction")
        }
    }

    fun setContactAction(direction: Direction, action: Action) {
        Timber.d("set: %s → %s", direction, action.type)
        Firebase.analytics.logEvent(
            "contact_set_action",
            Bundle().apply {
                putString("direction", direction.name)
                putString("action", action.type.name)
            }
        )
        when (direction) {
            Direction.LEFT -> _actions.left = action
            Direction.RIGHT -> _actions.right = action
            Direction.TOP -> _actions.top = action
            Direction.DOWN -> _actions.down = action
            else -> throw Exception("Unknown direction: $direction")
        }
        haveUnsavedChanges = true
        directActions.postValue(_actions)
        availableActions.postValue(getAvailableActions())
    }

    fun swapContactAction(one: Direction, another: Direction) {
        Timber.d("swap: %s ↔ %s", one, another)
        getContactAction(one).apply {
            setContactAction(one, getContactAction(another)).also {
                setContactAction(another, this)
            }
        }
    }

    fun saveContactSettings(context: Context) {
        Timber.d("Сохранение настроек контакта %s", _contact)
        if (haveUnsavedChanges) {
            Loader.saveContactSettings(context, _contact, _actions)
            haveUnsavedChanges = false
        }
    }

    private fun getAvailableActions(): List<Action.Type> =
        _contact.actions
            .filter {
                it != _actions.left && it != _actions.right && it != _actions.top && it != _actions.down
            }
            .map { it.type }.distinct()
}

package com.asinosoft.cdm.viewmodels

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.asinosoft.cdm.App
import com.asinosoft.cdm.api.*
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions
import com.asinosoft.cdm.helpers.Keys.Companion.CALL_HISTORY_LIMIT
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.hasPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val config = App.instance.config

    private lateinit var _actions: DirectActions
    private var haveUnsavedChanges: Boolean = false

    var initialized = false
    val isBlocked: MutableLiveData<Boolean> = MutableLiveData()
    val calls: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
    val contacts: MutableLiveData<Collection<Contact>> = MutableLiveData()

    val contact: MutableLiveData<Contact?> = MutableLiveData()
    val contactHistory: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
    val contactActions: MutableLiveData<DirectActions> = MutableLiveData()
    val availableActions: MutableLiveData<List<Action.Type>> = MutableLiveData()

    private val contactRepository = ContactRepositoryImpl(getApplication())

    private val callsRepository = CallHistoryRepositoryImpl(contactRepository)

    fun refresh() {
        val hasAccess = hasAccessToCallLog()
        isBlocked.postValue(!hasAccess)
        if (hasAccess) {
            viewModelScope.launch(Dispatchers.IO) {
                getContacts()
                getCalls(calls.value)
                initialized = true
            }
        } else {
            initialized = true
        }
    }

    fun getMoreCalls() {
        val hasAccess = hasAccessToCallLog()
        isBlocked.postValue(!hasAccess)
        if (hasAccess) {
            viewModelScope.launch(Dispatchers.IO) { retrieveLatestCalls() }
        }
    }

    fun getPhoneCalls(phone: String): List<CallHistoryItem> {
        return callsRepository.getHistoryByPhone(
            getApplication(),
            phone
        )
    }

    fun getContactByUri(context: Context, uri: Uri?): Contact? {
        Timber.d("Поиск контакта: %s", uri)
        return uri?.let {
            val projections = arrayOf(ContactsContract.Contacts._ID)
            val cursor = context.contentResolver.query(uri, projections, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnId = cursor.getColumnIndex(projections[0])
                val id = cursor.getLong(columnId)
                cursor.close()
                return contactRepository.getContactById(id)
            } else {
                return null
            }
        }
    }

    /**
     * Изменение настроек действий контакта в соответствии с выбранным телефоном
     */
    fun setContactPhone(contact: Contact, phone: Action) {
        val settings = config.getContactSettings(contact)
        if (settings.top.type === phone.type) {
            settings.top = phone
        }
        if (settings.down.type === phone.type) {
            settings.down = phone
        }
        if (settings.left.type === phone.type) {
            settings.left = phone
        }
        if (settings.right.type === phone.type) {
            settings.right = phone
        }
        config.setContactSettings(contact, settings)
    }

    fun setContact(contactId: Long) {
        contact.value = null
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.getContactById(contactId)?.let {
                _actions = App.instance.config.getContactSettings(it)

                contact.postValue(it)
                contactActions.postValue(_actions)
                availableActions.postValue(getAvailableActions())
                haveUnsavedChanges = false

                getContactCalls(it)
            }
        }
    }

    fun setContactAction(direction: Metoths.Companion.Direction, action: Action) {
        Timber.d("set: %s → %s", direction, action.type)
        Analytics.logContactSetAction(direction.name, action.type.name)
        when (direction) {
            Metoths.Companion.Direction.LEFT -> _actions.left = action
            Metoths.Companion.Direction.RIGHT -> _actions.right = action
            Metoths.Companion.Direction.TOP -> _actions.top = action
            Metoths.Companion.Direction.DOWN -> _actions.down = action
            else -> throw Exception("Unknown direction: $direction")
        }
        haveUnsavedChanges = true
        contactActions.postValue(_actions)
        availableActions.postValue(getAvailableActions())
    }

    fun swapContactAction(one: Metoths.Companion.Direction, another: Metoths.Companion.Direction) {
        Timber.d("swap: %s ↔ %s", one, another)
        getContactAction(one).apply {
            setContactAction(one, getContactAction(another)).also {
                setContactAction(another, this)
            }
        }
    }

    fun saveContactSettings() {
        Timber.d("Сохранение настроек контакта %s", contact.value)
        if (haveUnsavedChanges) {
            contact.value?.let { App.instance.config.setContactSettings(it, _actions) }
            haveUnsavedChanges = false
        }
    }

    fun purgeCallHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            callsRepository.purgeCallHistory(getApplication())
            getCalls()
        }
    }

    fun purgeContactHistory(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            callsRepository.purgeContactHistory(getApplication(), contact)
            getCalls()
        }
    }

    fun deleteCallHistoryItem(call: CallHistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            callsRepository.deleteCallHistoryItem(getApplication(), call)
            call.contact?.let { getContactCalls(it) }
            getCalls()
        }
    }

    private fun hasAccessToCallLog(): Boolean =
        getApplication<Application>().hasPermissions(
            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG)
        )

    private fun getContacts() {
        contactRepository.initialize()

        contacts.postValue(contactRepository.getContacts())
    }

    private fun getCalls(callHistory: List<CallHistoryItem>? = null) {
        if (null == callHistory) {
            Timber.d("Первая загрузка истории звонков")
            val latestCalls = callsRepository.getLatestHistory(
                getApplication(),
                Date(),
                CALL_HISTORY_LIMIT,
                CallHistoryFilter()
            )
            Timber.d("Найдено %d звонков", latestCalls.size)
            calls.postValue(latestCalls)
        } else {
            Timber.d("Проверка новых звонков")
            Analytics.logLoadCallHistory()
            val newCalls = callsRepository.getNewestHistory(
                getApplication(),
                callHistory.firstOrNull()?.timestamp ?: Date()
            )
            Timber.d("Найдено %d звонков", newCalls.size)

            // Объединяем новую историю и старую, исключая из неё контакты, которые отметились в новой
            val newContacts = newCalls.map { it.contact }
            val oldCalls = callHistory.filter { !newContacts.contains(it.contact) }
            calls.postValue(newCalls + oldCalls)
        }
    }

    private fun retrieveLatestCalls() {
        Timber.d("Подгрузка старой истории звонков")
        val callHistory = calls.value ?: listOf()
        val oldestCalls = callsRepository.getLatestHistory(
            getApplication(),
            callHistory.lastOrNull()?.timestamp ?: Date(),
            CALL_HISTORY_LIMIT,
            CallHistoryFilter(callHistory.mapNotNull { it.contact })
        )
        Timber.d("Найдено %d звонков", oldestCalls.size)
        calls.postValue(callHistory + oldestCalls)
    }

    private fun getAvailableActions(): List<Action.Type> =
        contact.value
            ?.actions
            ?.filter {
                it != _actions.left && it != _actions.right && it != _actions.top && it != _actions.down
            }
            ?.map { it.type }
            ?.distinct()
            ?: listOf()

    private fun getContactAction(direction: Metoths.Companion.Direction): Action {
        return when (direction) {
            Metoths.Companion.Direction.LEFT -> _actions.left
            Metoths.Companion.Direction.RIGHT -> _actions.right
            Metoths.Companion.Direction.TOP -> _actions.top
            Metoths.Companion.Direction.DOWN -> _actions.down
            else -> throw Exception("Unknown direction: $direction")
        }
    }

    private fun getContactCalls(contact: Contact) {
        val calls = callsRepository.getHistoryByContact(getApplication(), contact)
        contactHistory.postValue(calls)
    }
}

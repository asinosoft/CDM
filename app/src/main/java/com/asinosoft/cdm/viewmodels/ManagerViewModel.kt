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
import com.asinosoft.cdm.api.* // ktlint-disable no-wildcard-imports
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.helpers.Keys.Companion.CALL_HISTORY_LIMIT
import com.asinosoft.cdm.helpers.hasPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.* // ktlint-disable no-wildcard-imports

class ManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val config = App.instance!!.config
    var initialized = false
    val isBlocked: MutableLiveData<Boolean> = MutableLiveData()
    val calls: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
    val contacts: MutableLiveData<Collection<Contact>> = MutableLiveData()

    private val contactRepository = ContactRepositoryImpl(getApplication())

    fun refresh() {
        val hasAccess = hasAccessToCallLog()
        isBlocked.postValue(!hasAccess)
        if (hasAccess) {
            viewModelScope.launch(Dispatchers.IO) {
                retrieveCallsAndContacts()
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
        return CallHistoryRepositoryImpl(contactRepository).getHistoryByPhone(
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
        when (phone.type) {
            settings.top.type -> settings.top = phone
            settings.down.type -> settings.down = phone
            settings.left.type -> settings.top = phone
            settings.right.type -> settings.down = phone
            else -> {}
        }
        config.setContactSettings(contact, settings)
    }

    private fun hasAccessToCallLog(): Boolean =
        getApplication<Application>().hasPermissions(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG
            )
        )

    private fun retrieveCallsAndContacts() {
        contactRepository.initialize()

        contacts.postValue(contactRepository.getContacts())

        val callHistory = calls.value
        if (null == callHistory) {
            Timber.d("Первая загрузка истории звонков")
            val latestCalls = CallHistoryRepositoryImpl(contactRepository).getLatestHistory(
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
            val newCalls = CallHistoryRepositoryImpl(contactRepository).getNewestHistory(
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
        val oldestCalls = CallHistoryRepositoryImpl(contactRepository).getLatestHistory(
            getApplication(),
            callHistory.lastOrNull()?.timestamp ?: Date(),
            CALL_HISTORY_LIMIT,
            CallHistoryFilter(callHistory.map { it.contact })
        )
        Timber.d("Найдено %d звонков", oldestCalls.size)
        calls.postValue(callHistory + oldestCalls)
    }
}

package com.asinosoft.cdm.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.asinosoft.cdm.api.*
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.helpers.Keys.Companion.CALL_HISTORY_LIMIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ManagerViewModel(application: Application) : AndroidViewModel(application) {
    val calls: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()
    val contacts: MutableLiveData<Collection<Contact>> = MutableLiveData()
    var settings: Settings = Loader.loadSettings(application)

    private val contactRepository = ContactRepositoryImpl(getApplication())

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            settings = Loader.loadSettings(getApplication())
            contactRepository.initialize()

            contacts.postValue(contactRepository.getContacts())

            val callHistory = calls.value
            if (null == callHistory) {
                Log.d(null, "Первая загрузка истории звонков")
                val latestCalls = CallHistoryRepositoryImpl(contactRepository).getLatestHistory(
                    getApplication(),
                    Date(),
                    CALL_HISTORY_LIMIT,
                    CallHistoryFilter()
                )
                Log.d(null, "Найдено ${latestCalls.size} звонков")
                calls.postValue(latestCalls)
            } else {
                Log.d(null, "Проверка новых звонков")
                val newCalls = CallHistoryRepositoryImpl(contactRepository).getNewestHistory(
                    getApplication(),
                    callHistory.firstOrNull()?.timestamp ?: Date()
                )
                Log.d(null, "Найдено ${newCalls.size} звонков")

                // Объединяем новую историю и старую, исключая из неё контакты, которые отметились в новой
                val newContacts = newCalls.map { it.contact }
                val oldCalls = callHistory.filter { !newContacts.contains(it.contact) }
                calls.postValue(newCalls + oldCalls)
            }
        }
    }

    fun getMoreCalls() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(null, "Подгрузка старой истории звонков")
            val callHistory = calls.value ?: listOf()
            val oldestCalls = CallHistoryRepositoryImpl(contactRepository).getLatestHistory(
                getApplication(),
                callHistory.lastOrNull()?.timestamp ?: Date(),
                CALL_HISTORY_LIMIT,
                CallHistoryFilter(callHistory.map { it.contact })
            )
            Log.d(null, "Найдено ${oldestCalls.size} звонков")
            calls.postValue(callHistory + oldestCalls)
        }
    }

    fun getPhoneCalls(phone: String): List<CallHistoryItem> {
        return CallHistoryRepositoryImpl(contactRepository).getHistoryByPhone(
            getApplication(),
            phone
        )
    }

    fun getContactByUri(context: Context, uri: Uri?): Contact? {
        Log.d(null, "Поиск контакта: $uri")
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
        val settings = Loader.loadContactSettings(getApplication(), contact)
        if (settings.top.type == phone.type) {
            settings.top = phone
        } else if (settings.down.type == phone.type) {
            settings.down = phone
        } else if (settings.left.type == phone.type) {
            settings.left = phone
        } else if (settings.right.type == phone.type) {
            settings.right = phone
        }
        Loader.saveContactSettings(getApplication(), contact, settings)
    }
}

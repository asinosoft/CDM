package com.asinosoft.cdm.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.asinosoft.cdm.helpers.Keys.Companion.CALL_HISTORY_LIMIT
import com.asinosoft.cdm.api.CallHistoryFilter
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.CallHistoryRepositoryImpl
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.data.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ManagerViewModel(application: Application) : AndroidViewModel(application) {
    val calls: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()

    private val contactRepository = ContactRepositoryImpl(getApplication())

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.initialize()

            val callHistory = calls.value
            if (null == callHistory) {
                Timber.d("Первая загрузка истории звонков")
                val latestCalls = CallHistoryRepositoryImpl(contactRepository).getLatestHistory(
                    getApplication(),
                    Date(),
                    CALL_HISTORY_LIMIT,
                    CallHistoryFilter()
                )
                Timber.d("Найдено %s звонков", latestCalls.size)
                calls.postValue(latestCalls)
            } else {
                Timber.d("Проверка новых звонков")
                val newCalls = CallHistoryRepositoryImpl(contactRepository).getNewestHistory(
                    getApplication(),
                    callHistory.firstOrNull()?.timestamp ?: Date()
                )
                Timber.d("Найдено %s звонков", newCalls.size)

                // Объединяем новую историю и старую, исключая из неё контакты, которые отметились в новой
                val newContacts = newCalls.map { it.contact }
                val oldCalls = callHistory.filter { !newContacts.contains(it.contact) }
                calls.postValue(newCalls + oldCalls)
            }
        }
    }

    fun getMoreCalls() {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("Подгрузка старой истории звонков")
            val callHistory = calls.value ?: listOf()
            val oldestCalls = CallHistoryRepositoryImpl(contactRepository).getLatestHistory(
                getApplication(),
                callHistory.lastOrNull()?.timestamp ?: Date(),
                CALL_HISTORY_LIMIT,
                CallHistoryFilter(callHistory.map { it.contact })
            )
            Timber.d("Найдено %s звонков", oldestCalls.size)
            calls.postValue(callHistory + oldestCalls)
        }
    }

    fun getContactByUri(context: Context, uri: Uri): Contact? {
        val projections = arrayOf(ContactsContract.Contacts._ID)
        val cursor = context.contentResolver.query(uri, projections, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnId = cursor.getColumnIndex(projections[0])
            val id = cursor.getLong(columnId)
            cursor.close()
            return contactRepository.getContactById(id)
        }
        return null
    }
}

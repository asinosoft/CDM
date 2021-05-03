package com.asinosoft.cdm

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.CallHistoryRepositoryImpl
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.data.Contact

class ManagerViewModel : ViewModel() {
    companion object {
        /**
         * Показываемое количество последних звонков
         */
        const val CALL_HISTORY_LIMIT = 40
    }

    private val latestCalls: MutableLiveData<List<CallHistoryItem>> = MutableLiveData()

    fun refresh(context: Context) {
        latestCalls.value =
            CallHistoryRepositoryImpl(ContactRepositoryImpl(context))
                .getLatestHistory(context)
                .take(CALL_HISTORY_LIMIT)
    }

    fun getLatestCalls() = latestCalls

    fun getContactByUri(context: Context, uri: Uri): Contact? =
        ContactRepositoryImpl(context).getContactByUri(uri)
}

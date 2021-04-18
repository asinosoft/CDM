package com.asinosoft.cdm.detail_contact

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.asinosoft.cdm.App
import com.asinosoft.cdm.Loader
import com.asinosoft.cdm.Settings
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.api.Contact
import com.asinosoft.cdm.data.PhoneItem

/**
 * Данные для окна Просмотр контакта
 */
class DetailHistoryViewModel() : ViewModel() {
    private var phoneNumber: String = ""
    private var contactId: Long = 0
    val callHistory: MutableLiveData<List<CallHistoryItem>> by lazy {
        MutableLiveData<List<CallHistoryItem>>()
    }

    fun initialize(phoneNumber: String, contactID: Long) {
        this.phoneNumber = phoneNumber
        this.contactId = contactID
        callHistory.value =
            if (0L != contactID) App.callHistoryRepository.getHistoryByContactId(contactID)
            else App.callHistoryRepository.getHistoryByPhone(phoneNumber)
    }

    fun getPhoneNumber() = phoneNumber

    fun getContact(): Contact =
        App.contactRepository.getContactById(contactId)
            ?: App.contactRepository.getContactByPhone(phoneNumber)
            ?: Contact(0, phoneNumber).apply {
                phones.add(
                    PhoneItem(phoneNumber)
                )
            }

    fun getContactPhoto() = getContact().getPhoto()

    fun getContactSettings(): Settings =
        Loader.loadContactSettings(phoneNumber)

    fun saveContactSettings(settings: Settings) =
        Loader.saveContactSettings(phoneNumber, settings)
}

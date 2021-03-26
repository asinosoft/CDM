package com.asinosoft.cdm.detail_contact

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.asinosoft.cdm.HistoryItem
import com.asinosoft.cdm.Loader
import com.asinosoft.cdm.Settings
import com.asinosoft.cdm.api.ContactRepository
import com.asinosoft.cdm.api.CursorApi

/**
 * Данные для окна Просмотр контакта
 */
class DetailHistoryViewModel() : ViewModel() {
    private val contactRepository = ContactRepository()

    private var phoneNumber: String = ""
    private var contactId: Long = 0
    val callHistory: MutableLiveData<List<HistoryItem>> by lazy {
        MutableLiveData<List<HistoryItem>>()
    }

    fun initialize(context: Context, phoneNumber: String, contactID: Long) {
        this.phoneNumber = phoneNumber
        this.contactId = contactID
        callHistory.value = CursorApi.getContactCallLog(context, phoneNumber)
    }

    fun getPhoneNumber() = phoneNumber

    fun getContact(): Contact =
        contactRepository.contacts[contactId]
            ?: contactRepository.contactPhones[phoneNumber]
            ?: Contact(0, phoneNumber).apply {
                mPhoneNumbers.add(phoneNumber)
                mPhoneTypes.add(-1)
            }

    fun getContactPhoto() = getContact().getPhoto()

    fun getContactSettings(): Settings =
        Loader.loadContactSettings(phoneNumber)

    fun saveContactSettings(settings: Settings) =
        Loader.saveContactSettings(phoneNumber, settings)

}

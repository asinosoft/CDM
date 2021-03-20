package com.asinosoft.cdm.detail_contact

import android.content.Context
import androidx.lifecycle.ViewModel
import com.asinosoft.cdm.HistoryItem
import com.asinosoft.cdm.Loader
import com.asinosoft.cdm.Settings
import com.asinosoft.cdm.api.ContactRepository
import com.asinosoft.cdm.api.CursorApi
import timber.log.Timber

/**
 * Данные для окна Просмотр контакта
 */
class DetailHistoryViewModel() : ViewModel() {
    private val contactRepository = ContactRepository()

    lateinit var phoneNumber: String
    var contactId: Long = 0

    fun getContact(): Contact =
        contactRepository.contacts[contactId]
            ?: contactRepository.contactPhones[phoneNumber]
            ?: Contact(0, phoneNumber).apply {
                mPhoneNumbers.add(phoneNumber)
                mPhoneTypes.add(-1)
            }

    fun getContactPhoto() = getContact()?.photo

    fun getContactSettings(): Settings =
        Loader.loadContactSettings(phoneNumber)

    fun getContactCallHistory(context: Context): List<HistoryItem> {
        Timber.d("[%s] getContactCallHistory %s", this, phoneNumber)
        return CursorApi.getContactCallLog(context, phoneNumber)
    }

    fun saveContactSettings(settings: Settings) =
        Loader.saveContactSettings(phoneNumber, settings)

}

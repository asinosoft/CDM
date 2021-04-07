package com.asinosoft.cdm.api

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import com.asinosoft.cdm.detail_contact.Contact
import com.asinosoft.cdm.detail_contact.StHelper
import timber.log.Timber

/**
 * Доступ к контактам
 */
class ContactRepositoryImpl(contentResolver: ContentResolver) : ContactRepository {

    override fun getContacts(): Collection<Contact> {
        return contacts.values
    }

    override fun getContactById(id: Long): Contact? {
        return contacts[id]
    }

    override fun getContactByPhone(phone: String): Contact? {
        return contactPhones[StHelper.convertNumber(phone)]
    }

    // Список колонок, получаемых из базы контактов
    private val projection = arrayOf(
        ContactsContract.Data._ID,
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.PHOTO_URI,
        ContactsContract.Data.DISPLAY_NAME,
        ContactsContract.Data.MIMETYPE,
        ContactsContract.Data.DATA1,
        ContactsContract.Data.DATA2,
        ContactsContract.Data.DATA3
    )

    // Полный список контактов
    private val contacts: Map<Long, Contact> by lazy {
        contentResolver.query(
            ContactsContract.Data.CONTENT_URI, projection,
            null, null, null
        )!!.let {
            Timber.d("Retrieve contact list")
            ContactCursorAdapter(it).getAll()
        }
    }

    // Индекс контактов по номеру телефона
    private val contactPhones: Map<String, Contact> by lazy {
        val index = HashMap<String, Contact>()
        contacts.values.forEach { contact ->
            contact.mPhoneNumbers.forEach { phone ->
                index[phone] = contact
            }
        }
        index
    }

    inner class ContactCursorAdapter(private val cursor: Cursor) {
        private val _id = cursor.getColumnIndex(ContactsContract.Data._ID)
        private val contactId = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        private val photoUri = cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI)
        private val displayName = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
        private val mimeType = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
        private val data1 = cursor.getColumnIndex(ContactsContract.Data.DATA1)
        private val data2 = cursor.getColumnIndex(ContactsContract.Data.DATA2)
        private val data3 = cursor.getColumnIndex(ContactsContract.Data.DATA3)

        fun getAll(): HashMap<Long, Contact> {
            val result = HashMap<Long, Contact>()

            while (cursor.moveToNext()) {
                val id = cursor.getLong(contactId)
                result.getOrPut(
                    id,
                    {
                        Contact(
                            id,
                            cursor.getStringOrNull(this@ContactCursorAdapter.displayName) ?: ""
                        )
                    }
                ).let { contact ->
                    contact.photoUri = cursor.getStringOrNull(this@ContactCursorAdapter.photoUri)
                    when (cursor.getString(mimeType)) {
                        Contact.MIME_TYPE_PHONE -> parsePhone(contact)
                        Contact.MIME_TYPE_E_MAIL -> parseEmail(contact)
                        Contact.MIME_TYPE_WHATSAPP_CALL -> parseWhatsAppCall(contact)
                        Contact.MIME_TYPE_WHATSAPP_VIDEO -> parseWhatsAppVideo(contact)
                        Contact.MIME_TYPE_VIBER_MSG -> parseViber(contact)
                        Contact.MIME_TYPE_TELEGRAM -> parseTelegram(contact)
                        Contact.MIME_TYPE_SKYPE -> parseSkype(contact)
                        Contact.MIME_TYPE_BIRTHDAY -> parseBirthday(contact)
                    }
                }
            }
            return result
        }

        private fun parsePhone(contact: Contact) {
            StHelper.convertNumber(cursor.getString(data1))?.let { phoneNumber ->
                if (!contact.mPhoneNumbers.contains(phoneNumber)) {
                    val phoneType = cursor.getInt(data2)
                    contact.mPhoneNumbers.add(phoneNumber)
                    contact.mPhoneTypes.add(phoneType)
                }
            }
        }

        private fun parseEmail(contact: Contact) {
            val emailAddress = cursor.getString(data1)
            val emailType = cursor.getInt(data2)
            if (!contact.mEmailAdress.contains(emailAddress)) {
                contact.mEmailAdress.add(emailAddress)
                contact.mEmailType.add(emailType)
            }
        }

        private fun parseWhatsAppCall(contact: Contact) {
            StHelper.convertNumber(cursor.getString(data1))?.let {
                if (!contact.mWhatsAppNumbers.contains(it)) {
                    contact.mWhatsAppNumbers.add(it)
                    contact.mWhatsAppCallId.add(cursor.getString(_id))
                }
            }
        }

        private fun parseWhatsAppVideo(contact: Contact) {
            StHelper.convertNumber(cursor.getString(data1))?.let {
                if (!contact.mWhatsAppNumbers2.contains(it)) {
                    contact.mWhatsAppNumbers2.add(it)
                    contact.mWhatsAppVideoId.add(cursor.getString(_id))
                }
            }
        }

        private fun parseViber(contact: Contact) {
            StHelper.convertNumber(cursor.getString(data1))?.let {
                if (!contact.mWhatsAppNumbers2.contains(it)) {
                    contact.mWhatsAppNumbers2.add(it)
                    contact.mWhatsAppVideoId.add(cursor.getString(_id))
                }
            }
        }

        private fun parseTelegram(contact: Contact) {
            StHelper.convertNumber(cursor.getString(data3))?.let {
                if (!contact.mTelegram.contains(it)) {
                    contact.mTelegram.add(it)
                    contact.mTelegramId.add(cursor.getString(_id))
                }
            }
        }

        private fun parseSkype(contact: Contact) {
            contact.mSkypeName = cursor.getString(data1)
        }

        private fun parseBirthday(contact: Contact) {
            val value = cursor.getString(data1)
            try {
                val date = StHelper.parseDateToddMMyyyy(value)
                val age = StHelper.parseToMillis(value)
                contact.mBirthDay.add(date!!)
                contact.mAge.add(age)
                contact.mBirthDayType.add(cursor.getInt(data2))
            } catch (ex: java.text.ParseException) {
                Timber.e(ex)
            }
        }
    }
}

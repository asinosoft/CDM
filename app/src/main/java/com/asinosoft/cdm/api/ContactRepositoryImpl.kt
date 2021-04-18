package com.asinosoft.cdm.api

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import com.asinosoft.cdm.data.*
import com.asinosoft.cdm.detail_contact.StHelper
import timber.log.Timber

/**
 * Доступ к контактам
 */
class ContactRepositoryImpl(private val contentResolver: ContentResolver) : ContactRepository {

    override fun initialize() {
        contacts = contentResolver.query(
            ContactsContract.Data.CONTENT_URI, projection,
            null, null, null
        )!!.let {
            Timber.d("Retrieve contact list")
            ContactCursorAdapter(it).getAll()
        }

        val index = HashMap<String, Contact>()
        contacts.values.forEach { contact ->
            contact.phones.forEach {
                index[it.value] = contact
            }
        }
        contactPhones = index

        initialized = true
    }

    override fun getContacts(): Collection<Contact> {
        if (!initialized) initialize()
        return contacts.values
    }

    override fun getContactById(id: Long): Contact? {
        if (!initialized) initialize()
        return contacts[id]
    }

    override fun getContactByUri(uri: Uri): Contact? {
        val projections = arrayOf(ContactsContract.Contacts._ID)
        val cursor = contentResolver.query(uri, projections, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnId = cursor.getColumnIndex(projections[0])
            val id = cursor.getLong(columnId)
            cursor.close()
            return getContactById(id)
        }
        return null
    }

    override fun getContactByPhone(phone: String): Contact? {
        if (!initialized) initialize()
        return contactPhones[phone]
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
        ContactsContract.Data.DATA3,
        ContactsContract.Data.DATA4,
    )

    private var initialized = false

    // Полный список контактов
    private lateinit var contacts: Map<Long, Contact>

    // Индекс контактов по номеру телефона
    private lateinit var contactPhones: Map<String, Contact>

    inner class ContactCursorAdapter(private val cursor: Cursor) {
        private val _id = cursor.getColumnIndex(ContactsContract.Data._ID)
        private val contactId = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        private val photoUri = cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI)
        private val displayName = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
        private val mimeType = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
        private val data1 = cursor.getColumnIndex(ContactsContract.Data.DATA1)
        private val data2 = cursor.getColumnIndex(ContactsContract.Data.DATA2)
        private val data3 = cursor.getColumnIndex(ContactsContract.Data.DATA3)
        private val data4 = cursor.getColumnIndex(ContactsContract.Data.DATA4)

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
                    when (ContactItem.Type.mimeToType(cursor.getString(mimeType))) {
                        ContactItem.Type.BIRTHDAY -> parseBirthday(contact)
                        ContactItem.Type.PHONE -> parsePhone(contact)
                        ContactItem.Type.EMAIL -> parseEmail(contact)
                        ContactItem.Type.SKYPE -> parseSkype(contact)
                        ContactItem.Type.TELEGRAM -> parseTelegram(contact)
                        ContactItem.Type.VIBER -> parseViber(contact)
                        ContactItem.Type.WHATSAPP -> parseWhatsapp(contact)
                    }
                }
            }
            cursor.close()
            return result
        }

        private fun parsePhone(contact: Contact) {
            val rawNumber = cursor.getStringOrNull(data4) ?: cursor.getString(data1)
            if (null == contact.phones.find { it.value == rawNumber }) {
                PhoneItem(rawNumber, cursor.getInt(data2)).let {
                    contact.phones.add(it)
                }
            }
        }

        private fun parseEmail(contact: Contact) {
            val emailAddress = cursor.getString(data1)
            if (null == contact.emails.find { it.value == emailAddress }) {
                EmailItem(cursor.getInt(data2), emailAddress).let {
                    contact.emails.add(it)
                }
            }
        }

        private fun parseWhatsapp(contact: Contact) {
            StHelper.convertNumber(cursor.getString(data1))?.let { whatsappNumber ->
                var whatsapp = contact.whatsapps.find { it.value == whatsappNumber }
                if (null == whatsapp) {
                    whatsapp = WhatsAppItem(whatsappNumber).also {
                        contact.whatsapps.add(it)
                    }
                }

                when (cursor.getString(mimeType)) {
                    "vnd.android.cursor.item/vnd.com.whatsapp.profile" -> {
                        whatsapp.chatId = cursor.getString(_id)
                    }
                    "vnd.android.cursor.item/vnd.com.whatsapp.voip.call" -> {
                        whatsapp.audioId = cursor.getString(_id)
                    }
                    "vnd.android.cursor.item/vnd.com.whatsapp.video.call" -> {
                        whatsapp.videoId = cursor.getString(_id)
                    }
                }
            }
        }

        private fun parseViber(contact: Contact) {
            StHelper.convertNumber(cursor.getString(data1))?.let { viberNumber ->
                if (null == contact.vibers.find { it.value == viberNumber }) {
                    ViberItem(viberNumber, cursor.getString(_id)).let {
                        contact.vibers.add(it)
                    }
                }
            }
        }

        private fun parseTelegram(contact: Contact) {
            StHelper.convertNumber(cursor.getString(data3))?.let { telegramNumber ->
                if (null == contact.telegrams.find { it.value == telegramNumber }) {
                    TelegramItem(telegramNumber, cursor.getString(_id)).let {
                        contact.telegrams.add(it)
                    }
                }
            }
        }

        private fun parseSkype(contact: Contact) {
            val skypeLogin = cursor.getString(data1)
            if (null == contact.skypes.find { it.value == skypeLogin }) {
                SkypeItem(skypeLogin).let {
                    contact.skypes.add(it)
                }
            }
        }

        private fun parseBirthday(contact: Contact) {
            val date = cursor.getString(data1)

            StHelper.parseDateToddMMyyyy(date)?.let { dateDMY ->
                val age = StHelper.parseToMillis(date)
                BirthdayItem(dateDMY, age).let {
                    contact.birthday = it
                }
            }
        }
    }
}

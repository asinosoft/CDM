package com.asinosoft.cdm.api

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.core.database.getStringOrNull
import com.asinosoft.cdm.data.*
import com.asinosoft.cdm.detail_contact.StHelper
import timber.log.Timber

/**
 * Доступ к контактам
 */
class ContactRepositoryImpl(private val context: Context) : ContactRepository {

    // Полный список контактов
    private var contacts: MutableMap<Long, Contact> = mutableMapOf()

    // Индекс контактов по номеру телефона
    private var contactPhones: MutableMap<String, Contact> = mutableMapOf()

    fun initialize() {
        Timber.d("Initialize contact repository")
        contactPhones = mutableMapOf()
        contacts = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI, projection,
            null, null, null
        )!!.let {
            ContactCursorAdapter(it).getAll()
        }

        val index = HashMap<String, Contact>()
        contacts.values.forEach { contact ->
            contact.phones.forEach {
                index[it.value] = contact
            }
        }

        contactPhones = index
        Timber.d("%s contacts found", contacts.size)
    }

    override fun getContacts(): Collection<Contact> {
        return contacts.values
    }

    override fun getContactById(id: Long): Contact? {
        return contacts[id] ?: findContactById(id)?.also { cache(it) }
    }

    override fun getContactByUri(uri: Uri): Contact? {
        val projections = arrayOf(ContactsContract.Contacts._ID)
        val cursor = context.contentResolver.query(uri, projections, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnId = cursor.getColumnIndex(projections[0])
            val id = cursor.getLong(columnId)
            cursor.close()
            return getContactById(id)
        }
        return null
    }

    override fun getContactByPhone(phone: String): Contact? {
        return contactPhones[phone] ?: findContactByPhone(phone)?.also { cache(it) }
    }

    private fun cache(contact: Contact) {
        this.contacts[contact.id] = contact
        contact.phones.forEach {
            contactPhones[it.value] = contact
        }
    }

    private fun findContactById(id: Long): Contact? {
        Timber.d("Find contact by ID %s", id)
        return context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI, projection,
            "${ContactsContract.Data.CONTACT_ID} = ?", arrayOf(id.toString()), null
        )?.use { cursor ->
            ContactCursorAdapter(cursor).getAll()[id]
        }
    }

    private fun findContactByPhone(phone: String): Contact? {
        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI, arrayOf(ContactsContract.Data.CONTACT_ID),
            "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.Data.DATA4} = ?",
            arrayOf("vnd.android.cursor.item/phone_v2", phone),
            null
        )?.use { cursor ->
            if (cursor.moveToNext()) {
                val column = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                val contactId = cursor.getLong(column)
                return getContactById(contactId)
            }
        }
        return null
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
                    val mime = cursor.getString(mimeType).dropWhile { c -> c != '/' }
                    when (mime) {
                        "/contact_event" -> parseBirthday(contact)
                        "/phone_v2" -> parsePhone(contact)
                        "/email_v2" -> parseEmail(contact)
                        "/com.skype4life.message" -> parseAction(contact, Action.Type.SkypeChat)
                        "/com.skype4life.phone" -> parseAction(contact, Action.Type.SkypeCall)
                        "/vnd.org.telegram.messenger.android.profile" -> parseAction(
                            contact,
                            Action.Type.TelegramChat
                        )
                        "/vnd.com.viber.voip.viber_number_message" -> parseAction(
                            contact,
                            Action.Type.ViberChat
                        )
                        "/vnd.com.viber.voip.viber_number_call" -> parseAction(
                            contact,
                            Action.Type.ViberCall
                        )
                        "/vnd.com.whatsapp.profile" -> parseAction(
                            contact,
                            Action.Type.WhatsAppChat
                        )
                        "/vnd.com.whatsapp.voip.call" -> parseAction(
                            contact,
                            Action.Type.WhatsAppCall
                        )
                        "/vnd.com.whatsapp.video.call" -> parseAction(
                            contact,
                            Action.Type.WhatsAppVideo
                        )
                    }
                }
            }
            cursor.close()
            return result
        }

        private fun parseBirthday(contact: Contact) {
            val date = cursor.getString(data1)

            StHelper.parseDateToddMMyyyy(date)?.let { dateDMY ->
                contact.age = StHelper.parseToMillis(date)
                contact.birthday = dateDMY
            }
        }

        private fun parsePhone(contact: Contact) {
            val id = cursor.getInt(_id)
            val number = cursor.getStringOrNull(data4) ?: cursor.getString(data1)
            val description =
                context.resources.getString(Phone.getTypeLabelResource(cursor.getInt(data2)))

            contact.actions.add(
                Action(id, Action.Type.PhoneCall, number, description)
            )
            contact.actions.add(
                Action(id, Action.Type.Sms, number, description)
            )
        }

        private fun parseEmail(contact: Contact) {
            val id = cursor.getInt(_id)
            val emailAddress = cursor.getString(data1)
            val description =
                context.resources.getString(Email.getTypeLabelResource(cursor.getInt(data2)))

            contact.actions.add(Action(id, Action.Type.Email, emailAddress, description))
        }

        private fun parseAction(contact: Contact, type: Action.Type) {
            val id = cursor.getInt(_id)
            val value = when (type.group) {
                Action.Group.Telegram -> StHelper.convertNumber(cursor.getString(data3))
                Action.Group.WhatsApp -> StHelper.convertNumber(cursor.getString(data1))
                else -> cursor.getString(data1)
            }
            val description = cursor.getString(data2) ?: type.name

            contact.actions.add(Action(id, type, value, description))
        }
    }
}

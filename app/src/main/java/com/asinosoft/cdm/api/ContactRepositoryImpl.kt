package com.asinosoft.cdm.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.core.database.getStringOrNull
import com.asinosoft.cdm.App
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.helpers.StHelper
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Доступ к контактам
 */
class ContactRepositoryImpl(private val context: Context) : ContactRepository {
    // Полный список контактов
    private var contacts: MutableMap<Long, Contact> = mutableMapOf()

    // Индекс контактов по номеру телефона
    private var contactPhones: MutableMap<String, Contact> = mutableMapOf()

    private var contactActions: MutableMap<Long, Set<Action>> = mutableMapOf()

    fun initialize() {
        Timber.d("Чтение списка контактов")
        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI, projection,
            null, null, null
        )!!.use {
            readAll(it)
        }

        Analytics.logContacts(contacts.values)

        Timber.d("Найдено %d контактов", contacts.size)
    }

    override fun getContacts(): Collection<Contact> {
        return contacts.values
    }

    override fun getContactById(id: Long): Contact? {
        return contacts[id] ?: findContactById(id)
    }

    override fun getContactByPhone(phone: String): Contact? {
        return contactPhones[phone] ?: findContactByPhone(phone)
    }

    override fun getContactByUri(uri: Uri): Contact? {
        Timber.d("Поиск контакта по URI $uri")
        if (PackageManager.PERMISSION_DENIED == context.checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            return null
        }

        val projections = arrayOf(ContactsContract.Contacts._ID)
        return context.contentResolver.query(uri, projections, null, null, null)?.use { cursor ->
            return if (cursor.moveToFirst()) {
                val columnId = cursor.getColumnIndex(projections[0])
                val id = cursor.getLong(columnId)
                getContactById(id)
            } else null
        }
    }

    override fun getContactActions(contactId: Long): Collection<Action>? =
        contactActions[contactId]

    private fun readAll(cursor: Cursor) {
        val adapter = ContactCursorAdapter(cursor)
        contacts.putAll(adapter.contacts)
        contactActions.putAll(adapter.contactActions)

        val db = App.instance.database.contacts()
        contacts.forEach { contact ->
            db.upsert(contact.value)
            contactActions[contact.key]?.let { actions ->
                actions.find { a -> a.type === Action.Type.PhoneCall }?.let { phone ->
                    contactPhones[phone.value] = contact.value
                }
            }
        }
    }

    private fun findContactById(id: Long): Contact? {
        Timber.d("Поиск контакта по ID %d", id)
        if (PackageManager.PERMISSION_DENIED == context.checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            return null
        }

        return context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI, projection,
            "${ContactsContract.Data.CONTACT_ID} = ?", arrayOf(id.toString()), null
        )?.use {
            readAll(it)
            contacts[id]
        }
    }

    private fun findContactByPhone(phone: String): Contact? {
        Timber.d("Поиск контакта по телефону $phone")
        if (PackageManager.PERMISSION_DENIED == context.checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
            return null
        }

        return context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI, arrayOf(ContactsContract.Data.CONTACT_ID),
            "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.Data.DATA4} = ?",
            arrayOf("vnd.android.cursor.item/phone_v2", phone),
            null
        )?.use {
            readAll(it)
            contactPhones[phone]
        }
    }

    // Список колонок, получаемых из базы контактов
    private val projection = arrayOf(
        ContactsContract.Data._ID,
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.PHOTO_URI,
        ContactsContract.Data.DISPLAY_NAME,
        ContactsContract.Data.MIMETYPE,
        ContactsContract.Data.STARRED,
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
        private val starred = cursor.getColumnIndex(ContactsContract.Data.STARRED)
        private val data1 = cursor.getColumnIndex(ContactsContract.Data.DATA1)
        private val data2 = cursor.getColumnIndex(ContactsContract.Data.DATA2)
        private val data3 = cursor.getColumnIndex(ContactsContract.Data.DATA3)
        private val data4 = cursor.getColumnIndex(ContactsContract.Data.DATA4)

        val contacts = HashMap<Long, Contact>()
        val contactActions = HashMap<Long, MutableSet<Action>>()

        init {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(contactId)
                val name = cursor.getStringOrNull(displayName) ?: ""
                val photo = cursor.getStringOrNull(photoUri)

                contacts.getOrPut(id) {
                    Contact(id, name, null, photo, 1 == cursor.getInt(starred))
                }.let { contact ->
                    when (cursor.getString(mimeType).dropWhile { c -> c != '/' }) {
                        "/contact_event" -> parseBirthday(contact)
                        "/phone_v2" -> parsePhone(contact)
                        "/email_v2" -> parseEmail(contact)
                        "/com.skype4life.phone" -> {
                            parseAction(contact, Action.Type.SkypeChat)
                            parseAction(contact, Action.Type.SkypeCall)
                        }
                        "/vnd.org.telegram.messenger.android.profile" -> parseAction(
                            contact,
                            Action.Type.TelegramChat
                        )
                        "/vnd.org.telegram.messenger.android.call" -> parseAction(
                            contact,
                            Action.Type.TelegramCall
                        )
                        "/vnd.org.telegram.messenger.android.call.video" -> parseAction(
                            contact,
                            Action.Type.TelegramVideo
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
        }

        private fun parseBirthday(contact: Contact) {
            val date = cursor.getString(data1)
            contact.birthday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
        }

        private fun parsePhone(contact: Contact) {
            val id = cursor.getInt(_id)
            val number = cursor.getStringOrNull(data4) ?: cursor.getString(data1)
            val description =
                context.resources.getString(Phone.getTypeLabelResource(cursor.getInt(data2)))

            contactActions.getOrPut(contact.id) { mutableSetOf() }
                .add(Action(id, Action.Type.PhoneCall, number, description))

            contactActions.getOrPut(contact.id) { mutableSetOf() }
                .add(Action(id, Action.Type.Sms, number, description))
        }

        private fun parseEmail(contact: Contact) {
            val id = cursor.getInt(_id)
            val emailAddress = cursor.getString(data1)
            val description =
                context.resources.getString(Email.getTypeLabelResource(cursor.getInt(data2)))

            contactActions.getOrPut(contact.id) { mutableSetOf() }
                .add(Action(id, Action.Type.Email, emailAddress, description))
        }

        private fun parseAction(contact: Contact, type: Action.Type) {
            val id = cursor.getInt(_id)
            val value = when (type.group) {
                Action.Group.Telegram -> StHelper.convertNumber(cursor.getString(data3))
                Action.Group.WhatsApp -> StHelper.convertNumber(cursor.getString(data1))
                else -> cursor.getString(data1)
            }
            val description = cursor.getString(data2) ?: type.name

            contactActions.getOrPut(contact.id) { mutableSetOf() }
                .add(Action(id, type, value, description))
        }
    }
}

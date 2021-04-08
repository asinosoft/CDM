package com.asinosoft.cdm.api

import android.content.ContentResolver
import android.provider.ContactsContract
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ContactRepositoryImplTest {
    /* TODO: найти способ полной проверки данных:
    init {
        Assert.assertEquals(
            listOf(
                Contact(41L, "Иванов").apply { ... },
                Contact(42L, "Петров"),
                Contact(43L, "Сидоров"),
            )
            ContactRepositoryImpl(mockValidData()).getContacts()
        )
    }
    */
    private lateinit var contactRepository: ContactRepository

    @Before
    fun setUp() {
        contactRepository = ContactRepositoryImpl(mockValidData())
    }

    @Test
    fun `all contact should be grouped by contactId`() {
        val contacts = contactRepository.getContacts()
        Assert.assertEquals(3, contacts.size)
        contacts.forEach { contact ->
            when (contact.id) {
                41L -> {
                    Assert.assertEquals("Иванов", contact.name)
                    Assert.assertEquals(listOf("+71234567890"), contact.phones.map { it.value })
                }
                42L -> {
                    Assert.assertEquals("Петров", contact.name)
                    Assert.assertEquals(
                        listOf("+70987654321", "+79876543210"),
                        contact.phones.map { it.value }
                    )
                }
                43L -> {
                    Assert.assertEquals("Сидоров", contact.name)
                    Assert.assertTrue(contact.phones.isEmpty())
                }
            }
        }
    }

    @Test
    fun `telegram contacts should contain phone numbers`() {
        val contacts = contactRepository.getContacts()
            .filter { contact -> !contact.telegrams.isEmpty() }

        Assert.assertEquals(1, contacts.size)
        contacts.forEach {
            when (it.id) {
                41L -> {
                    Assert.assertEquals(1, it.telegrams.size)
                    Assert.assertEquals("+7 777 666 5555", it.telegrams[0].value)
                }
            }
        }
    }

    private fun mockValidData(): ContentResolver {
        val projection = listOf(
            ContactsContract.Data._ID,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.PHOTO_URI,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.DATA2,
            ContactsContract.Data.DATA3
        )

        val contentResolver = mock(ContentResolver::class.java)
        `when`(contentResolver.query(any(), any(), any(), any(), any()))
            .thenReturn(ArrayCursor(projection, validData))

        return contentResolver
    }

    private val validData = listOf(
        // Контакт с одним телефоном
        listOf(11, 41L, null, "Иванов", "vnd.android.cursor.item/name", "Иванов", "Иванов"),
        listOf(13, 41L, null, "Иванов", "vnd.android.cursor.item/phone_v2", "+71234567890", 2),
        listOf(
            141,
            41L,
            null,
            "Иванов",
            "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile",
            218072587,
            "Telegram Profile",
            "Message +77776665555"
        ),
        // Контакт с двумя телефонами
        listOf(16, 42L, null, "Петров", "vnd.android.cursor.item/name", "Петров", "Петров"),
        listOf(17, 42L, null, "Петров", "vnd.android.cursor.item/phone_v2", "+70987654321", 2),
        listOf(12, 42L, null, "Петров", "vnd.android.cursor.item/phone_v2", "+79876543210", 2),
        // Контакт без телефонов
        listOf(19, 43L, null, "Сидоров", "vnd.android.cursor.item/name", "Сидоров", "Сидоров")
    )
}

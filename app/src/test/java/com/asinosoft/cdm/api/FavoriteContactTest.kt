package com.asinosoft.cdm.api

import com.asinosoft.cdm.detail_contact.Contact
import org.junit.Assert
import org.junit.Test

class FavoriteContactTest {
    @Test
    fun favoriteContactToJson() {
        Assert.assertEquals(
            "{\"contactID\":77,\"selectedNumber\":\"+7-987-654-3210\"}",
            FavoriteContact(Contact(77, "John Doe"), "+7-987-654-3210").toJson()
        )
    }

    @Test
    fun jsonToFavoriteContact() {
        val contactRepository = ContactRepositoryMock()

        FavoriteContact.fromJson(
            "{\"contactID\":77,\"selectedNumber\":\"+7-987-654-3210\"}",
            contactRepository
        ).apply {
            Assert.assertEquals(1, contactRepository.requests.size)
            Assert.assertEquals("getContactById(77)", contactRepository.requests[0])
            Assert.assertEquals(77L, contact?.id)
            Assert.assertEquals("+7-987-654-3210", phone)
        }

        FavoriteContact.fromJson(
            "{}",
            contactRepository
        ).apply {
            Assert.assertEquals(2, contactRepository.requests.size)
            Assert.assertEquals("getContactById(0)", contactRepository.requests[1])
            Assert.assertNull(contact)
            Assert.assertNull(phone)
        }
    }

    inner class ContactRepositoryMock : ContactRepository {
        var requests = mutableListOf<String>()

        override fun getContacts(): Collection<Contact> {
            TODO("Not yet implemented")
        }

        override fun getContactById(id: Long): Contact? {
            requests.add("getContactById($id)")
            return when (id) {
                0L -> null
                77L -> Contact(77L, "John Doe")
                else -> error("Запрос неизвестного контакта (ID = $id)")
            }
        }

        override fun getContactByPhone(phone: String): Contact? {
            TODO("Not yet implemented")
        }
    }
}

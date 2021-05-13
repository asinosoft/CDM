package com.asinosoft.cdm.api

import com.asinosoft.cdm.data.Contact
import com.google.gson.Gson

/**
 * Избранный контакт
 */
class FavoriteContact(
    var contact: Contact? = null,
    var phone: String? = null
) {
    companion object {
        fun fromJson(json: String, contactRepository: ContactRepository): FavoriteContact {
            return Gson().fromJson(json, JsonData::class.java)?.let { data ->
                FavoriteContact(
                    if (0L == data.contactID) null else contactRepository.getContactById(data.contactID),
                    data.selectedNumber
                )
            } ?: FavoriteContact()
        }
    }

    fun toJson(): String {
        return Gson().toJson(
            JsonData(
                contact?.id ?: 0,
                phone
            )
        )
    }

    override fun toString(): String {
        return "[${contact?.id}] ${contact?.name} -> $phone"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FavoriteContact

        if (contact != other.contact) return false
        if (phone != other.phone) return false

        return true
    }

    /**
     * Вспомогательный класс для сохранения/восстановления настроек панели избранных контактов
     */
    inner class JsonData(
        var contactID: Long = 0,
        val selectedNumber: String? = null
    )
}

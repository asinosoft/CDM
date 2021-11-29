package com.asinosoft.cdm.data

import com.asinosoft.cdm.api.ContactRepository
import com.google.gson.Gson

/**
 * Избранный контакт
 */
data class FavoriteContact(
    var contact: Contact? = null
) {
    companion object {
        fun fromJson(json: String, contactRepository: ContactRepository): FavoriteContact {
            return Gson().fromJson(json, JsonData::class.java)?.let { data ->
                FavoriteContact(
                    if (0L == data.contactID) null else contactRepository.getContactById(data.contactID),
                )
            } ?: FavoriteContact()
        }
    }

    fun toJson(): String {
        return Gson().toJson(
            JsonData(
                contact?.id ?: 0
            )
        )
    }

    /**
     * Вспомогательный класс для сохранения/восстановления настроек панели избранных контактов
     */
    inner class JsonData(
        var contactID: Long = 0,
    )
}

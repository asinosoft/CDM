package com.asinosoft.cdm.api

import android.content.SharedPreferences
import com.asinosoft.cdm.CirPairData
import com.asinosoft.cdm.Keys
import com.google.gson.Gson
import java.util.Collections.swap

/**
 * Реализация списка избранных контактов, который хранится в настройках приложения
 * Все изменения списка сразу сохраняются в настройки
 */
class FavoriteContactRepositoryImpl(
    private val contactRepository: ContactRepository,
    private val sharedPreferences: SharedPreferences
) : FavoriteContactRepository {
    private val favoriteContacts: MutableList<FavoriteContact> by lazy {
        loadContacts()
    }

    override fun getContacts(): List<FavoriteContact> {
        return favoriteContacts
    }

    override fun addContact(contact: FavoriteContact) {
        favoriteContacts.add(contact)
        saveContacts()
    }

    override fun removeContact(position: Int) {
        favoriteContacts.removeAt(position)
        saveContacts()
    }

    override fun replaceContact(position: Int, contact: FavoriteContact) {
        favoriteContacts[position] = contact
        saveContacts()
    }

    override fun swapContacts(i: Int, j: Int) {
        swap(favoriteContacts, i, j)
        saveContacts()
    }

    private fun loadContacts(): MutableList<FavoriteContact> {
        sharedPreferences.getString(Keys.Cirs, null)?.let {
            val list = it.split("<end>").dropLast(1)
            val contacts = list.map {
                FavoriteContact.fromJson(it, contactRepository)
            }

            return if (contacts.isNotEmpty()) {
                contacts.toMutableList()
            } else {
                defaultContacts()
            }
        }

        return defaultContacts()
    }

    private fun saveContacts() {
        favoriteContacts.map {
            Gson().toJson(
                CirPairData(
                    it.contact?.id ?: 0,
                    selectedNumber = it.phone
                )
            )
        }.joinToString("<end>").let {
            sharedPreferences
                .edit()
                .putString(Keys.Cirs, it)
                .apply()
        }
    }

    /**
     * По умолчанию список избранных контактов состоит из 9 пустых слотов
     */
    private fun defaultContacts(): MutableList<FavoriteContact> {
        return generateSequence { FavoriteContact() }.take(9).toMutableList()
    }
}

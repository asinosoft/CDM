package com.asinosoft.cdm.api

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asinosoft.cdm.data.FavoriteContact
import com.asinosoft.cdm.helpers.Keys
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import java.util.Collections.swap

/**
 * Реализация списка избранных контактов, который хранится в настройках приложения
 * Все изменения списка сразу сохраняются в настройки
 */
class FavoriteContactRepositoryImpl(
    private val context: Context,
    private val contactRepository: ContactRepository
) : FavoriteContactRepository {
    private val favoriteContacts: MutableList<FavoriteContact> by lazy {
        loadContacts()
    }

    override fun getContacts(): List<FavoriteContact> {
        return favoriteContacts
    }

    override fun addContact(contact: FavoriteContact) {
        Firebase.analytics.logEvent("favorite_plus", Bundle.EMPTY)
        favoriteContacts.add(contact)
        saveContacts()
    }

    override fun removeContact(position: Int) {
        Firebase.analytics.logEvent(
            "favorite_remove",
            Bundle().apply { putInt("position", position) }
        )
        favoriteContacts.removeAt(position)
        saveContacts()
    }

    override fun replaceContact(position: Int, contact: FavoriteContact) {
        Firebase.analytics.logEvent(
            "favorite_add",
            Bundle().apply { putInt("position", position) }
        )
        favoriteContacts[position] = contact
        saveContacts()
    }

    override fun swapContacts(i: Int, j: Int) {
        Firebase.analytics.logEvent("favorite_swap", Bundle.EMPTY)
        swap(favoriteContacts, i, j)
        saveContacts()
    }

    private fun loadContacts(): MutableList<FavoriteContact> {
        val preferences = context.getSharedPreferences(
            Keys.ManagerPreference,
            AppCompatActivity.MODE_PRIVATE
        )
        preferences.getString(Keys.Cirs, null)?.let {
            val list = it.split("<end>")
            val contacts = list.map { json ->
                FavoriteContact.fromJson(json, contactRepository)
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
        favoriteContacts.joinToString("<end>") {
            it.toJson()
        }.let {
            val preferences =
                context.getSharedPreferences(Keys.ManagerPreference, Context.MODE_PRIVATE)
            preferences
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

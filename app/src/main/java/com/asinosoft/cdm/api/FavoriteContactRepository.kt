package com.asinosoft.cdm.api

import com.asinosoft.cdm.data.FavoriteContact

/**
 * Список избранных контактов, которые показываются на главном экране
 */
interface FavoriteContactRepository {
    /**
     * Получить список избранных контактов
     */
    fun getContacts(): List<FavoriteContact>

    /**
     * Добавить контакт в конец списка избранных
     */
    fun addContact(contact: FavoriteContact)

    /**
     * Удалить контакт из указанной позиции списка избранных
     */
    fun removeContact(position: Int)

    /**
     * Заменить контакт в указанной позиции списка избранных на другой
     */
    fun replaceContact(position: Int, contact: FavoriteContact)

    /**
     * Поменять местами два контакта в списке
     */
    fun swapContacts(i: Int, j: Int)
}

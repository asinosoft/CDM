package com.asinosoft.cdm.api

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.data.Contact
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock

class FavoriteContactRepositoryImplTest {
    private val mockContext = mock(Context::class.java)
    private val mockContactRepository = mock(ContactRepository::class.java)
    private val mockSharedPreferences = mock(SharedPreferences::class.java)
    private val mockEditor = mock(SharedPreferences.Editor::class.java)

    init {
        Mockito.`when`(mockContext.getSharedPreferences(Keys.ManagerPreference, AppCompatActivity.MODE_PRIVATE)).thenReturn(mockSharedPreferences)
        Mockito.`when`(mockContactRepository.getContactById(0)).thenReturn(null)
        Mockito.`when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)

        // По-умолчанию считаем, что в настройках хранится 3 пустых слота для избранных контактов
        Mockito.`when`(mockSharedPreferences.getString(Keys.Cirs, null))
            .thenReturn("{\"contactID\":0}<end>{\"contactID\":0}<end>{\"contactID\":0}")
    }

    @Test
    fun `если настроек нет, то список избранных должен состоять из 9 пустых слотов`() {
        val mockEmptyPreferences = mock(SharedPreferences::class.java)
        Mockito.`when`(mockEmptyPreferences.getString(Keys.Cirs, null)).thenReturn(null)

        val mockEmptyContext = mock(Context::class.java)
        Mockito.`when`(mockEmptyContext.getSharedPreferences(Keys.ManagerPreference, AppCompatActivity.MODE_PRIVATE)).thenReturn(mockEmptyPreferences)

        val favoriteContactRepository =
            FavoriteContactRepositoryImpl(mockEmptyContext, mockContactRepository)

        Assert.assertEquals(
            generateSequence { FavoriteContact() }.take(9).toList(),
            favoriteContactRepository.getContacts()
        )
    }

    @Test
    fun `при добавлении контакта он должен попасть в конец списка + список должен сохраниться в настройки`() {
        val favoriteContactRepository =
            FavoriteContactRepositoryImpl(mockContext, mockContactRepository)

        val addedContact = FavoriteContact(Contact(1, "John Doe"), "+71234567890")

        favoriteContactRepository.addContact(addedContact)

        Assert.assertEquals(
            generateSequence { FavoriteContact() }.take(3).plus(addedContact).toList(),
            favoriteContactRepository.getContacts()
        )

        Mockito.verify(mockEditor).putString(
            Keys.Cirs,
            generateSequence { "{\"contactID\":0}" }
                .take(3)
                .plus("{\"contactID\":1,\"selectedNumber\":\"+71234567890\"}")
                .joinToString("<end>")
        )
    }

    @Test
    fun `при замене контакта количество элементов в списке должно остаться прежним`() {
        val favoriteContactRepository =
            FavoriteContactRepositoryImpl(mockContext, mockContactRepository)

        val newContact = FavoriteContact(Contact(77, "John Doe"), "+71234567890")

        favoriteContactRepository.replaceContact(1, newContact)

        Assert.assertEquals(
            listOf(FavoriteContact(), newContact, FavoriteContact()),
            favoriteContactRepository.getContacts()
        )

        Mockito.verify(mockEditor).putString(
            Keys.Cirs,
            """{"contactID":0}<end>{"contactID":77,"selectedNumber":"+71234567890"}<end>{"contactID":0}"""
        )
    }

    @Test
    fun `при обмене контактов остальные должны остаться на своих местах`() {
        val favoriteContactRepository =
            FavoriteContactRepositoryImpl(mockContext, mockContactRepository)

        val alphaMale = FavoriteContact(Contact(22, "Alpha Male"))
        val betaTester = FavoriteContact(Contact(33, "Beta Tester"))
        val gammaRay = FavoriteContact(Contact(44, "Gamma Ray"))

        favoriteContactRepository.replaceContact(0, alphaMale)
        favoriteContactRepository.replaceContact(1, betaTester)
        favoriteContactRepository.replaceContact(2, gammaRay)

        Assert.assertEquals(
            listOf(alphaMale, betaTester, gammaRay),
            favoriteContactRepository.getContacts()
        )

        favoriteContactRepository.swapContacts(0, 2)

        Assert.assertEquals(
            listOf(gammaRay, betaTester, alphaMale),
            favoriteContactRepository.getContacts()
        )

        Mockito.verify(mockEditor).putString(
            Keys.Cirs,
            """{"contactID":44}<end>{"contactID":33}<end>{"contactID":22}"""
        )
    }
}

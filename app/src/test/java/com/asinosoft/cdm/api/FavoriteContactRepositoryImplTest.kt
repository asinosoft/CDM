package com.asinosoft.cdm.api

import android.content.SharedPreferences
import com.asinosoft.cdm.Keys
import com.asinosoft.cdm.detail_contact.Contact
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock

class FavoriteContactRepositoryImplTest {
    private val mockContactRepository = mock(ContactRepository::class.java)
    private val mockSharedPreferences = mock(SharedPreferences::class.java)
    private val mockEditor = mock(SharedPreferences.Editor::class.java)

    init {
        Mockito.`when`(mockContactRepository.getContactById(0)).thenReturn(Contact(0, ""))
        Mockito.`when`(mockSharedPreferences.getString(Keys.Cirs, "")).thenReturn("")
        Mockito.`when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        Mockito.`when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
    }

    @Test
    fun `если настроек нет, то список избранных должен состоять из 9 пустых слотов`() {
        val favoriteContactRepository =
            FavoriteContactRepositoryImpl(mockContactRepository, mockSharedPreferences)

        Assert.assertEquals(
            generateSequence { FavoriteContact() }.take(9).toList(),
            favoriteContactRepository.getContacts()
        )
    }

    @Test
    fun `при добавлении контакта он должен попасть в конец списка + список должен сохраниться в настройки`() {
        val favoriteContactRepository =
            FavoriteContactRepositoryImpl(mockContactRepository, mockSharedPreferences)

        val addedContact = FavoriteContact(Contact(1, "John Doe"), "+71234567890")

        favoriteContactRepository.addContact(addedContact)

        Assert.assertEquals(
            generateSequence { FavoriteContact() }.take(9).plus(addedContact).toList(),
            favoriteContactRepository.getContacts()
        )

        Mockito.verify(mockEditor).putString(
            Keys.Cirs,
            generateSequence { "{\"contactID\":0}" }
                .take(9)
                .plus("{\"contactID\":1,\"selectedNumber\":\"+71234567890\"}")
                .joinToString("<end>")
        )
    }
}

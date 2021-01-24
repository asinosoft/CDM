package com.asinosoft.cdm.globals

import android.content.Intent
import android.provider.ContactsContract
import com.asinosoft.cdm.App
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts

object Ut {
    fun getContactFromIntent( data: Intent?) : Contact? {
        val uri = data!!.data
        val projections = arrayOf(ContactsContract.Contacts._ID)
        val cursor = App.INSTANCE.contentResolver.query(uri!!, projections, null, null, null)
        var id = 0L
        if (cursor != null && cursor.moveToFirst()) {
            val i = cursor.getColumnIndex(projections[0])
            id = cursor.getLong(i)
        }
        cursor?.close()
        return Contacts.getQuery().whereEqualTo(Contact.Field.ContactId, id).find().first()
    }
}
package com.asinosoft.cdm.detail_contact

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log

class URIMaker {
    val stHelper = StHelper()

    fun getUriByNumber(
        context: Context,
        number: String?,
        uriFirsPart: String,
        mimeType: String
    ): Uri? {
        return try {
            var result: Uri? = null
            val numberFrom: String = stHelper.convertNumber(number!!)
            val resolver = context.contentResolver
            val cursor1 = resolver.query(
                ContactsContract.Data.CONTENT_URI,
                null,
                null, null,
                ContactsContract.Contacts.DISPLAY_NAME
            )
            if (null != cursor1) {
                cursor1.moveToFirst()
                while (cursor1.moveToNext()) {
                    val _id = cursor1.getString(
                        cursor1.getColumnIndex(
                            ContactsContract.Data._ID
                        )
                    )
                    val contactId = cursor1.getString(
                        cursor1.getColumnIndex(
                            ContactsContract.Data.CONTACT_ID
                        )
                    )
                    val displayName = cursor1.getString(
                        cursor1.getColumnIndex(
                            ContactsContract.Data.DISPLAY_NAME
                        )
                    )
                    val mimeType1 = cursor1.getString(
                        cursor1.getColumnIndex(
                            ContactsContract.Data.MIMETYPE
                        )
                    )
                    val data = cursor1.getString(
                        cursor1.getColumnIndex(
                            ContactsContract.Data.DATA1
                        )
                    )
                    if (mimeType == mimeType1) {
                        val numberData: String = stHelper.convertNumber(data)
                        if (numberFrom == numberData) {
                            Log.d(
                                "getUriByNumber", _id + " " + displayName + " " + mimeType1
                                        + " contact id = " + contactId + " data = " + data
                            )
                            result = Uri.parse(uriFirsPart + _id)
                            cursor1.moveToLast()
                        }
                    }
                }
                cursor1.close()
            }
            result
        } catch (e: Exception) {
            Log.e("myLog", "get uri by number fail", e)
            null
        }
    }
}
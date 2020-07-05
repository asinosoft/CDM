package com.asinosoft.cdm;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.IOException;
import java.io.InputStream;

/**
 * Класс с методами для работы парсера.
 */
public class Funcs {

    /**
     * Метод возвращающий идентификатор контакта по его номеру.
     * @param context контекст
     * @param number номер
     * @return id
     */
    public static String getContactID(Context context, String number){
        ContentResolver contentResolver = context.getContentResolver();
        String contactId = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] projection = new String[]{ContactsContract.Contacts._ID};

        Cursor cursor = null;
        try {
            cursor =
                    contentResolver.query(
                            uri,
                            projection,
                            null,
                            null,
                            null);
        }catch(Exception ignored){}
        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                if (contactId != null) break;
            }
            cursor.close();
        }
        return contactId;
    }

    /**
     * Класс возвращающий фото контакта по номеру
     * @param context контекст
     * @param number номер
     * @return фото
     */
    public static Drawable retrieveContactPhoto(Context context, String number) {
        ContentResolver contentResolver = context.getContentResolver();
        String contactId = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

        Cursor cursor = null;
        try {
            cursor =
                    contentResolver.query(
                            uri,
                            projection,
                            null,
                            null,
                            null);
        }catch(Exception ignored){}
        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }
            cursor.close();
        }

        if (contactId == null) return null;

        Bitmap photo = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.contact_unfoto);

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }

            if (inputStream != null)
            inputStream.close();

        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
        return new BitmapDrawable(photo);
    }

}

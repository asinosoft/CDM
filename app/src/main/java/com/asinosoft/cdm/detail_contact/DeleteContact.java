package com.asinosoft.cdm.detail_contact;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

public class DeleteContact {
    Context mContext;
    String mContactId;

    public static final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY};
    @SuppressLint("InlinedApi")
    public static final String SELECTION
            = ContactsContract.Contacts._ID + " = ?";

    public DeleteContact(Context context, String contactId){
        mContactId = contactId;
        mContext = context;
    }

    public void makeRequest(){
        Log.d("myLogDeleteContact", "-- delete contact -- with id = " + mContactId);
        Request request = new Request();
        request.execute();
    }

    private class Request extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver cr = mContext.getContentResolver();
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    PROJECTION,
                    SELECTION,
                    new String[]{mContactId},
                    null);
            if(null != cursor){
                try{
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        System.out.println("The uri is " + uri.toString());
                        cr.delete(uri, null, null);

                        cursor.moveToNext();
                    }
                }catch (Exception e){
                    Log.e("myLog_DeleteContact", e.getMessage());
                }finally {
                    cursor.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void data){
        }
    }
}

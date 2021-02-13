package com.asinosoft.cdm.detail_contact;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

public class VCardLoader {
    private Context mContext;
    private String mContactId;
    private String mName;
    public VCardLoader(Context context, String contactId, String name){
        mContext = context;
        mContactId = contactId;
        mName = name;
    }

    public void makeRequest(){
        RequestClass request = new RequestClass();
        request.execute();
    }

    private class RequestClass extends AsyncTask<Void, Void, Uri> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Uri doInBackground(Void... params) {
            Uri uri = null;
            try {
                Cursor cursor = mContext.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null,
                        ContactsContract.Contacts._ID + " =?",
                        new String[]{mContactId},
                        null);
                if(null != cursor){
                    cursor.moveToFirst();
                    String lookUpKey = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    if(null != lookUpKey){
                        uri = Uri.withAppendedPath(
                                ContactsContract.Contacts.CONTENT_VCARD_URI,
                                lookUpKey);
                    }
                    cursor.close();
                }
            }catch (Exception e){
                Log.e("myLog", "VCardLoader doInBackground" , e);
            }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            try {
                if (null != uri) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(ContactsContract.Contacts.CONTENT_VCARD_TYPE);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (null != mName)
                        intent.putExtra(Intent.EXTRA_SUBJECT, mName);
                    mContext.startActivity(intent);
                }
            }catch (Exception e){
                Log.e("myLog", "VCardLoader onPostExecute", e);
            }
        }
    }
}

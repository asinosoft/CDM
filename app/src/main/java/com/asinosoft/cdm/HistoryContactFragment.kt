package com.asinosoft.cdm.DetailContact

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Funcs
import com.asinosoft.cdm.Keys
import com.asinosoft.cdm.R

/**
 * Класс вкладки "Контакт" в детальной информации по элементу истории.
 */
class HistoryContactFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.history_contact_fragment, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val recyclerView = this.view!!.findViewById<RecyclerView>(R.id.recyclerViewForContact)
        var lim = LinearLayoutManager(this.context!!)
        lim.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = lim
//        recyclerView.adapter = AdapterContact(getContactInfo(getNumber(this.context!!)))

        getPhone(getNumber(this.context!!))

    }

    private fun getNumber(context: Context): String {
        val sharedpreferences = activity!!.getSharedPreferences(Keys.SharedNum, Context.MODE_PRIVATE)
        return sharedpreferences.getString("TAG_NUM", "")!!
    }

    private fun getContactInfo(number: String): ArrayList<ContactItem>{

        try {
            val list = ArrayList<ContactItem>()
            val listNumbers = ArrayList<String>()
            val id = Funcs.getContactID(this.context!!, number)
            val selection: String = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"
            val selectionArg = arrayOf(id)
            val cursor: Cursor =
                context!!.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                    selection,
                    selectionArg,
                    null)!!


            val selection1: String = ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?"
            val selectionArg1 = arrayOf(id)
            val cursor1: Cursor =
                context!!.contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.ADDRESS),
                    selection1,
                    selectionArg1,
                    null)!!

            var contactEmail: String? = null
            var contactEmailType: Int? = null
            var contactNumber: String? = null
            var contactNumberType: Int = 0
            var name: String = ""


            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactNumberType = cursor.getInt(0)
                    contactNumber = cursor.getString(1)
                    name = cursor.getString(2)
                }
            }

            if (cursor1 != null) {
                if (cursor1.moveToFirst()) {
                    contactEmail = cursor1.getString(1)
                    contactEmailType = cursor1.getInt(0)
                }
            }

            list.add(ContactItem(
                name = name,
                number = contactNumber,
                number2 = "",
                numberType = contactNumberType,
                email = contactEmail,
                emailType = contactEmailType,
                rawContactId = hasWhatsApp(number),
                whatsAppVideoCallId = getContactIdForWhatsAppVideoCall(name),
                whatsAppCallId = getContactIdForWhatsAppCall(name),
                viberUri = callViberUri(number),
                hasViber = hasViber(name),
                hasTelegram = getTelegram(number),
                telegramId = getTelegramId(name),
                skypeId = getSkypeId(number),
                skypeName = "?"))

            return list
        }catch (e: Exception){e.printStackTrace()}
        return arrayListOf()
    }

    private fun hasWhatsApp(number: String) : String? {
        val id = Funcs.getContactID(this.context!!, number)
        val selectionWhatsApp: String =  ContactsContract.Data.CONTACT_ID + " = ? AND account_type IN (?)"
        val selectionArgsWhatsApp = arrayOf(id, "com.whatsapp")
        val cursorWhatsApp: Cursor = context!!.contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID) ,selectionWhatsApp, selectionArgsWhatsApp, null)!!

        var rawContactId: String? = null

        if(cursorWhatsApp != null){
            if(cursorWhatsApp.moveToFirst()){
                rawContactId = cursorWhatsApp.getString(0)
            }
        }

        return rawContactId
    }

    private fun getContactIdForWhatsAppCall(name: String): Int{
        val cursor: Cursor = context!!.contentResolver.query(ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data._ID),
            ContactsContract.Data.DISPLAY_NAME + "=? and "+ContactsContract.Data.MIMETYPE+ "=?",
            arrayOf(name,"vnd.android.cursor.item/vnd.com.whatsapp.voip.call"),
            ContactsContract.Contacts.DISPLAY_NAME)!!

        var phoneContactId: Int = 0

        if(cursor.count > 0){
            if(cursor.moveToFirst()){
                phoneContactId = cursor.getInt(0)
            }
        }
        return phoneContactId
    }

    private fun getContactIdForWhatsAppVideoCall(name: String): Int{
        val cursor: Cursor = context!!.contentResolver.query(ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data._ID),
            ContactsContract.Data.DISPLAY_NAME + "=? and "+ContactsContract.Data.MIMETYPE+ "=?",
            arrayOf(name,"vnd.android.cursor.item/vnd.com.whatsapp.video.call"),
            ContactsContract.Contacts.DISPLAY_NAME)!!

        var phoneContactId: Int = 0

        if(cursor.count > 0){
            if(cursor.moveToFirst()){
                phoneContactId = cursor.getInt(0)
            }
        }
        return phoneContactId
    }

    private fun hasViber(name: String): Int? {
        val cursor: Cursor = context!!.contentResolver.query(ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data._ID),
            ContactsContract.Data.DISPLAY_NAME + "=? and "+ContactsContract.Data.MIMETYPE+ "=?",
            arrayOf(name,"vnd.android.cursor.item/vnd.com.viber.voip.viber_number_message"),
            ContactsContract.Contacts.DISPLAY_NAME)!!

        var contactId: Int = 0

        if(cursor.count > 0){
            if(cursor.moveToFirst()){
                contactId = cursor.getInt(0)
            }
        }
        return contactId
    }

    private fun callViberUri(number: String): Uri? {
        val id = Funcs.getContactID(this.context!!, number)
        var uri: Uri? = null
        if(!TextUtils.isEmpty(id)){
            val cursor: Cursor = context!!.contentResolver.query(ContactsContract.Data.CONTENT_URI, arrayOf(ContactsContract.Data._ID),
                ContactsContract.Data.DATA2+"=? AND " + ContactsContract.Data.CONTACT_ID + "=?", arrayOf("Viber", id), null)!!
            if(cursor != null){
                if(cursor.moveToFirst()){
                    val idUri: String = cursor.getString(0)
                    if(!TextUtils.isEmpty(idUri)){
                        uri = Uri.parse("content://com.android.contacts/data/$idUri")
                    }
                }
            }
        }
        return uri
    }

    private fun getTelegram(number: String): String? {

        val id = Funcs.getContactID(this.context!!, number)
        val selection: String =  ContactsContract.Data.CONTACT_ID + " = ? AND account_type IN (?)"
        val selectionArg = arrayOf(id, "org.telegram.messenger")
        val cursor: Cursor = context!!.contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID) ,selection, selectionArg, null)!!

        var contactId: String? = null

        if(cursor != null){
            if(cursor.moveToFirst()){
                contactId = cursor.getString(0)
            }
        }

        return contactId
    }

    private fun getTelegramId(name: String): String? {
        val cursor: Cursor = context!!.contentResolver.query(ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data._ID),
            ContactsContract.Data.DISPLAY_NAME + "=? and "+ContactsContract.Data.MIMETYPE+ "=?",
            arrayOf(name,"vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile"),
            ContactsContract.Contacts.DISPLAY_NAME)!!

        var contactId: String? = null

        if(cursor.count > 0){
            if(cursor.moveToFirst()){
                contactId = cursor.getString(0)
            }
        }
        return contactId
    }

    private fun getSkypeId(number: String): String? {
        val id = Funcs.getContactID(this.context!!, number)
        val selection: String =  ContactsContract.Data.CONTACT_ID + " = ? AND account_type IN (?)"
        val selectionArg = arrayOf(id, "com.skype.raider")
        val cursor: Cursor = context!!.contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID) ,selection, selectionArg, null)!!

        var contactId: String? = null

        if(cursor != null){
            if(cursor.moveToFirst()){
                contactId = cursor.getString(0)
            }
        }

        return contactId
    }

    private fun getPhone(number: String){

    }
}

package com.asinosoft.cdm.DetailContact

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Funcs
import com.asinosoft.cdm.Keys
import com.asinosoft.cdm.R


class ContactDetailFragment : Fragment() {

    val contact = Contact()

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.history_contact_fragment, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getInfoForContact()

        val recyclerView = this.view!!.findViewById<RecyclerView>(R.id.recyclerViewForContact)
        var lim = LinearLayoutManager(this.context!!)
        lim.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = lim
        recyclerView.adapter = AdapterContact(getContactListForDetail())
    }

    private fun getNumber(context: Context): String {
        val sharedpreferences = activity!!.getSharedPreferences(Keys.SharedNum, Context.MODE_PRIVATE)
        return sharedpreferences.getString("TAG_NUM", "")!!
    }

    fun getInfoForContact(){
        val id = Funcs.getContactID(this.context!!, getNumber(this.context!!))
        contact.parseDataCursor(id, this.context!!)
    }

    fun getContactListForDetail(): ArrayList<ContactDetailListElement>{

        var result = ArrayList<ContactDetailListElement>()
        for (i in contact.mPhoneNumbers.indices) {
            val numberStr: String = contact.mPhoneNumbers.get(i)
            val element = ContactDetailListElement(0, numberStr, contact.mPhoneTypes.get(i))
            result.add(element)
        }

        if(contact.mEmailAdress.size > 0){
            for(i in contact.mEmailAdress.indices){
                val eMail: String = contact.mEmailAdress[i]
                val element = ContactDetailListElement(1, eMail, contact.TYPE_EMAIL)
                result.add(element)
            }
        }

        if(contact.mWhatsAppNumbers.size > 0){
            for(i in contact.mWhatsAppNumbers.indices){
                val numberStr = contact.mWhatsAppNumbers[i]
                val element = ContactDetailListElement(2, numberStr, contact.TYPE_WHATSAPP, contact.mWhatsAppCallId[i])
                result.add(element)
            }
        }
        if(contact.mWhatsAppNumbers.size > 0){
            for(i in contact.mWhatsAppNumbers.indices){
                val numberStr = contact.mWhatsAppNumbers[i]
                val element = ContactDetailListElement(3, numberStr, contact.TYPE_WHATSAPP, contact.mWhatsAppCallId[i])
                result.add(element)
            }
        }
        if(contact.mWhatsAppNumbers2.size > 0){
            for(i in contact.mWhatsAppNumbers2.indices){
                val numberStr = contact.mWhatsAppNumbers2[i]
                val element = ContactDetailListElement(4, numberStr, contact.TYPE_WHATSAPP, contact.mWhatsAppVideoId[i])
                result.add(element)
            }
        }
        if(contact.mViberNumbers.size > 0){
            for(i in contact.mViberNumbers.indices){
                val numberStr = contact.mViberNumbers[i]
                val element = ContactDetailListElement(5, numberStr, contact.TYPE_VIBER, contact.mViberId[i])
                val element1 = ContactDetailListElement(6, numberStr, contact.TYPE_VIBER, contact.mViberId[i])
                result.add(element)
                result.add(element1)
            }
        }
        if(contact.mTelegram.size > 0){
            for(i in contact.mTelegram.indices){
                val numberStr = contact.mPhoneNumbers[i]
                val element = ContactDetailListElement(7, numberStr, contact.TYPE_TELEGRAM, contact.mTelegramId[i])
                result.add(element)
            }
        }
        if(null != contact.mSkypeName){
            val numberStr = contact.mSkypeName
            val element = ContactDetailListElement(8, numberStr, contact.TYPE_SKYPE, contact.contactID, contact.name)
            val element1 = ContactDetailListElement(9, numberStr, contact.TYPE_SKYPE, contact.contactID, contact.name)
            result.add(element)
            result.add(element1)

        }

        return result
    }

}

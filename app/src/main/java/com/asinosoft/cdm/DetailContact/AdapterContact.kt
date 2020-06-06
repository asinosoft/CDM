package com.asinosoft.cdm.DetailContact

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths
import com.asinosoft.cdm.R

class AdapterContact(val elements: ArrayList<ContactDetailListElement>): RecyclerView.Adapter<AdapterContact.ViewContactInfo>() {

    private lateinit var context: Context
    val mContact = Contact()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewContactInfo {
        context = parent.context
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.contact_detail_element,parent, false)!!
        return ViewContactInfo(view)
    }

    override fun getItemCount(): Int {
        return elements.size
    }

    override fun onBindViewHolder(holder: ViewContactInfo, position: Int) {
        holder.bind(elements[position])
    }

    inner class ViewContactInfo(view: View): RecyclerView.ViewHolder(view){

        private val mCustomLeft = itemView.findViewById<ImageButton>(R.id.callBtnNumber)
        private var mCustomRight = itemView.findViewById<ImageButton>(R.id.msgBtnNumber)
        private var numberType = itemView.findViewById<TextView>(R.id.discription_id)
        private var number = itemView.findViewById<TextView>(R.id.number_id)

        fun bind(item: ContactDetailListElement){

            if(null != item.numberType){
                when(item.numberType){
                    1 -> numberType.text = context.getString(R.string.type_home)
                    2 -> numberType.text = context.getString(R.string.type_mobile)
                    3 -> numberType.text = context.getString(R.string.type_work)
                    4 -> numberType.text = context.getString(R.string.type_fax_work)
                    5 -> numberType.text = context.getString(R.string.type_fax_home)
                    6 -> numberType.text = context.getString(R.string.type_pager)
                    7 -> numberType.text = context.getString(R.string.type_other)
                    8 -> numberType.text = context.getString(R.string.type_callback)
                    9 -> numberType.text = context.getString(R.string.type_car)
                    10 -> numberType.text = context.getString(R.string.type_company_main)
                    11 -> numberType.text = context.getString(R.string.type_isdn)
                    12 -> numberType.text = context.getString(R.string.type_main)
                    13 -> numberType.text = context.getString(R.string.type_other_fax)
                    14 -> numberType.text = context.getString(R.string.type_radio)
                    15 -> numberType.text = context.getString(R.string.type_telex)
                    16 -> numberType.text = context.getString(R.string.type_tty_tdd)
                    17 -> numberType.text = context.getString(R.string.type_work_mobile)
                    18 -> numberType.text = context.getString(R.string.type_work_pager)
                    19 -> numberType.text = context.getString(R.string.type_assistant)
                    20 -> numberType.text = context.getString(R.string.type_mms)
                    mContact.TYPE_EMAIL -> numberType.text = context.getString(R.string.type_e_mail)
                    mContact.TYPE_WHATSAPP -> numberType.text = context.getString(R.string.type_whatsapp)
                    mContact.TYPE_VIBER -> numberType.text = context.getString(R.string.type_viber)
                    mContact.TYPE_TELEGRAM -> numberType.text = context.getString(R.string.type_telegram)
                    mContact.TYPE_SKYPE -> numberType.text = context.getString(R.string.type_skype)
                }
            }

            /**
             * 0 - NumberCallAndMsg
             * 1 - EmailMsg
             * 2 - WhatsAppMsg
             * 3 - WhatsAppCall
             * 4 - WhatsAppVideoCall
             * 5 - ViberMsg
             * 6 - ViberCall
             * 7 - open Telegram
             * 8 - SkypeMsg
             * 9 - SkypeCall
             */

            when (item.mActiveType) {
                0 -> { mCustomLeft.setBackgroundResource(R.drawable.call)
                        number.text = item.active
                       mCustomLeft.setOnClickListener(View.OnClickListener { v ->
                        Metoths.callPhone(item.active!!, context) })
                    mCustomRight.setBackgroundResource(R.drawable.message)
                    mCustomRight.setOnClickListener(View.OnClickListener { v ->
                        Metoths.sendMsg(item.active!!, context)
                    })
                }
                1 -> {
                    number.text = item.active
                    mCustomLeft.setBackgroundResource(R.drawable.email_192)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.sendEmail(item.active!!, context)
                    }
                }
                2 -> {
                    number.text = "Написать " + item.active
                    mCustomLeft.setBackgroundResource(R.drawable.whatsapp_192)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.openWhatsAppMsg(item.active!!, context)
                    }
                }
                3 -> {
                    number.text = "Аудиозвонок " + item.active
                    mCustomLeft.setBackgroundResource(R.drawable.whatsapp_call)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.callWhatsApp(item.Id!!, context)
                    }
                }
                4 -> {
                    number.text = "Видеозвонок " + item.active
                    mCustomLeft.setBackgroundResource(R.drawable.whatsapp_call)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.videoCallWhatsApp(item.Id!!, context)
                    }
                }
                5 -> {
                    number.text = "Написать " + item.active
                    mCustomLeft.setBackgroundResource(R.drawable.viber_message)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.viberMsg(item.Id!!, context)
                    }
                }
                6 -> {
                    number.text = "Аудиозвонок " + item.active
                    mCustomLeft.setBackgroundResource(R.drawable.viber)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.viberCall(item.active!!, context)
                }
                }

                7 -> {
                    number.text = "Написать " + item.active
                    mCustomLeft.setBackgroundResource(R.drawable.telegram)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.openTelegramNow(item.Id!!, context)
                    }
                }
                8 -> {
                    number.text = "Написать " + item.contactName
                    mCustomLeft.setBackgroundResource(R.drawable.skype_message)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.skypeMsg(item.active!!, context)
                    }
                }
                9 -> {
                    number.text = "Звонок " + item.contactName
                    mCustomLeft.setBackgroundResource(R.drawable.skype_call)
                    mCustomRight.visibility = View.INVISIBLE
                    mCustomLeft.setOnClickListener {
                        Metoths.skypeCall(item.active!!, context)
                    }
                }
            }
        }
    }
}

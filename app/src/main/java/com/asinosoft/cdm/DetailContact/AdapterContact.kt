package com.asinosoft.cdm.DetailContact

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths
import com.asinosoft.cdm.R
import org.jetbrains.anko.find


class AdapterContact(private val items: ArrayList<ContactItem>): RecyclerView.Adapter<AdapterContact.ViewContactInfo>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewContactInfo {
        context = parent.context
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.history_contact_fragment_element,parent, false)!!
        return ViewContactInfo(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewContactInfo, position: Int) {
       holder.getContact(items[position])
    }

    inner class ViewContactInfo(view: View): RecyclerView.ViewHolder(view){

        private val number = itemView.find<TextView>(R.id.number_id)
        private val numberType = itemView.find<TextView>(R.id.discription_id1)
        private val btnCall = itemView.find<ImageButton>(R.id.callBtnNumber)
        private val btnMsg = itemView.findViewById<ImageButton>(R.id.msgBtnNumber)

        private val linearEmail = itemView.findViewById<LinearLayout>(R.id.linearEmail)
        private val email = itemView.findViewById<TextView>(R.id.email_id)
        private val emailType = itemView.findViewById<TextView>(R.id.discriptionEmail_id)
        private val btnMsgEmail = itemView.findViewById<ImageButton>(R.id.msgBtnEmail)

        private val whatsAppNumberCall = itemView.findViewById<TextView>(R.id.whatsAppCallNumber)
        private val whatsAppNumberMsg = itemView.findViewById<TextView>(R.id.whatsAppMsgNumber)
        private val linearwhatsApp = itemView.findViewById<LinearLayout>(R.id.linearWhatsApp)
        private val whatsAppVideoCall = itemView.findViewById<TextView>(R.id.whatsAppVideoCall)
        private val whatsAppVideoCallBtn = itemView.findViewById<ImageButton>(R.id.whatsAppVideoCallBtn)
        private val whatsAppMsgBtn = itemView.findViewById<ImageButton>(R.id.whatsAppMsgBtn)
        private val whatsAppCallBtn = itemView.findViewById<ImageButton>(R.id.whatsAppCallBtn)

        private val linearViber = itemView.findViewById<LinearLayout>(R.id.linearViber)
        private val viberMsg = itemView.findViewById<TextView>(R.id.viberMsg)
        private val viberCall = itemView.findViewById<TextView>(R.id.viberCall)
        private val viberMsgBtn = itemView.findViewById<ImageButton>(R.id.viberMsgBtn)
        private val viberCallBtn = itemView.findViewById<ImageButton>(R.id.viberCallBtn)

        private val linearTelegram = itemView.findViewById<LinearLayout>(R.id.linearTelegram)
        private val telegram = itemView.findViewById<TextView>(R.id.telegramName)
        private val telegramMsgBtn = itemView.findViewById<ImageButton>(R.id.telegramMsgBtn)

        private val linearSkype = itemView.findViewById<LinearLayout>(R.id.linearSkype)
        private val skypeName = itemView.findViewById<TextView>(R.id.skypeName)
        private val skypeCallName = itemView.findViewById<TextView>(R.id.skypeNameCall)
        private val skypeMsgBtn = itemView.findViewById<ImageButton>(R.id.skypeMsgBtn)
        private val skypeCallBtn = itemView.findViewById<ImageButton>(R.id.skypeCallBtn)

        fun getContact(item: ContactItem){

            number.text = item.number.toString()
            numberType.text = item.numberType.toString()
            btnCall.setBackgroundResource(R.drawable.telephony_call_192)
            btnMsg.setBackgroundResource(R.drawable.sms_192)

            btnCall.setOnClickListener{
                Metoths.callPhone(item.number.toString(), context)
            }
            btnMsg.setOnClickListener {
                Metoths.sendMsg(item.number.toString(), context)
            }

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
                }
            }

            if(item.email == null){
                linearEmail.visibility = View.GONE
            }else{
                email.text = item.email
                emailType.text = item.emailType.toString()
                btnMsgEmail.setBackgroundResource(R.drawable.email_192)

                btnMsgEmail.setOnClickListener {
                    Metoths.sendEmail(item.email!!, context)
                }

                if(null != item.emailType){
                    when(item.numberType){
                        1 -> emailType.text = context.getString(R.string.type_work)
                        2 -> emailType.text = context.getString(R.string.type_my)
                        3 -> emailType.text = context.getString(R.string.type_other)
                        4 -> emailType.text = context.getString(R.string.type_assistant)
                    }
                }
            }

            if(item.rawContactId != null){
                whatsAppNumberCall.text = "Аудиозвонок " + item.number
                whatsAppNumberMsg.text = "Написать " + item.number
                whatsAppVideoCall.text = "Видеозвонок " + item.number
                whatsAppMsgBtn.setBackgroundResource(R.drawable.whatsapp_message)
                whatsAppCallBtn.setBackgroundResource(R.drawable.whatsapp_call)
                whatsAppVideoCallBtn.setBackgroundResource(R.drawable.whatsapp_call)

                whatsAppVideoCallBtn.setOnClickListener {
                    Metoths.videoCallWhatsApp(item.whatsAppVideoCallId!!, context)
                }

                whatsAppMsgBtn.setOnClickListener {
                    Metoths.openWhatsAppMsg(item.number!!, context)
                }

                whatsAppCallBtn.setOnClickListener {
                    Metoths.callWhatsApp(item.whatsAppCallId!!, context)
                }
            }else{
                linearwhatsApp.visibility = View.GONE
            }

            if(item.hasViber != 0){
                viberMsg.text = "Написать " + item.number
                viberCall.text = "Аудиозвонок " + item.number
                viberMsgBtn.setBackgroundResource(R.drawable.viber_message)
                viberCallBtn.setBackgroundResource(R.drawable.viber)

                viberMsgBtn.setOnClickListener {
                    Metoths.viberMsg(item.hasViber!!, context)
                }
                viberCallBtn.setOnClickListener {
                    Metoths.viberCall(item.viberUri!!, context)
                }

            }else{
                linearViber.visibility = View.GONE
            }

            if(item.hasTelegram != null){
                telegram.text = "Написать " +  item.number
                telegramMsgBtn.setBackgroundResource(R.drawable.telegram)

                telegramMsgBtn.setOnClickListener {
                    Metoths.openTelegram(item.telegramId!!, context)
                }

            }else{
                linearTelegram.visibility = View.GONE
            }

            if(item.skypeId != null){
                skypeName.text = item.skypeName
                skypeCallName.text = item.skypeName
                skypeMsgBtn.setBackgroundResource(R.drawable.skype_message)
                skypeCallBtn.setBackgroundResource(R.drawable.skype_call)

                skypeCallBtn.setOnClickListener {
                    Metoths.skypeCall(item.skypeName!!, context)
                }
                skypeMsgBtn.setOnClickListener {
                    Metoths.skypeMsg(item.skypeName!!, context)
                }

            }else{
                linearSkype.visibility = View.GONE
            }
        }
    }
}

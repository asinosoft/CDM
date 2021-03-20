package com.asinosoft.cdm.detail_contact

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths
import com.asinosoft.cdm.R
import kotlin.collections.ArrayList

class AdapterContact(private val elements: ArrayList<ContactDetailListElement>): RecyclerView.Adapter<AdapterContact.ViewContactInfo>() {

    private lateinit var context: Context


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

    inner class ViewContactInfo(val view: View): RecyclerView.ViewHolder(view){

        private val mCustomLeft = itemView.findViewById<ImageButton>(R.id.callBtnNumber)
        private var mCustomMiddle = itemView.findViewById<ImageButton>(R.id.msgBtnNumber)
        private var mCustomRight = itemView.findViewById<ImageButton>(R.id.videoBtnNumber)
        private var numberType = itemView.findViewById<TextView>(R.id.discription_id)
        private var number = itemView.findViewById<TextView>(R.id.number_id)
        private var bText = itemView.findViewById<TextView>(R.id.bText)

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
                    Contact.TYPE_EMAIL -> numberType.text = context.getString(R.string.type_e_mail)
                    Contact.TYPE_WHATSAPP -> numberType.text = context.getString(R.string.type_whatsapp)
                    Contact.TYPE_VIBER -> numberType.text = context.getString(R.string.type_viber)
                    Contact.TYPE_TELEGRAM -> numberType.text = context.getString(R.string.type_telegram)
                    Contact.TYPE_SKYPE -> numberType.text = context.getString(R.string.type_skype)
                    Contact.TYPE_BIRTHDAY -> numberType.text = context.getString(R.string.type_birthday)
                }
            }else{
                numberType.text = " "
            }

            if(null != item.emailType){
                when(item.emailType){
                    1 -> numberType.text = context.getString(R.string.type_work)
                    2 -> numberType.text = context.getString(R.string.type_my)
                    3 -> numberType.text = context.getString(R.string.type_other)
                }
            }

            /**
             * 0 - NumberCallAndMsg
             * 1 - WhatsApp
             * 2 - Viber
             * 3 - Telegram
             * 4 - Skype
             * 5 - Email
             * 6 - BirthDay
             */

            when (item.mActiveType) {
                0 -> {
                    mCustomMiddle.setBackgroundResource(R.drawable.call)
                    number.text = item.active
                    mCustomLeft.visibility = View.INVISIBLE
                    bText.visibility = View.GONE
                    mCustomMiddle.setOnClickListener { v ->
                        Metoths.callPhone(item.active!!, context)
                    }
                    mCustomRight.setBackgroundResource(R.drawable.message)
                    mCustomRight.setOnClickListener { v ->
                        Metoths.sendMsg(item.active!!, context)
                    }
                }
                1 -> {
                    number.text = StHelper.convertNumber(item.active!!)
                    mCustomLeft.setBackgroundResource(R.drawable.whatsapp_message)
                    mCustomMiddle.setBackgroundResource(R.drawable.whatsapp_call)
                    bText.visibility = View.GONE
                    mCustomLeft.setOnClickListener {
                        Metoths.openWhatsApp(item.active!!, context)
                    }
                    mCustomMiddle.setOnClickListener {
                        Metoths.callWhatsApp(item.Id!!, context)
                    }
                    mCustomRight.setOnClickListener {
                        Metoths.videoCallWhatsApp(item.callId!!, context)
                    }
                }
                2 -> {
                    number.text = StHelper.convertNumber(item.active!!)
                    mCustomMiddle.setBackgroundResource(R.drawable.viber_message)
                    mCustomRight.setBackgroundResource(R.drawable.viber)
                    mCustomLeft.visibility = View.INVISIBLE
                    bText.visibility = View.GONE
                    mCustomMiddle.setOnClickListener {
                        Metoths.viberMsg(item.Id!!, context)
                    }
                    mCustomRight.setOnClickListener {
                        Metoths.viberCall(item.active!!, context)
                    }
                }
                3 -> {
                    number.text = item.contactNameAndNumber
                    mCustomRight.setBackgroundResource(R.drawable.telegram)
                    mCustomMiddle.visibility = View.INVISIBLE
                    mCustomLeft.visibility = View.INVISIBLE
                    bText.visibility = View.GONE
                    mCustomRight.setOnClickListener {
                        Metoths.openTelegramNow(item.active!!, context)
                    }
                }
                4 -> {
                    number.text = item.contactNameAndNumber
                    mCustomMiddle.setBackgroundResource(R.drawable.skype_message)
                    mCustomRight.setBackgroundResource(R.drawable.skype_call)
                    mCustomLeft.visibility = View.INVISIBLE
                    bText.visibility = View.GONE
                    mCustomMiddle.setOnClickListener {
                        Metoths.skypeMsg(item.active!!, context)
                    }
                    mCustomRight.setOnClickListener {
                        Metoths.skypeCall(item.active!!, context)
                    }
                }
                5 -> {
                    number.text = item.active
                    mCustomRight.setBackgroundResource(R.drawable.email_192)
                    mCustomMiddle.visibility = View.INVISIBLE
                    mCustomLeft.visibility = View.INVISIBLE
                    bText.visibility = View.GONE
                    mCustomRight.setOnClickListener {
                        Metoths.sendEmail(item.active!!, context)
                    }
                }
                6 -> {
                    number.text = item.active
                    bText.text = item.Id + " лет"
                    bText.visibility = View.VISIBLE
                    mCustomLeft.visibility = View.GONE
                    mCustomMiddle.visibility = View.GONE
                    mCustomRight.visibility = View.GONE
                }

            }
        }
    }
}

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
import com.asinosoft.cdm.data.*

class AdapterContact(private val elements: List<ContactItem>) :
    RecyclerView.Adapter<AdapterContact.ViewContactInfo>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewContactInfo {
        context = parent.context
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_detail_element, parent, false)!!
        return ViewContactInfo(view)
    }

    override fun getItemCount(): Int {
        return elements.size
    }

    override fun onBindViewHolder(holder: ViewContactInfo, position: Int) {
        holder.bind(elements[position])
    }

    inner class ViewContactInfo(val view: View) : RecyclerView.ViewHolder(view) {

        private val mCustomLeft = itemView.findViewById<ImageButton>(R.id.callBtnNumber)
        private var mCustomMiddle = itemView.findViewById<ImageButton>(R.id.msgBtnNumber)
        private var mCustomRight = itemView.findViewById<ImageButton>(R.id.videoBtnNumber)
        private var numberType = itemView.findViewById<TextView>(R.id.discription_id)
        private var number = itemView.findViewById<TextView>(R.id.number_id)
        private var bText = itemView.findViewById<TextView>(R.id.bText)

        fun bind(item: ContactItem) {
            when (item) {
                is BirthdayItem -> bindBirthday(item)
                is EmailItem -> bindEmail(item)
                is PhoneItem -> bindPhone(item)
                is SkypeItem -> bindSkype(item)
                is TelegramItem -> bindTelegram(item)
                is ViberItem -> bindViber(item)
                is WhatsAppItem -> bindWhatsApp(item)
            }
        }

        private fun bindBirthday(item: BirthdayItem) {
            numberType.text = context.getString(R.string.type_birthday)
            number.text = item.value
            bText.text = item.age + " лет"
            bText.visibility = View.GONE
            mCustomLeft.visibility = View.GONE
            mCustomMiddle.visibility = View.GONE
            mCustomRight.visibility = View.GONE
        }

        private fun bindEmail(item: EmailItem) {
            when (item.emailType) {
                1 -> numberType.text = context.getString(R.string.type_work)
                2 -> numberType.text = context.getString(R.string.type_my)
                3 -> numberType.text = context.getString(R.string.type_other)
                else -> numberType.text = context.getString(R.string.type_e_mail)
            }

            number.text = item.value
            mCustomRight.setBackgroundResource(R.drawable.email_192)
            mCustomMiddle.visibility = View.INVISIBLE
            mCustomLeft.visibility = View.INVISIBLE
            bText.visibility = View.GONE
            mCustomRight.setOnClickListener {
                Metoths.sendEmail(item.value, context)
            }
        }

        private fun bindPhone(item: PhoneItem) {
            when (item.phoneType) {
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
                else -> R.string.type_main
            }

            mCustomMiddle.setBackgroundResource(R.drawable.call)
            number.text = item.prettyNumber
            mCustomLeft.visibility = View.INVISIBLE
            bText.visibility = View.GONE
            mCustomMiddle.setOnClickListener { v ->
                Metoths.callPhone(item.value, context)
            }
            mCustomRight.setBackgroundResource(R.drawable.message)
            mCustomRight.setOnClickListener { v ->
                Metoths.sendMsg(item.value, context)
            }
        }

        private fun bindSkype(item: SkypeItem) {
            numberType.text = context.getString(R.string.type_skype)
            number.text = item.value
            mCustomMiddle.setBackgroundResource(R.drawable.skype_message)
            mCustomRight.setBackgroundResource(R.drawable.skype_call)
            mCustomLeft.visibility = View.INVISIBLE
            bText.visibility = View.GONE
            mCustomMiddle.setOnClickListener {
                Metoths.skypeMsg(item.value, context)
            }
            mCustomRight.setOnClickListener {
                Metoths.skypeCall(item.value, context)
            }
        }

        private fun bindTelegram(item: TelegramItem) {
            numberType.text = context.getString(R.string.type_telegram)
            number.text = item.value
            mCustomRight.setBackgroundResource(R.drawable.telegram)
            mCustomMiddle.visibility = View.INVISIBLE
            mCustomLeft.visibility = View.INVISIBLE
            bText.visibility = View.GONE
            mCustomRight.setOnClickListener {
                Metoths.openTelegramNow(item.chatId, context)
            }
        }

        private fun bindViber(item: ViberItem) {
            numberType.text = context.getString(R.string.type_viber)
            number.text = StHelper.convertNumber(item.value)
            mCustomMiddle.setBackgroundResource(R.drawable.viber_message)
            mCustomRight.setBackgroundResource(R.drawable.viber)
            mCustomLeft.visibility = View.INVISIBLE
            bText.visibility = View.GONE
            mCustomMiddle.setOnClickListener {
                Metoths.viberMsg(item.value, context)
            }
            item.videoId?.let { videoId ->
                mCustomRight.setOnClickListener {
                    Metoths.viberCall(videoId, context)
                }
            }
        }

        private fun bindWhatsApp(item: WhatsAppItem) {
            numberType.text = context.getString(R.string.type_whatsapp)
            number.text = StHelper.convertNumber(item.value)
            mCustomLeft.setBackgroundResource(R.drawable.whatsapp_message)
            mCustomMiddle.setBackgroundResource(R.drawable.whatsapp_call)
            bText.visibility = View.GONE
            item.chatId?.let { chatId ->
                mCustomLeft.setOnClickListener {
                    Metoths.openWhatsAppChat(chatId, context)
                }
            }
            item.audioId?.let { audioId ->
                mCustomMiddle.setOnClickListener {
                    Metoths.callWhatsApp(audioId, context)
                }
            }
            item.videoId?.let { videoId ->
                mCustomRight.setOnClickListener {
                    Metoths.videoCallWhatsApp(videoId, context)
                }
            }
        }
    }
}

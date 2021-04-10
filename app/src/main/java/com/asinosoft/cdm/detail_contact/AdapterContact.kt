package com.asinosoft.cdm.detail_contact

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths
import com.asinosoft.cdm.R
import com.asinosoft.cdm.data.*
import org.jetbrains.anko.textResource

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
            bText.visibility = View.VISIBLE
            mCustomLeft.visibility = View.GONE
            mCustomMiddle.visibility = View.GONE
            mCustomRight.visibility = View.GONE
        }

        private fun bindEmail(item: EmailItem) {
            numberType.textResource = Email.getTypeLabelResource(item.emailType)
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
            numberType.textResource = Phone.getTypeLabelResource(item.phoneType)
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
                Metoths.viberMsg(item.videoId, context)
            }
            mCustomRight.setOnClickListener {
                Metoths.viberCall(item.videoId, context)
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

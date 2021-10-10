package com.asinosoft.cdm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.helpers.StHelper

class ContactActionsAdapter(private val contact: Contact) :
    RecyclerView.Adapter<ContactActionsAdapter.ViewContactInfo>() {

    private lateinit var context: Context
    private val groups = contact.actions.groupBy { Item(it.type.group, it.value, it.description) }
    private val keys = groups.keys.toList().sortedBy { it.group.order }

    inner class Item(val group: Action.Group, val name: String, val description: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Item

            if (group != other.group) return false
            if (name != other.name) return false

            return true
        }

        override fun hashCode(): Int {
            var result = group.hashCode()
            result = 31 * result + name.hashCode()
            return result
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewContactInfo {
        context = parent.context
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_detail_element, parent, false)!!
        return ViewContactInfo(view)
    }

    override fun getItemCount(): Int {
        return keys.size + (if (contact.birthday != null) 1 else 0)
    }

    override fun onBindViewHolder(holder: ViewContactInfo, position: Int) {
        if (position >= keys.size) {
            holder.bindBirthday(contact.birthday, contact.age)
        } else {

            val key = keys[position]
            val actions = groups[key]!!
            when (key.group) {
                Action.Group.Email -> holder.bindEmail(actions.first())
                Action.Group.Phone -> holder.bindPhone(key, actions)
                Action.Group.Skype -> holder.bindSkype(key, actions)
                Action.Group.Telegram -> holder.bindTelegram(key, actions)
                Action.Group.Viber -> holder.bindViber(key, actions)
                Action.Group.WhatsApp -> holder.bindWhatsApp(key, actions)
            }
        }
    }

    inner class ViewContactInfo(val view: View) : RecyclerView.ViewHolder(view) {

        private val mCustomLeft = itemView.findViewById<ImageButton>(R.id.callBtnNumber)
        private var mCustomMiddle = itemView.findViewById<ImageButton>(R.id.msgBtnNumber)
        private var mCustomRight = itemView.findViewById<ImageButton>(R.id.videoBtnNumber)
        private var numberType = itemView.findViewById<TextView>(R.id.discription_id)
        private var number = itemView.findViewById<TextView>(R.id.number_id)
        private var bText = itemView.findViewById<TextView>(R.id.bText)

        fun bindBirthday(birthday: String?, age: String?) {
            numberType.text = context.getString(R.string.type_birthday)
            number.text = birthday
            bText.text = "$age лет"
            bText.visibility = View.VISIBLE
            mCustomLeft.visibility = View.GONE
            mCustomMiddle.visibility = View.GONE
            mCustomRight.visibility = View.GONE
        }

        fun bindEmail(email: Action) {
            numberType.text = email.description
            number.text = email.value
            mCustomRight.setBackgroundResource(R.drawable.ic_email)
            mCustomMiddle.visibility = View.INVISIBLE
            mCustomLeft.visibility = View.INVISIBLE
            bText.visibility = View.GONE
            mCustomRight.setOnClickListener {
                email.perform(context)
            }
        }

        fun bindPhone(item: Item, actions: List<Action>) {
            numberType.text = item.description
            mCustomMiddle.setBackgroundResource(R.drawable.ic_phone_call)
            number.text = StHelper.convertNumber(item.name)
            mCustomLeft.visibility = View.INVISIBLE
            bText.visibility = View.GONE
            mCustomMiddle.setOnClickListener { v ->
                actions.find { it.type == Action.Type.PhoneCall }?.perform(context)
            }
            mCustomRight.setBackgroundResource(R.drawable.ic_sms)
            mCustomRight.setOnClickListener { v ->
                actions.find { it.type == Action.Type.Sms }?.perform(context)
            }
        }

        fun bindSkype(item: Item, actions: List<Action>) {
            numberType.text = item.description
            number.text = item.name
            mCustomMiddle.setBackgroundResource(R.drawable.ic_skype_chat)
            mCustomRight.setBackgroundResource(R.drawable.ic_skype_call)
            mCustomLeft.visibility = View.INVISIBLE
            bText.visibility = View.GONE
            mCustomMiddle.setOnClickListener {
                actions.find { it.type == Action.Type.SkypeChat }?.perform(context)
            }
            mCustomRight.setOnClickListener {
                actions.find { it.type == Action.Type.SkypeCall }?.perform(context)
            }
        }

        fun bindTelegram(item: Item, actions: List<Action>) {
            numberType.text = item.description
            number.text = item.name
            mCustomLeft.setBackgroundResource(R.drawable.ic_telegram_chat)
            mCustomMiddle.setBackgroundResource(R.drawable.ic_telegram_call)
            mCustomRight.setBackgroundResource(R.drawable.ic_telegram_video)
            bText.visibility = View.GONE
            mCustomLeft.setOnClickListener {
                actions.find { it.type == Action.Type.TelegramChat }?.perform(context)
            }
            mCustomMiddle.setOnClickListener {
                actions.find { it.type == Action.Type.TelegramCall }?.perform(context)
            }
            mCustomRight.setOnClickListener {
                actions.find { it.type == Action.Type.TelegramVideo }?.perform(context)
            }
        }

        fun bindViber(item: Item, actions: List<Action>) {
            numberType.text = item.description
            number.text = StHelper.convertNumber(item.name)
            mCustomLeft.visibility = View.INVISIBLE
            mCustomMiddle.setBackgroundResource(R.drawable.ic_viber_chat)
            mCustomRight.setBackgroundResource(R.drawable.ic_viber_call)
            bText.visibility = View.GONE
            mCustomMiddle.setOnClickListener {
                actions.find { it.type == Action.Type.ViberChat }?.perform(context)
            }
            mCustomRight.setOnClickListener {
                actions.find { it.type == Action.Type.ViberCall }?.perform(context)
            }
        }

        fun bindWhatsApp(item: Item, actions: List<Action>) {
            numberType.text = item.description
            number.text = StHelper.convertNumber(item.name)
            mCustomLeft.setBackgroundResource(R.drawable.ic_whatsapp_chat)
            mCustomMiddle.setBackgroundResource(R.drawable.ic_whatsapp_call)
            mCustomRight.setBackgroundResource(R.drawable.ic_whatsapp_video)
            bText.visibility = View.GONE
            mCustomLeft.setOnClickListener {
                actions.find { it.type == Action.Type.WhatsAppChat }?.perform(context)
            }
            mCustomMiddle.setOnClickListener {
                actions.find { it.type == Action.Type.WhatsAppCall }?.perform(context)
            }
            mCustomRight.setOnClickListener {
                actions.find { it.type == Action.Type.WhatsAppVideo }?.perform(context)
            }
        }
    }
}

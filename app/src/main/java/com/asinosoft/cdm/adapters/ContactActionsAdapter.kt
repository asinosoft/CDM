package com.asinosoft.cdm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.App
import com.asinosoft.cdm.R
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.helpers.StHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class ContactActionsAdapter(private val contact: Contact) :
    RecyclerView.Adapter<ContactActionsAdapter.ViewContactInfo>() {

    companion object {
        const val TYPE_ADVERTISER = 0
        const val TYPE_ACTION = 1
        const val TYPE_BIRTHDAY = 2
    }

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

    override fun getItemCount(): Int {
        return 1 + keys.size + (if (contact.birthday != null) 1 else 0)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 1 + keys.size) {
            TYPE_BIRTHDAY
        } else if (keys.size < 3) {
            if (position == keys.size) TYPE_ADVERTISER else TYPE_ACTION
        } else {
            if (position == 3) TYPE_ADVERTISER else TYPE_ACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewContactInfo {
        context = parent.context
        val view: View = when (viewType) {
            TYPE_ACTION, TYPE_BIRTHDAY ->
                LayoutInflater.from(context)
                    .inflate(R.layout.contact_detail_element, parent, false)
            TYPE_ADVERTISER ->
                LayoutInflater.from(context)
                    .inflate(R.layout.advertiser, parent, false)
            else -> throw Exception("Invalid view type: $viewType")
        }
        return ViewContactInfo(view)
    }

    override fun onBindViewHolder(holder: ViewContactInfo, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ACTION -> bindAction(holder, position)
            TYPE_BIRTHDAY -> holder.bindBirthday(contact.birthday, contact.age)
            TYPE_ADVERTISER -> bindAdvertiser(holder)
        }
    }

    private fun bindAction(holder: ViewContactInfo, position: Int) {
        val index = if (position > 3) position - 1 else position
        val key = keys[index]
        val actions = groups[key]!!
        when (key.group) {
            Action.Group.Email -> holder.bindEmail(actions.first())
            Action.Group.Phone -> holder.bindPhone(key, actions)
            Action.Group.Skype -> holder.bindSkype(key, actions)
            Action.Group.Telegram -> holder.bindTelegram(key, actions)
            Action.Group.Viber -> holder.bindViber(key, actions)
            Action.Group.WhatsApp -> holder.bindWhatsApp(key, actions)
        }

        holder.view.findViewById<View>(R.id.divider).isVisible =
            App.instance?.config?.listDivider ?: false
    }

    private fun bindAdvertiser(holder: ViewContactInfo) {
        holder.itemView.findViewById<AdView>(R.id.adView)
            .loadAd(AdRequest.Builder().build())
    }

    inner class ViewContactInfo(val view: View) : RecyclerView.ViewHolder(view) {

        private val mCustomLeft = itemView.findViewById<ImageButton>(R.id.callBtnNumber)
        private var mCustomMiddle = itemView.findViewById<ImageButton>(R.id.msgBtnNumber)
        private var mCustomRight = itemView.findViewById<ImageButton>(R.id.videoBtnNumber)
        private var numberType = itemView.findViewById<TextView>(R.id.description_id)
        private var number = itemView.findViewById<TextView>(R.id.number_id)
        private var bText = itemView.findViewById<TextView>(R.id.bText)

        fun bindBirthday(birthday: String?, age: Int) {
            numberType.text = context.getString(R.string.type_birthday)
            number.text = birthday
            bText.text = view.resources.getQuantityString(R.plurals.age, age, age)
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

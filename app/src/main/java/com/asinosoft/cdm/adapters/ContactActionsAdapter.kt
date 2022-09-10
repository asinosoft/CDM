package com.asinosoft.cdm.adapters

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import com.asinosoft.cdm.helpers.telecomManager
import com.google.android.gms.ads.AdView
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
import java.util.*
import com.google.android.gms.ads.AdRequest as GoogleAds
import com.yandex.mobile.ads.common.AdRequest as YandexAds

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

    override fun getItemCount(): Int {
        return keys.size + (if (contact.birthday != null) 1 else 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewContactInfo {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_detail_element, parent, false)
        return ViewContactInfo(view)
    }

    override fun onBindViewHolder(holder: ViewContactInfo, position: Int) {
        if (position == keys.size) {
            holder.bindBirthday(contact.birthday, contact.age)
        } else {
            bindAction(holder, keys[position])
            if (position == (keys.size - 1).coerceAtMost(2)) {
                bindAdvertiser(holder)
            }
        }
    }

    private fun bindAction(holder: ViewContactInfo, key: Item) {
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

    private fun bindAdvertiser(holder: ViewContactInfo) {
        if ("ru" == Locale.getDefault().language) {
            holder.itemView.findViewById<BannerAdView>(R.id.yandexAds).apply {
                setAdUnitId(context.getString(R.string.yandex_ads_unit_id))
                setAdSize(AdSize.flexibleSize(320, 250))
                loadAd(YandexAds.Builder().build())
                visibility = View.VISIBLE
            }
        } else {
            holder.itemView.findViewById<AdView>(R.id.googleAds).apply {
                loadAd(GoogleAds.Builder().build())
                visibility = View.VISIBLE
            }
        }
    }

    inner class ViewContactInfo(val view: View) : RecyclerView.ViewHolder(view) {

        private val mCustomLeft = itemView.findViewById<ImageButton>(R.id.btnAction_1)
        private var mCustomMiddle = itemView.findViewById<ImageButton>(R.id.btnAction_2)
        private var mCustomRight = itemView.findViewById<ImageButton>(R.id.btnAction_3)
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
            // Если в телефоне установлены две симки, то показываем две иконки вызов взамен стандартной
            if (PackageManager.PERMISSION_GRANTED == context.checkSelfPermission((Manifest.permission.READ_PHONE_STATE))) {
                (context.telecomManager.callCapablePhoneAccounts.size >= 2).let { isDualSim ->
                    mCustomLeft.visibility = View.VISIBLE
                    mCustomLeft.setBackgroundResource(R.drawable.call_sim1)
                    mCustomLeft.setOnClickListener { v ->
                        actions.find { it.type == Action.Type.PhoneCall }?.perform(context, 1)
                    }
                    mCustomMiddle.setBackgroundResource(R.drawable.call_sim2)
                    mCustomMiddle.setOnClickListener { v ->
                        actions.find { it.type == Action.Type.PhoneCall }?.perform(context, 2)
                    }
                }
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

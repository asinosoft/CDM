package com.asinosoft.cdm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.setColoredText
import com.asinosoft.cdm.api.Contact
import com.asinosoft.cdm.databinding.HistorySwipingItemBinding
import com.zerobranch.layout.SwipeLayout
import kotlinx.coroutines.*

class AdapterContacts : RecyclerView.Adapter<AdapterContacts.Holder>() {

    private lateinit var context: Context
    private var nums = ""
    private var regex: Regex? = null
    private var contacts = listOf<Contact>()

    private var clickContactCallback: (Contact) -> Unit = {}

    fun setContactList(contacts: List<Contact>, nums: String = "", regex: Regex? = null) {
        this.contacts = contacts
        this.nums = nums
        this.regex = regex
        notifyDataSetChanged()
    }

    fun doOnClickContact(callback: (Contact) -> Unit) {
        clickContactCallback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        context = parent.context
        return Holder(HistorySwipingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(contacts[position])
    }

    inner class Holder(private val v: HistorySwipingItemBinding) :
        RecyclerView.ViewHolder(v.root) {

        fun bind(contact: Contact) {
            contact.photoUri?.let { v.imageContact.setImageURI(it.toUri()) }
                ?: v.imageContact.setImageResource(R.drawable.contact_unfoto)
            v.name.text = contact.name
            var tNum = ""
            contact.phones.forEach {
                if (!tNum.contains(it.value)) tNum =
                    tNum.plus("${it.value}, ")
            }
            v.number.text = tNum.dropLast(2)
            v.timeContact.text = ""
            v.dateContact.text = ""
            v.typeCall.visibility = View.GONE

            setColors()

            v.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                override fun onOpen(direction: Int, isContinuous: Boolean) {
                    when (direction) {
                        SwipeLayout.RIGHT -> {
                            contact.phones.firstOrNull()?.let { Metoths.callPhone(it.value, context) }
                        }
                        SwipeLayout.LEFT -> {
                            contact.whatsapps.firstOrNull()?.chatId?.let { Metoths.openWhatsAppChat(it, context) }
                        }
                    }
                    v.swipeLayout.close()
                }

                override fun onClose() {
                }
            })

            v.dragLayout.setOnClickListener {
                clickContactCallback(contact)
            }
        }

        /**
         * Установка выделения текста по номеру телефона.
         */
        private fun setColors() {
            if (nums.isNotEmpty() && v.number.text.contains(nums)) v.number.setColoredText(nums)
            regex?.find(v.name.text.toString())?.let {
                v.name.setColoredText(it.value)
            }
        }
    }
}

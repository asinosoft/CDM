package com.asinosoft.cdm.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.App
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.ContactItemBinding
import com.asinosoft.cdm.helpers.AvatarHelper
import com.asinosoft.cdm.helpers.Metoths.Companion.setColoredText
import com.zerobranch.layout.SwipeLayout

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.Holder>() {

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
        return Holder(
            ContactItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(contacts[position])
    }

    inner class Holder(private val v: ContactItemBinding) :
        RecyclerView.ViewHolder(v.root) {

        fun bind(contact: Contact) {
            v.imageContact.setImageDrawable(contact.getAvatar(context, AvatarHelper.SHORT))
            App.instance!!.config.favoritesBorderColor?.let { v.imageContact.borderColor = it }
            v.divider.isVisible = App.instance!!.config.listDivider

            v.name.text = contact.name
            var tNum = ""
            contact.phones.forEach {
                if (!tNum.contains(it.value)) tNum =
                    tNum.plus("${it.value}, ")
            }
            v.number.text = tNum.dropLast(2)
            setColors()

            v.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                override fun onOpen(direction: Int, isContinuous: Boolean) {
                    when (direction) {
                        SwipeLayout.RIGHT -> {
                            Analytics.logSearchSwipeRight()
                            contact.phones.firstOrNull()?.perform(context)
                        }
                        SwipeLayout.LEFT -> {
                            Analytics.logSearchSwipeLeft()
                            contact.chats.firstOrNull()?.perform(context)
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

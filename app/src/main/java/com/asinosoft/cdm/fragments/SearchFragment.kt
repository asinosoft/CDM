package com.asinosoft.cdm.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.asinosoft.cdm.R
import com.asinosoft.cdm.adapters.ContactsAdapter
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.ActivitySearchBinding
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.Metoths.Companion.toggle
import com.asinosoft.cdm.viewmodels.ManagerViewModel

class SearchFragment : Fragment() {
    private val model: ManagerViewModel by activityViewModels()
    private val contactsAdapter = ContactsAdapter()
    private var contacts = listOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Analytics.logActivitySearch()
        val v = ActivitySearchBinding.inflate(layoutInflater)
        initActivity(v)
        return v.root
    }

    private fun initActivity(v: ActivitySearchBinding) {
        val keyboard = childFragmentManager.findFragmentById(R.id.keyboard) as KeyboardFragment

        model.contacts.observe(viewLifecycleOwner) { contacts ->
            this.contacts = contacts.filter { contact -> contact.phones.isNotEmpty() }
                .sortedBy { it.name }
            contactsAdapter.setContactList(this.contacts)
        }

        v.rvFilteredContacts.adapter = contactsAdapter

        contactsAdapter.doOnClickContact { contact ->
            findNavController().navigate(
                R.id.action_open_found_contact,
                bundleOf("contactId" to contact.id)
            )
        }

        keyboard.doOnTextChanged { text ->
            val regex = Regex(Metoths.getPattern(text, requireContext()), RegexOption.IGNORE_CASE)
            contactsAdapter.setContactList(contacts.filtered(text, regex), text, regex)
        }

        keyboard.onCallButtonClick { phoneNumber, sim ->
            Analytics.logCallFromSearch()
            findNavController().popBackStack()
            Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
                .putExtra("sim", sim)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .let { startActivity(it) }
        }

        keyboard.onSettingsButtonClick {
            findNavController().navigate(R.id.action_open_settings)
        }

        keyboard.onCloseButtonClick {
            Analytics.logSearchKeyboardClose()
            v.layoutKeyboard.toggle()
            v.fabKeyboard.show()
        }

        v.fabKeyboard.setOnClickListener {
            v.layoutKeyboard.toggle()
            v.fabKeyboard.hide()
        }
    }

    private fun List<Contact>.filtered(nums: String, regex: Regex?): List<Contact> {
        val r = ArrayList<Contact>()
        this@filtered.forEach { contact ->
            contact.name?.let {
                regex?.find(it)?.let {
                    r.add(contact)
                    return@forEach
                }
            }
            contact.phones.forEach {
                if (it.value.contains(nums, true)) {
                    r.add(contact)
                    return@forEach
                }
            }
        }
        r.sortWith(
            compareBy {
                val res = it.name?.let { it1 -> regex?.find(it1) }
                if (res != null) {
                    it.name.indexOf(res.value)
                } else {
                    var index = 0
                    it.phones.forEach {
                        if (it.value.contains(nums, true)) {
                            index = it.value.indexOf(nums)
                            return@forEach
                        }
                    }
                    index
                }
            }
        )
        return r
    }
}

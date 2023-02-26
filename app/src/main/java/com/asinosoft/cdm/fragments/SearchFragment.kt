package com.asinosoft.cdm.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.asinosoft.cdm.R
import com.asinosoft.cdm.activities.CallActivity
import com.asinosoft.cdm.adapters.ContactsAdapter
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.ActivitySearchBinding
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.helpers.isDefaultDialer
import com.asinosoft.cdm.viewmodels.ManagerViewModel

class SearchFragment : Fragment() {
    private lateinit var v: ActivitySearchBinding
    private lateinit var keyboard: KeyboardFragment
    private val model: ManagerViewModel by activityViewModels()
    private val contactsAdapter = ContactsAdapter()
    private var contacts = listOf<Contact>()

    /**
     * Сброс фильтра при нажатии системной кнопки Назад
     */
    private val onBackPressed = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            keyboard.text = ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Analytics.logActivitySearch()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressed)
        v = ActivitySearchBinding.inflate(layoutInflater)
        keyboard = childFragmentManager.findFragmentById(R.id.keyboard) as KeyboardFragment
        arguments?.getString("phone")?.let { phone ->
            keyboard.text = phone
        }

        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.contacts.observe(viewLifecycleOwner) { contacts ->
            this.contacts = contacts.filter { contact -> contact.phones.isNotEmpty() }
                .sortedBy { it.name }
            filter(keyboard.text)
        }

        v.rvFilteredContacts.adapter = contactsAdapter

        contactsAdapter.doOnClickContact { contact ->
            findNavController().navigate(
                R.id.action_open_found_contact,
                bundleOf("contactId" to contact.id)
            )
        }

        keyboard.doOnTextChanged { text -> filter(text) }

        keyboard.onCallButtonClick { phoneNumber, sim ->
            Analytics.logCallFromSearch()
            findNavController().popBackStack()

            val intent = if (requireContext().isDefaultDialer())
                Intent(requireContext(), CallActivity::class.java)
            else
                Intent(Intent.ACTION_CALL)

            intent
                .setData(Uri.fromParts("tel", phoneNumber, null))
                .putExtra("sim", sim)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .let { startActivity(it) }
        }

        keyboard.onSettingsButtonClick {
            findNavController().navigate(R.id.action_open_settings)
        }

        keyboard.onCloseButtonClick {
            Analytics.logSearchKeyboardClose()
            v.layoutKeyboard.visibility = View.GONE
            v.fabKeyboard.show()
        }

        v.fabKeyboard.setOnClickListener {
            v.layoutKeyboard.visibility = View.VISIBLE
            v.fabKeyboard.hide()
        }
    }

    private fun filter(text: String) {
        val regex = Regex(Metoths.getPattern(text, requireContext()), RegexOption.IGNORE_CASE)
        val filtered = contacts.filtered(text, regex)
        contactsAdapter.setContactList(filtered, text, regex)
        onBackPressed.isEnabled = text.isNotEmpty()

        // Показываем/скрываем сообщение "Не найдено"
        v.notFound.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
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

package com.asinosoft.cdm.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.asinosoft.cdm.*
import com.asinosoft.cdm.helpers.Metoths.Companion.toggle
import com.asinosoft.cdm.adapters.AdapterContacts
import com.asinosoft.cdm.api.ContactRepositoryImpl
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.ActivitySearchBinding
import com.asinosoft.cdm.fragments.KeyboardFragment
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.helpers.Metoths
import com.jaeger.library.StatusBarUtil

/**
 * Экран поиска в списке контактов
 */
class SearchActivity : AppCompatActivity() {

    companion object {
        const val RESULT_CALL = 1
        const val RESULT_OPEN_SETTINGS = 2
    }

    private lateinit var v: ActivitySearchBinding
    private lateinit var keyboard: KeyboardFragment
    private val contactsAdapter = AdapterContacts()
    private var contacts = listOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(v.root)
        StatusBarUtil.setTranslucentForImageView(this, v.rvFilteredContacts)
        keyboard = supportFragmentManager.findFragmentById(R.id.keyboard) as KeyboardFragment
        initActivity()
    }

    private fun initActivity() {
        val contactRepository = ContactRepositoryImpl(this).apply { initialize() }

        contacts = contactRepository.getContacts()
            .filter { contact -> contact.phones.isNotEmpty() }
            .sortedBy { it.name }

        v.rvFilteredContacts.layoutManager = LinearLayoutManager(this)

        v.rvFilteredContacts.adapter = contactsAdapter

        v.fab.setOnClickListener { v.layoutKeyboard.toggle() }

        contactsAdapter.doOnClickContact { contact ->
            Metoths.openDetailContact(
                contact.phones.first().value,
                contact,
                this
            )
        }

        contactsAdapter.setContactList(contacts)

        keyboard.doOnTextChanged { text ->
            val regex = Regex(Metoths.getPattern(text, this), RegexOption.IGNORE_CASE)
            contactsAdapter.setContactList(contacts.filtered(text, regex), text, regex)
        }

        keyboard.onCallButtonClick { phoneNumber ->
            setResult(RESULT_CALL, Intent().apply { putExtra(Keys.number, phoneNumber) })
            finish()
        }

        keyboard.onSettingsButtonClick {
            setResult(RESULT_OPEN_SETTINGS)
            finish()
        }

        keyboard.onCloseButtonClick {
            finish()
        }
    }

    private fun List<Contact>.filtered(nums: String, regex: Regex?): List<Contact> {
        val r = ArrayList<Contact>()
        this@filtered.forEach { contact ->
            regex?.find(contact.name)?.let {
                r.add(contact)
                return@forEach
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
                val res = regex?.find(it.name)
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

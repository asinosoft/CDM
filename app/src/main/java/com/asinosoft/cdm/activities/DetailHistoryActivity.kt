package com.asinosoft.cdm.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asinosoft.cdm.R
import com.asinosoft.cdm.databinding.ActivityDetailHistoryBinding
import com.asinosoft.cdm.fragments.ContactDetailFragment
import com.asinosoft.cdm.fragments.ContactSettingsFragment
import com.asinosoft.cdm.fragments.HistoryDetailFragment
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Активность "Просмотр контакта"
 */
class DetailHistoryActivity : BaseActivity() {
    private val viewModel: DetailHistoryViewModel by viewModels()
    private lateinit var v: ActivityDetailHistoryBinding
    private lateinit var tabLabels: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.analytics.logEvent("activity_contact", Bundle.EMPTY)
        v = ActivityDetailHistoryBinding.inflate(layoutInflater)
        setContentView(v.root)

        val contactId = intent.getLongExtra(Keys.id, 0)
        if (0L != contactId) {
            viewModel.initialize(this, contactId)
            tabLabels = intArrayOf(
                R.string.contact_tab_history,
                R.string.contact_tab_actions,
                R.string.contact_tab_settings
            )
        } else {
            val phoneNumber = intent.getStringExtra(Keys.number) ?: ""
            viewModel.initialize(this, phoneNumber)
            tabLabels = intArrayOf(
                R.string.contact_tab_history
            )
        }

        v.pages.adapter = ContactPagesAdapter(this, tabLabels.size)

        TabLayoutMediator(v.tabs, v.pages) { tab, position ->
            tab.setText(tabLabels[position])
        }.attach()

        v.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // TODO: отправлять событие о переключении вкладок в Firebase
//                when (tab?.id) {
//                    0 -> Firebase.analytics.logEvent("contact_tab_calls", Bundle.EMPTY)
//                    1 -> Firebase.analytics.logEvent("contact_tab_actions", Bundle.EMPTY)
//                    2 -> Firebase.analytics.logEvent("contact_tab_settings", Bundle.EMPTY)
//                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        v.image.setImageDrawable(viewModel.getContactPhoto(this))
    }

    private inner class ContactPagesAdapter(
        fa: FragmentActivity,
        private val count: Int
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = count
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HistoryDetailFragment()
                1 -> ContactDetailFragment()
                else -> ContactSettingsFragment()
            }
        }
    }
}

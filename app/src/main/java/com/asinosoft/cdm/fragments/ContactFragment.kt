package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asinosoft.cdm.R
import com.asinosoft.cdm.databinding.ActivityDetailHistoryBinding
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Окно контакта
 */
class ContactFragment : Fragment() {
    private val model: DetailHistoryViewModel by activityViewModels()
    private lateinit var v: ActivityDetailHistoryBinding
    private val tabLabels = intArrayOf(
        R.string.contact_tab_actions,
        R.string.contact_tab_history,
        R.string.contact_tab_settings
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = ActivityDetailHistoryBinding.inflate(inflater)
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Firebase.analytics.logEvent("activity_contact", Bundle.EMPTY)

        arguments?.getLong("contactId")?.let { contactId ->
            model.initialize(requireContext(), contactId)
        }

        v.pages.adapter = ContactPagesAdapter(requireActivity(), tabLabels.size)

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

        v.image.setImageDrawable(model.getContactPhoto())
    }

    private inner class ContactPagesAdapter(
        fa: FragmentActivity,
        private val count: Int
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = count
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ContactDetailFragment()
                1 -> HistoryDetailFragment()
                else -> ContactSettingsFragment()
            }
        }
    }
}

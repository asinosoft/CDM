package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.databinding.ActivityDetailHistoryBinding
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Окно контакта
 */
class ContactFragment : Fragment() {
    private val model: DetailHistoryViewModel by activityViewModels()
    private lateinit var v: ActivityDetailHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getLong("contactId")?.let { contactId ->
            model.initialize(requireContext(), contactId)
        }

        Analytics.logActivityContact()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = ActivityDetailHistoryBinding.inflate(inflater, container, false)
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        v.pages.offscreenPageLimit = 2
        v.pages.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> ContactDetailFragment()
                1 -> HistoryDetailFragment()
                2 -> ContactSettingsFragment()
                else -> throw Exception("wrong position")
            }

            override fun getItemCount(): Int {
                return 3
            }
        }

        TabLayoutMediator(v.tabs, v.pages) { tab, position ->
            tab.text = when (position) {
                0 -> resources.getString(R.string.contact_tab_actions)
                1 -> resources.getString(R.string.contact_tab_history)
                2 -> resources.getString(R.string.contact_tab_settings)
                else -> throw Exception("wrong position")
            }

            when (position) {
                0 -> Analytics.logContactDetailsTab()
                1 -> Analytics.logContactHistoryTab()
                2 -> Analytics.logContactSettingsTab()
            }
        }.attach()

        if ("history" == arguments?.getString("tab")) {
            v.pages.post {
                v.pages.setCurrentItem(1, false)
            }
        }

        model.contact.observe(viewLifecycleOwner) { contact ->
            contact?.let {
                v.image.setImageDrawable(it.getAvatar(requireContext()))
                v.toolbarLayout.title = it.name
            }
        }
    }
}

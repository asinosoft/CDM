package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.R
import com.asinosoft.cdm.databinding.ActivityDetailHistoryBinding
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

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
        Firebase.analytics.logEvent("activity_contact", Bundle.EMPTY)

        v.pages.adapter = FragmentPagerItemAdapter(
            childFragmentManager,
            FragmentPagerItems.with(requireContext())
                .add(
                    resources.getString(R.string.contact_tab_actions),
                    ContactDetailFragment::class.java
                )
                .add(
                    resources.getString(R.string.contact_tab_history),
                    HistoryDetailFragment::class.java
                )
                .add(
                    resources.getString(R.string.contact_tab_settings),
                    ContactSettingsFragment::class.java
                )
                .create()
        )

        v.tabs.setViewPager(v.pages)

        model.contact.observe(viewLifecycleOwner) { contact ->
            contact?.let {
                v.image.setImageURI(it.photoUri)
                v.name.text = it.name
            }
        }
    }
}

package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asinosoft.cdm.R
import com.asinosoft.cdm.databinding.FragmentSettingsBinding
import com.google.android.material.tabs.TabLayoutMediator

class SettingsFragment : Fragment() {
    private lateinit var v: FragmentSettingsBinding
    private val tabLabels = arrayOf(
        R.string.settings_tab_appearance,
        R.string.settings_tab_actions,
        R.string.settings_tab_about
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = FragmentSettingsBinding.inflate(inflater, container, false)
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        v.pages.adapter = SettingsPagesAdapter(requireActivity())

        TabLayoutMediator(v.tabs, v.pages) { tab, position ->
            tab.setText(tabLabels[position])
        }.attach()
    }

    private inner class SettingsPagesAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> FavoritesSettingsFragment()
                1 -> ActionSettingsFragment()
                2 -> OutfitSettingsFragment()
                else -> OutfitSettingsFragment()
            }
        }
    }
}

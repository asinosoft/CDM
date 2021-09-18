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
        val v = FragmentSettingsBinding.inflate(inflater, container, false)

        v.pages.adapter = SettingsPagesAdapter(requireActivity())

        TabLayoutMediator(v.tabs, v.pages) { tab, position ->
            tab.setText(tabLabels[position])
        }.attach()

        return v.root
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

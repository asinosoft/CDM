package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.databinding.FragmentSettingsBinding
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = FragmentSettingsBinding.inflate(inflater, container, false)

        v.pages.adapter = FragmentPagerItemAdapter(
            childFragmentManager,
            FragmentPagerItems.with(requireContext())
                .add(
                    resources.getString(R.string.settings_tab_appearance),
                    FavoritesSettingsFragment::class.java
                )
                .add(
                    resources.getString(R.string.settings_tab_actions),
                    ActionSettingsFragment::class.java
                )
                .add(
                    resources.getString(R.string.settings_tab_phone),
                    OutfitSettingsFragment::class.java
                )
                .add(
                    resources.getString(R.string.settings_tab_about),
                    SettingsAboutFragment::class.java
                )
                .create()
        )

        v.pages.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> Analytics.logSettingsOutfitTab()
                    1 -> Analytics.logSettingsActionTab()
                    2 -> Analytics.logSettingsDialerTab()
                    3 -> Analytics.logSettingsAboutTab()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        v.tabs.setViewPager(v.pages)

        return v.root
    }
}

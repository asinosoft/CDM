package com.asinosoft.cdm.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asinosoft.cdm.R
import com.asinosoft.cdm.databinding.ActivitySettingsBinding
import com.asinosoft.cdm.fragments.ActionSettingsFragment
import com.asinosoft.cdm.fragments.FavoritesSettingsFragment
import com.asinosoft.cdm.fragments.OutfitSettingsFragment
import com.asinosoft.cdm.viewmodels.SettingsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

/**
 * Класс экрана настроек приложения
 */
class SettingsActivity : BaseActivity(), ColorPickerDialogListener {

    private val model: SettingsViewModel by viewModels()
    private val tabLabels = arrayOf(
        R.string.settings_tab_appearance,
        R.string.settings_tab_actions,
        R.string.settings_tab_about
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.analytics.logEvent("activity_settings", Bundle.EMPTY)
        ActivitySettingsBinding.inflate(layoutInflater).let { v ->
            setContentView(v.root)
            v.pages.adapter = SettingsPagesAdapter(this)

            TabLayoutMediator(v.tabs, v.pages) { tab, position ->
                tab.setText(tabLabels[position])
            }.attach()
        }
    }

    override fun onPause() {
        model.save()
        super.onPause()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        model.settings.colorBorder = color
        model.buttonColor.postValue(color)
    }

    override fun onDialogDismissed(dialogId: Int) {
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

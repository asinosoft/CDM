package com.asinosoft.cdm.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.asinosoft.cdm.R
import com.asinosoft.cdm.databinding.SettingsLayoutBinding
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

    companion object {
        const val SETTINGS_CHANGED = 1
        const val THEME_CHANGED = 2
    }

    private val model: SettingsViewModel by viewModels()
    private val icons = arrayOf(
        R.drawable.ic_photo_default,
        R.drawable.ic_whatsapp_chat,
        R.drawable.palette
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.analytics.logEvent("activity_settings", Bundle.EMPTY)
        SettingsLayoutBinding.inflate(layoutInflater).let { v ->
            setContentView(v.root)
            v.pages.adapter = SettingsPagesAdapter(this)

            TabLayoutMediator(v.tabs, v.pages) { tab, position ->
                tab.setIcon(icons[position])
            }.attach()
        }
    }

    override fun onPause() {
        setResult(model.save())
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

package com.asinosoft.cdm.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager.SIM_STATE_READY
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.asinosoft.cdm.R
import com.asinosoft.cdm.activities.BaseActivity
import com.asinosoft.cdm.helpers.TelecomHelper
import com.asinosoft.cdm.viewmodels.SettingsViewModel

/**
 * Страница настройки дозвонщика
 *
 * Список сим-карт пока только показывается, но не использвется!
 */
class DialerSettingsFragment : PreferenceFragmentCompat() {
    private val model: SettingsViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (findPreference("default_dialer") as SwitchPreference).apply {
            isChecked = model.settings.checkDefaultDialer

            setOnPreferenceChangeListener { preference, newValue ->
                model.settings.checkDefaultDialer = (newValue as Boolean)
                Log.d("SETTINGS", "$preference -> $newValue (${model.settings.checkDefaultDialer})")
                if (model.settings.checkDefaultDialer) {
                    (requireActivity() as BaseActivity).setDefaultDialer()
                }
                true
            }
        }

        if (PackageManager.PERMISSION_GRANTED == requireActivity().checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)) {
            fillSimList()
        }
    }

    private fun fillSimList() {
        val context = requireContext()
        val category = PreferenceCategory(context).apply {
            key = "sims"
            title = context.getString(R.string.sim_usage_settings)
        }.also {
            preferenceScreen.addPreference(it)
        }

        TelecomHelper.getSimSlotList(context).forEachIndexed { index, slot ->
            CheckBoxPreference(context).apply {
                key = "SIM $index"
                isChecked = true
                when (slot.simState) {
                    SIM_STATE_READY -> {
                        title = "SIM $index : ${slot.simOperatorName}"
                        check(true)
                    }
                    else -> {
                        val state = TelecomHelper.getSimStateText(context, slot.simState)
                        title = "SIM $index : $state"
                        isEnabled = false
                        check(false)
                    }
                }
                setOnPreferenceChangeListener { preference, newValue ->
                    // TODO: сохранить в настройках список сим-карт, доступных для звонков
                    Log.d("SIM $index", "$preference ->  $newValue")
                    true
                }
            }.also {
                category.addPreference(it)
            }
        }
    }
}

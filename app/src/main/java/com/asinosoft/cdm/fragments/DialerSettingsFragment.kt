package com.asinosoft.cdm.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager.SIM_STATE_READY
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.activityViewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.helpers.getAvailableSimSlots
import com.asinosoft.cdm.helpers.isDefaultDialer
import com.asinosoft.cdm.helpers.setDefaultDialer
import com.asinosoft.cdm.viewmodels.SettingsViewModel
import timber.log.Timber

/**
 * Страница настройки дозвонщика
 *
 * Список сим-карт пока только показывается, но не используется!
 */
class DialerSettingsFragment : PreferenceFragmentCompat() {
    private val model: SettingsViewModel by activityViewModels()
    private val launcher = registerForActivityResult(StartActivityForResult()) {
        if (true == context?.isDefaultDialer()) {
            Analytics.logDefaultDialer()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<SwitchPreference>("default_dialer")?.apply {
            isChecked = model.config.checkDefaultDialer

            setOnPreferenceChangeListener { preference, newValue ->
                model.config.checkDefaultDialer = (newValue as Boolean)
                Timber.d("%s -> %s, %s", preference, newValue, model.config.checkDefaultDialer)
                if (model.config.checkDefaultDialer) {
                    launcher.launch(context.setDefaultDialer())
                }
                Analytics.logCheckDefaultDialer(newValue)
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

        context.getAvailableSimSlots().forEach { slot ->
            CheckBoxPreference(context).apply {
                key = "SIM ${slot.id}"
                isChecked = true
                when (slot.state) {
                    SIM_STATE_READY -> {
                        title = "SIM ${slot.id} : ${slot.operator}"
                        check(true)
                    }
                    else -> {
                        title = "SIM ${slot.id} : ${slot.getStateText(context)}"
                        isEnabled = false
                        check(false)
                    }
                }
                setOnPreferenceChangeListener { preference, newValue ->
                    // TODO: сохранить в настройках список сим-карт, доступных для звонков
                    Timber.d("SIM %s: %s → %s", slot.id, preference, newValue)
                    true
                }
            }.also {
                category.addPreference(it)
            }
        }
    }
}

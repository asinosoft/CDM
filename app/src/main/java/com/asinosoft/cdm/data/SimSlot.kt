package com.asinosoft.cdm.data

import android.content.Context
import android.telecom.PhoneAccountHandle
import android.telephony.TelephonyManager
import com.asinosoft.cdm.R

/**
 * Информация о слоте для сим-карты
 */
data class SimSlot(
    val id: Int, // Номер слота (1..)
    val handle: PhoneAccountHandle,
    val state: Int, // Состояние (см. Telecom.SIM_***
    val operator: String, // Название сотового оператора
    val phoneNumber: String
) {
    fun getStateText(context: Context): String {
        return when (state) {
            TelephonyManager.SIM_STATE_READY -> context.getString(R.string.sim_state_ready)
            TelephonyManager.SIM_STATE_ABSENT -> context.getString(R.string.sim_state_absent)
            TelephonyManager.SIM_STATE_PIN_REQUIRED,
            TelephonyManager.SIM_STATE_PUK_REQUIRED,
            TelephonyManager.SIM_STATE_PERM_DISABLED,
            TelephonyManager.SIM_STATE_CARD_RESTRICTED -> context.getString(R.string.sim_state_locked)
            TelephonyManager.SIM_STATE_NOT_READY -> context.getString(R.string.sim_state_not_ready)
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> context.getString(R.string.sim_state_network_locked)
            else -> context.getString(R.string.sim_state_error)
        }
    }
}

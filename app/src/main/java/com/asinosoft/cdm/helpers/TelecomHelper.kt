package com.asinosoft.cdm.helpers

import android.Manifest.permission.READ_PHONE_STATE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import android.util.Log
import com.asinosoft.cdm.R
import org.jetbrains.anko.telecomManager
import org.jetbrains.anko.telephonyManager

/**
 * Информация о слоте для сим-карты
 */
data class SimSlotInfo(
    val id: Int, // Номер слота (1..)
    val state: Int, // Состояние (см. Telecom.SIM_***
    val stateText: String? = null, // Текстовое описание состояния
    val operator: String? = null, // Название сотового оператора
)

class TelecomHelper {
    companion object {
        /**
         * Возвращает список SIM-карт и операторов
         */
        fun getSimSlotList(context: Context): List<TelephonyManager> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(READ_PHONE_STATE)
            ) {
                return (1..context.telephonyManager.phoneCount).map { slot ->
                    context.telephonyManager.createForSubscriptionId(slot)
                }
            } else {
                return listOf()
            }
        }

        fun getSimStateText(context: Context, simState: Int): String {
            return when (simState) {
                SIM_STATE_READY -> context.getString(R.string.sim_state_ready)
                SIM_STATE_ABSENT -> context.getString(R.string.sim_state_absent)
                SIM_STATE_PIN_REQUIRED,
                SIM_STATE_PUK_REQUIRED,
                SIM_STATE_PERM_DISABLED,
                SIM_STATE_CARD_RESTRICTED -> context.getString(R.string.sim_state_locked)
                SIM_STATE_NOT_READY -> context.getString(R.string.sim_state_not_ready)
                SIM_STATE_NETWORK_LOCKED -> context.getString(R.string.sim_state_network_locked)
                else -> context.getString(R.string.sim_state_error)
            }
        }

        /**
         * Проверяет, явняется ли приложение дозвонщиком по-умолчанию
         */
        fun isDefaultDialer(context: Context): Boolean {
            Log.d("TelecomManager::isDefaultDialer", context.telecomManager.defaultDialerPackage)
            return context.packageName == context.telecomManager.defaultDialerPackage
        }
    }
}

package com.asinosoft.cdm

/**
 * Вспомогательный класс для сохранения/восстановления настроек панели избранных контактов
 */
data class CirPairData(
    var contactID: Long = 0,
    val contactSettings: ContactSettings? = null,
    val selectedNumber: String? = null
)

package com.asinosoft.cdm.api

import com.asinosoft.cdm.data.Contact

/**
 * Фильтр истории звонков, который пропускает только уникальные контакты
 */
class CallHistoryFilter(initialContacts: Collection<Contact> = listOf()) :
    CallHistoryRepository.Filter {
    private val phones: MutableSet<String> =
        initialContacts.flatMap { c -> c.phones.map { it.value } }.toMutableSet()

    override fun filter(call: CallHistoryItem): Boolean {
        if (phones.contains(call.phone)) {
            return false
        }

        if (null !== call.contact) {
            phones.addAll(call.contact.phones.map { it.value })
        }
        return true
    }
}

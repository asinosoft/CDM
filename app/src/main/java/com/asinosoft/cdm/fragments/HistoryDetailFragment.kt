package com.asinosoft.cdm.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.App
import com.asinosoft.cdm.activities.BaseActivity
import com.asinosoft.cdm.adapters.HistoryDetailsCallsAdapter
import com.asinosoft.cdm.api.CallHistoryItem
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.databinding.HistoryDetailFragmentBinding
import com.asinosoft.cdm.viewmodels.ManagerViewModel

/**
 * Фрагмент вкладки "Истории" в детальной информации по элементу истории
 */
class HistoryDetailFragment : Fragment() {
    private val model: ManagerViewModel by activityViewModels()
    private lateinit var v: HistoryDetailFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = HistoryDetailFragmentBinding.inflate(inflater, container, false)
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.contactHistory.observe(viewLifecycleOwner) { calls ->
            calls?.let { call ->
                v.rvContactCalls.adapter = HistoryDetailsCallsAdapter(
                    App.instance!!.config,
                    requireContext(),
                    call,
                    { item -> deleteCallHistoryItem(item) },
                    { contact -> purgeContactHistory(contact) }
                )
            }
        }
    }

    private fun deleteCallHistoryItem(call: CallHistoryItem) {
        (requireActivity() as BaseActivity).withPermission(Manifest.permission.WRITE_CALL_LOG) {
            model.deleteCallHistoryItem(call)
        }
    }

    private fun purgeContactHistory(contact: Contact) {
        (requireActivity() as BaseActivity).withPermission(Manifest.permission.WRITE_CALL_LOG) {
            model.purgeContactHistory(contact)
        }
    }
}

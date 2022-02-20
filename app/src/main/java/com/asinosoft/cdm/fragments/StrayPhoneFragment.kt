package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.App
import com.asinosoft.cdm.adapters.HistoryDetailsCallsAdapter
import com.asinosoft.cdm.databinding.StrayPhoneFragmentBinding
import com.asinosoft.cdm.viewmodels.ManagerViewModel

/**
 * Окно истории звонков с телефона, которого нет в списке контактов
 */
class StrayPhoneFragment : Fragment() {
    private val model: ManagerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return StrayPhoneFragmentBinding.inflate(inflater).also { v ->
            arguments?.getString("phone")?.let { phone ->
                v.phone.text = phone
                model.getPhoneCalls(phone).let { calls ->
                    v.calls.adapter = HistoryDetailsCallsAdapter(
                        (requireActivity() as App).config,
                        requireContext(),
                        calls
                    )
                }
            }
        }.root
    }
}

package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.adapters.HistoryDetailsCallsAdapter
import com.asinosoft.cdm.databinding.HistoryDetailFragmentBinding
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel

/**
 * Фрагмент вкладки "Истории" в детальной информации по элементу истории
 */
class HistoryDetailFragment : Fragment() {
    private val viewModel: DetailHistoryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bindings = HistoryDetailFragmentBinding.inflate(inflater)

        viewModel.callHistory.observe(
            requireActivity(),
            { calls ->
                bindings.rvContactCalls.adapter =
                    HistoryDetailsCallsAdapter(requireContext(), calls)
            }
        )

        return bindings.root
    }
}

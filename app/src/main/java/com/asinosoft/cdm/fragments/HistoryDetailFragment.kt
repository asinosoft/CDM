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
    private val model: DetailHistoryViewModel by activityViewModels()
    private var v: HistoryDetailFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = HistoryDetailFragmentBinding.inflate(inflater, container, false)
        return v!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        v = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.callHistory.observe(viewLifecycleOwner) { calls ->
            calls?.let {
                v!!.rvContactCalls.adapter = HistoryDetailsCallsAdapter(requireContext(), it)
            }
        }
    }
}

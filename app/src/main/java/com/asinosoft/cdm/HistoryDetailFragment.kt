package com.asinosoft.cdm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.adapters.AdapterCallLogs
import com.asinosoft.cdm.detail_contact.DetailHistoryViewModel

/**
 * Фрагмент вкладки "Истории" в детальной информации по элементу истории
 */
class HistoryDetailFragment : Fragment() {
    private val viewModel: DetailHistoryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerView)
        var lim = LinearLayoutManager(requireContext())
        lim.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = lim

        val callsAdapter = AdapterCallLogs(false, requireContext())
        recyclerView.adapter = callsAdapter

        viewModel.callHistory.observe(requireActivity(), Observer<List<HistoryItem>> {
            callsAdapter.setList(it)
        })
    }
}

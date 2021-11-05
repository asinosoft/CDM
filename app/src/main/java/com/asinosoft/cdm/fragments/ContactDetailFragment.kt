package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.adapters.ContactActionsAdapter
import com.asinosoft.cdm.databinding.HistoryContactFragmentBinding
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel

class ContactDetailFragment : Fragment() {
    private val model: DetailHistoryViewModel by activityViewModels()
    private var v: HistoryContactFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = HistoryContactFragmentBinding.inflate(inflater, container, false)
        return v!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        v = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.contact.observe(viewLifecycleOwner) { contact ->
            contact?.let { v!!.rvContactActions.adapter = ContactActionsAdapter(it) }
        }
    }
}

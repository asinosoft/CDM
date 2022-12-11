package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.adapters.ContactActionsAdapter
import com.asinosoft.cdm.databinding.HistoryContactFragmentBinding
import com.asinosoft.cdm.viewmodels.ManagerViewModel

class ContactDetailFragment : Fragment() {
    private val model: ManagerViewModel by activityViewModels()
    private lateinit var v: HistoryContactFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = HistoryContactFragmentBinding.inflate(inflater, container, false)
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.contact.observe(viewLifecycleOwner) { contact ->
            contact?.let { v.rvContactActions.adapter = ContactActionsAdapter(it) }
        }
    }
}

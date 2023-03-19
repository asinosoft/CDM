package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.asinosoft.cdm.R
import com.asinosoft.cdm.adapters.ContactActionsAdapter
import com.asinosoft.cdm.databinding.HistoryContactFragmentBinding
import com.asinosoft.cdm.helpers.DateHelper
import com.asinosoft.cdm.viewmodels.ManagerViewModel
import java.text.SimpleDateFormat
import java.util.*

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
            contact?.birthday?.let { birthday ->
                v.birthday.text =
                    SimpleDateFormat("d MMMM yyyy г.", Locale.getDefault()).format(birthday)
                val age = DateHelper.age(birthday)
                v.age.text = view.resources.getQuantityString(R.plurals.age, age, age)
                v.birthdayBlock.visibility = View.VISIBLE
            }
        }

        model.contactActions.observe(viewLifecycleOwner) { contactActions ->
            contactActions?.let { v.rvContactActions.adapter = ContactActionsAdapter(it) }
        }
    }
}

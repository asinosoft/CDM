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
import java.text.DateFormat

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
            contact?.let {
                v.rvContactActions.adapter = ContactActionsAdapter(it)
                contact.birthday?.let { date ->
                    val age = DateHelper.age(date)
                    val birthday = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
                    v.age.text = view.resources.getQuantityString(R.plurals.age, age, age)
                    v.birthday.text =
                        view.resources.getString(R.string.type_birthday).format(birthday)
                    v.birth.visibility = View.VISIBLE
                }
            }
        }
    }
}

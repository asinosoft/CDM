package com.asinosoft.cdm

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Класс вкладки "Настройки" в экране детальной информации по элементу истории.
 */
class HistoryOptionFragment : Fragment() {

    private lateinit var viewModel: HistoryOptionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history_option_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HistoryOptionViewModel::class.java)
        // TODO: Use the ViewModel
    }

}

package com.asinosoft.cdm

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class HistoryOptionFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryOptionFragment()
    }

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

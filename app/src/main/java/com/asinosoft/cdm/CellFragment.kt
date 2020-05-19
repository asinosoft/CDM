package com.asinosoft.cdm

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [CellFragment.OnListFragmentInteractionListener] interface.
 */
class CellFragment : DialogFragment() {

    lateinit var hisCellList: HistoryListFrame
    lateinit var parList: ArrayList<HistoryCell>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }


    fun setParam(list: ArrayList<HistoryCell>){
        val listCells = ArrayList<HistoryFragment>() //getListCells(6)
        listCells.add(fragmentManager!!.findFragmentById(R.id.cellhistory1) as HistoryFragment)
        listCells.add(fragmentManager!!.findFragmentById(R.id.cellhistory2) as HistoryFragment)
        listCells.add(fragmentManager!!.findFragmentById(R.id.cellhistory3) as HistoryFragment)
        listCells.add(fragmentManager!!.findFragmentById(R.id.cellhistory4) as HistoryFragment)
        listCells.add(fragmentManager!!.findFragmentById(R.id.cellhistory5) as HistoryFragment)
        listCells.add(fragmentManager!!.findFragmentById(R.id.cellhistory6) as HistoryFragment)
        var i = listCells.count()
        (0 until i).forEach {
            if(list[i].nameContact != "name") {
                listCells[i].setParams(list[i])
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cell_list, container, false)
        return view
    }


    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            CellFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}

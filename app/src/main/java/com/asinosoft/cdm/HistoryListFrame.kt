package com.asinosoft.cdm

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_history.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HistoryListFrame.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HistoryListFrame.newInstance] factory method to
 * create an instance of this fragment.
 */

class HistoryListFrame : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var listCells: ArrayList<HistoryFragment>
    private var cellSize = 65

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_list_frame, container, false)
    }
/*
    fun getListCells(count: Int): ArrayList<HistoryFragment>{
        val transaction = fragmentManager!!.beginTransaction()
        (0..count).forEach {
            var frag = HistoryFragment()
            //frag.onCreateView(layoutInflater, historyListFragment_liner, null)
            transaction.add(R.id.historyListFragment_container, frag, "Cell$it")
        }
        transaction.commitNow()
        //transaction.commitNow()
        val list = ArrayList<HistoryFragment>()
        (0..count).forEach {
            list.add(fragmentManager!!.findFragmentByTag("Cell$it") as HistoryFragment)
        }
        list.forEach {
            //it.onCreateView(layoutInflater, historyListFragment_container, null)
        }
        return  list
    }*/

    fun getList(): ArrayList<HistoryFragment>{
        listCells = ArrayList()
//        getListCells(6)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history1) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history2) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history3) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history4) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history5) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history6) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history7) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history8) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history9) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history10) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history11) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history12) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history13) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history14) as HistoryFragment)
        listCells.add(childFragmentManager!!.findFragmentById(R.id.history15) as HistoryFragment)
        return listCells
    }

    fun setAllParams(listUnique: ArrayList<HistoryCell>, listFull: ArrayList<HistoryCell> = ArrayList(), fonColor: Int = 0){

        listCells = getList()
        var i = listCells.count()
        listUnique.forEach {
            if(i==0) return@forEach
            if (listFull.isNotEmpty())
            listCells[--i].setParams(it, listFull.getArrayContainsNumber(it.numberContact))
            else listCells[--i].setParams(it)
        }
        listCells.forEach {
            it.view!!.visibility = if (it.nameFrame.text == "name") View.INVISIBLE else View.VISIBLE
            it.setFontWithFone(fonColor)
        }
    }

    fun setParamWithDetail(list: ArrayList<HistoryCell>){
        listCells = getList()
        var i = 0
        list.forEach {
            if (i==listCells.count()) return@forEach
            listCells[i++].setParams(it,  onClick = false)
        }
        listCells.forEach {
            it.view!!.visibility = if (it.nameFrame.text == "name") View.INVISIBLE else View.VISIBLE
            it.setFontWithFone(Color.WHITE)
        }
    }

    fun ArrayList<HistoryCell>.getArrayContainsNumber(num: String): ArrayList<HistoryCell>{
        val list = ArrayList<HistoryCell>()
        this.forEach {
            if (it.numberContact == num) list.add(it)
        }
        return list
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryListFrame.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryListFrame().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

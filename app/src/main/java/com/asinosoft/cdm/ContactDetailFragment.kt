package com.asinosoft.cdm.DetailContact

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ContentView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Funcs
import com.asinosoft.cdm.Keys
import com.asinosoft.cdm.R


class ContactDetailFragment : Fragment() {

    val contact = Contact()

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.history_contact_fragment, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getInfoForContact()

        val recyclerView = this.view!!.findViewById<RecyclerView>(R.id.recyclerViewForContact)
        var lim = LinearLayoutManager(this.context!!)
        lim.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = lim
        recyclerView.adapter = AdapterContact(contact.getContactListForDetail())
    }

    private fun getNumber(context: Context): String {
        val sharedpreferences = activity!!.getSharedPreferences(Keys.SharedNum, Context.MODE_PRIVATE)
        return sharedpreferences.getString("TAG_NUM", "")!!
    }

    fun getInfoForContact(){
        val id = Funcs.getContactID(this.context!!, getNumber(this.context!!))
        contact.parseDataCursor(id, this.context!!)
    }



}

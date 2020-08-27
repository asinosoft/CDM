package com.asinosoft.cdm

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.asinosoft.cdm.Metoths.Companion.toggle
import com.asinosoft.cdm.databinding.ActivitySearchBinding
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.keyboard.*

/**
 * Класс экрана поиска
 */
class SearchActivity : AppCompatActivity() {

    private lateinit var v: ActivitySearchBinding
    private lateinit var keyboard: Keyboard
    private var list = listOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(v.root)
        StatusBarUtil.setTranslucentForImageView(this, recyclerView)
        Contacts.initialize(this)
        keyboard = supportFragmentManager.findFragmentById(R.id.keyboard) as Keyboard
        setListens()
    }

    override fun onBackPressed() {
        if (v.layoutKeyboard.height != 1) v.layoutKeyboard.toggle()
        else super.onBackPressed()
    }

    private fun setListens() {
        list = Contacts.getQuery().find().filter { !it.phoneNumbers.isNullOrEmpty() }


        v.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            initialPrefetchItemCount = 11}

        v.recyclerView.adapter = AdapterContacts(list, View.OnClickListener { v: View? ->
            val num = v?.findViewById<TextView>(R.id.number)?.text
            num?.let {
                if (!num.contains(',')) searched(num.toString())
                else dialogNumbers(arrayOf(num.substring(0, num.indexOf(',')), num.substring(num.indexOf(' ')+1, num.lastIndex+1)))
            }
        }, true)

        v.fab.setOnClickListener { v.layoutKeyboard.toggle() }

        keyboard.input_text.doOnTextChanged { text, start, count, after ->
            (v.recyclerView.adapter as AdapterContacts).setFilter(text.toString())
        }

    }

    private fun searched(num: String) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(Keys.number, num) })
        finish()
    }


    private fun dialogNumbers(items: Array<String>) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Выберите номер телефона:")
            setCancelable(false)
            setItems(items) { _, which ->
                searched(items[which])
            }
            show()
        }
    }

}
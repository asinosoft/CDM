package com.asinosoft.cdm

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onItemClick

class ActivityPhoneChoose : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_choose)
        supportActionBar!!.hide()
        val list = find<ListView>(R.id.listView)


        var bundle =intent.extras!![NUM]
        val nums = (bundle as HashSet<String>).toArray()
        if (nums == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        list.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, nums)
        list.onItemClick { p0, p1, p2, p3 ->
            Log.d("NumChoosen: ", nums[p2].toString())
            setResult(Activity.RESULT_OK, Intent().putExtra("Number", nums[p2].toString()))
            finish()
        }
    }

    companion object {
        const val NUM = "NUMBERS"
    }
}

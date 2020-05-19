package com.asinosoft.cdm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class SearchActivity : AppCompatActivity(), Nk_board.OnKeyBoard{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    override fun onKeyPressed(value: Int) {
//        TODO("Not yet implemented")
    }

    override fun onOkPressed(v: View?) {
//        TODO("Not yet implemented")
    }

    override fun onOnDeletePressed(v: View?) {
//        TODO("Not yet implemented")
    }
}

package com.asinosoft.cdm.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.fragments.HistoryDetailFragment
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.R
import com.asinosoft.cdm.fragments.ContactDetailFragment
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel
import com.asinosoft.cdm.fragments.ContactSettingsFragment
import com.jaeger.library.StatusBarUtil
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

/**
 * Активность "Просмотр контакта"
 */
class DetailHistoryActivity : AppCompatActivity() {
    private val viewModel: DetailHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history)

        val contactId = intent.getLongExtra(Keys.id, 0)
        val fragmentPagerItems: FragmentPagerItems
        if (0L != contactId) {
            viewModel.initialize(this, contactId)
            fragmentPagerItems = FragmentPagerItems.with(this)
                .add("История", HistoryDetailFragment().javaClass)
                .add("Контакт", ContactDetailFragment().javaClass)
                .add("Настройки", ContactSettingsFragment().javaClass)
                .create()
        } else {
            val phoneNumber = intent.getStringExtra(Keys.number) ?: ""
            viewModel.initialize(this, phoneNumber)
            fragmentPagerItems = FragmentPagerItems.with(this)
                .add("История", HistoryDetailFragment().javaClass)
                .create()
        }

        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager,
            fragmentPagerItems
        )

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        viewPager.adapter = adapter
        val viewPagerTab = findViewById<SmartTabLayout>(R.id.viewpagertab)
        viewPagerTab.setViewPager(viewPager)
        val imageView = findViewById<ImageView>(R.id.image)
        StatusBarUtil.setTranslucentForImageView(this, imageView)

        imageView.setImageDrawable(viewModel.getContactPhoto(this))
    }
}

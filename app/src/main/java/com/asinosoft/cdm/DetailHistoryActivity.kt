package com.asinosoft.cdm

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.detail_contact.ContactDetailFragment
import com.asinosoft.cdm.detail_contact.DetailHistoryViewModel
import com.asinosoft.cdm.fragments.ContactSettingsFragment
import com.jaeger.library.StatusBarUtil
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

/**
 * Активность "Просмотр контакта"
 */
class DetailHistoryActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    private val viewModel: DetailHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history)

        val contactId = intent.getLongExtra(Keys.id, 0)
        val phoneNumber = intent.getStringExtra(Keys.number) ?: ""
        viewModel.initialize(this, contactId, phoneNumber)

        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager,
            FragmentPagerItems.with(this)
                .add("История", HistoryDetailFragment().javaClass)
                .add("Контакт", ContactDetailFragment().javaClass)
                .add("Настройки", ContactSettingsFragment().javaClass)
                .create()
        )

        viewPager = findViewById(R.id.viewpager)
        viewPager.adapter = adapter
        val viewPagerTab = findViewById<SmartTabLayout>(R.id.viewpagertab)
        viewPagerTab.setViewPager(viewPager)
        val imageView = findViewById<ImageView>(R.id.image)
        StatusBarUtil.setTranslucentForImageView(this, imageView)

        imageView.setImageDrawable(viewModel.getContactPhoto(this))
    }
}

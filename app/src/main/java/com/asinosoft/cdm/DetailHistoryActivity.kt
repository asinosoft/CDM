package com.asinosoft.cdm

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.detail_contact.ContactDetailFragment
import com.asinosoft.cdm.detail_contact.DetailHistoryViewModel
import com.asinosoft.cdm.fragments.ContactSettingsFragment
import com.asinosoft.cdm.fragments.ScrollViewListener
import com.jaeger.library.StatusBarUtil
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import kotlinx.android.synthetic.main.activity_detail_history.*

/**
 * Активномть "Просмотр контакта"
 */
class DetailHistoryActivity : AppCompatActivity(), ScrollViewListener {

    lateinit var viewPager: ViewPager
    private val viewModel: DetailHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history)

        viewModel.initialize(
            phoneNumber = intent.getStringExtra(Keys.number) ?: "",
            contactID = intent.getLongExtra(Keys.id, 0)
        )

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

        imageView.setImageDrawable(viewModel.getContactPhoto())
    }

    override fun onScrolledToTop() {
        drag_layout.setTouchMode(true)
    }
}

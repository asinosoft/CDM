package com.asinosoft.cdm.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.R
import com.asinosoft.cdm.fragments.ContactDetailFragment
import com.asinosoft.cdm.fragments.ContactSettingsFragment
import com.asinosoft.cdm.fragments.HistoryDetailFragment
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

/**
 * Активность "Просмотр контакта"
 */
class DetailHistoryActivity : BaseActivity() {
    private val viewModel: DetailHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.analytics.logEvent("activity_contact", Bundle.EMPTY)
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
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> Firebase.analytics.logEvent("contact_tab_calls", Bundle.EMPTY)
                    1 -> Firebase.analytics.logEvent("contact_tab_actions", Bundle.EMPTY)
                    2 -> Firebase.analytics.logEvent("contact_tab_settings", Bundle.EMPTY)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        val viewPagerTab = findViewById<SmartTabLayout>(R.id.viewpagertab)
        viewPagerTab.setViewPager(viewPager)
        val imageView = findViewById<ImageView>(R.id.image)

        imageView.setImageDrawable(viewModel.getContactPhoto(this))
    }
}

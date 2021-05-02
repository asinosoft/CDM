package com.asinosoft.cdm

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.detail_contact.ContactDetailFragment
import com.asinosoft.cdm.detail_contact.DetailHistoryViewModel
import com.asinosoft.cdm.fragments.ContactSettingsFragment
import com.jaeger.library.StatusBarUtil
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

/**
 * Активномть "Просмотр контакта"
 */
class DetailHistoryActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    private val viewModel: DetailHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history)

        val phoneNumber = intent.getStringExtra(Keys.number) ?: ""
        val contactID = intent.getLongExtra(Keys.id, 0)
        val contact = App.contactRepository.getContactById(contactID)
            ?: Contact(0, phoneNumber).apply {
                actions.add(Action(0, Action.Type.PhoneCall, phoneNumber, ""))
            }
        viewModel.initialize(contact)

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
}

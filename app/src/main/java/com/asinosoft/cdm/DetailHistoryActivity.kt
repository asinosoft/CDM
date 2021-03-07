package com.asinosoft.cdm

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ogaclejapan.smarttablayout.SmartTabLayout
import androidx.viewpager.widget.ViewPager
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import android.net.Uri
import android.provider.ContactsContract
import java.io.IOException
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.asinosoft.cdm.detail_contact.ContactDetailFragment
import com.asinosoft.cdm.fragments.ContactSettingsFragment
import com.asinosoft.cdm.fragments.NumberGetter
import com.asinosoft.cdm.fragments.ScrollViewListener
import com.asinosoft.cdm.globals.Globals
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_detail_history.*
import java.lang.Exception

class DetailHistoryActivity : AppCompatActivity(), ScrollViewListener, NumberGetter {

    lateinit var viewPager: ViewPager
    lateinit var settingsFragment: ContactSettingsFragment
    var contactNumber: String? = null

    override fun getNumber(): String? = contactNumber

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history)
        contactNumber = intent.getStringExtra(Keys.number)


        settingsFragment = ContactSettingsFragment()
        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add("История", HistoryDetailFragment().javaClass)
                .add("Контакт", ContactDetailFragment().javaClass)
                .add("Настройки", settingsFragment.javaClass)
                .create()
        )

        setNum()

        viewPager = findViewById(R.id.viewpager)
        viewPager.adapter = adapter
        val viewPagerTab = findViewById<SmartTabLayout>(R.id.viewpagertab)
        viewPagerTab.setViewPager(viewPager)
        val imageView = findViewById<ImageView>(R.id.image)
        StatusBarUtil.setTranslucentForImageView(this, imageView)
        try {
            var id: Long? = intent.getStringExtra(Keys.id)?.toLong()
            if (id == null) id = Globals.passedContactId
            imageView.setImageDrawable(getPhotoSafety(id!!.toLong()))
        } catch (e: Exception) {
        }

    }

    private fun setNum() {
        val sharedPreferences = getSharedPreferences(Keys.SharedNum, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("TAG_NUM", intent.getStringExtra(Keys.number))
        editor.apply()
    }

    private fun getPhotoSafety(id: Long): Drawable {
        val photo = getPhotoNow(id)
        return if (photo != null) BitmapDrawable(photo) else ContextCompat.getDrawable(
            this,
            R.drawable.contact_unfoto
        ) as Drawable
    }

    private fun getPhotoNow(id: Long): Bitmap? = openDisplayPhoto(contactId = id)

    private fun openDisplayPhoto(contactId: Long): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val displayPhotoUri = Uri.withAppendedPath(
            contactUri,
            ContactsContract.Contacts.Photo.DISPLAY_PHOTO
        )
        try {
            val fd = contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
            return BitmapFactory.decodeStream(fd!!.createInputStream())
        } catch (e: IOException) {
            return null
        }

    }

    override fun onScrolledToTop() {
        drag_layout.setTouchMode(true)
    }
}


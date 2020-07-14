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
import com.jaeger.library.StatusBarUtil
import java.lang.Exception


class DetailHistoryActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history)

        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add("История", HistoryDetailFragment().javaClass)
//                .add("Настройки", HistoryOptionFragment::class.java)
                .add("Контакт", ContactDetailFragment().javaClass)
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
        imageView.setImageDrawable(getPhotoSaffety(intent.getStringExtra(Keys.id)!!.toLong()))
        }catch (e: Exception){}

    }

    private fun setNum() {
        val sharedpreferences = getSharedPreferences(Keys.SharedNum, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        editor.putString("TAG_NUM", intent.getStringExtra(Keys.number))
        editor.apply()
    }

    private fun getPhotoSaffety(id: Long): Drawable {
        val photo = getPhotoNow(id)
        return if (photo != null) BitmapDrawable(photo) else ContextCompat.getDrawable(
            this,
            R.drawable.contact_unfoto
        ) as Drawable
    }

    private fun getPhotoNow(id: Long): Bitmap? = openDisplayPhoto(contactId = id)

    private fun openDisplayPhoto(contactId: Long): Bitmap? {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val displayPhotoUri = Uri.withAppendedPath(contactUri,
            ContactsContract.Contacts.Photo.DISPLAY_PHOTO
        )
        try {
            val fd = contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
            return BitmapFactory.decodeStream(fd!!.createInputStream())
        } catch (e: IOException) {
            return null
        }

    }


}


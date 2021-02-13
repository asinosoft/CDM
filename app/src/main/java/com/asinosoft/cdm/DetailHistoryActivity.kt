package com.asinosoft.cdm

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.detail_contact.Contact
import com.asinosoft.cdm.detail_contact.ContactDetailFragment
import com.asinosoft.cdm.detail_contact.VCardLoader
import com.jaeger.library.StatusBarUtil
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import kotlinx.android.synthetic.main.activity_detail_history.*
import java.io.IOException


class DetailHistoryActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    lateinit var toolbar: Toolbar

    lateinit var mContact: Contact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_history)

        toolbar = findViewById<Toolbar>(R.id.detailHisToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        supportActionBar!!.title = ""


        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add("История", HistoryDetailFragment().javaClass)
//                .add("Настройки", HistoryOptionFragment::class.java)
                .add("Контакт", ContactDetailFragment().javaClass)
                .create()
        )

        setNum()
        contact_name.text = getContactName(intent.getStringExtra(Keys.id)!!.toString())

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_history_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.redContact -> editContact()
            R.id.shareContact -> showShareContactDialog()

            else -> ""
        }

        return super.onOptionsItemSelected(item)
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
        val contactUri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactId
        )
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

    private fun getContactName(id: String):String {

        var name = ""

        var isFirst: Boolean = true

        val cursor: Cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI, null,
            ContactsContract.Data.CONTACT_ID + "=?", arrayOf(id), null
        )!!

        cursor.moveToFirst()
        while(!cursor.isAfterLast()){

            val displayName =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))


            if(isFirst){
                isFirst = false
                name = displayName
            }

            cursor.moveToNext()
        }

        return name
    }

    private fun editContact() {
        val mUri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI, intent.getStringExtra(
                Keys.id
            )!!.toLong()
        )
        val intent = Intent(Intent.ACTION_EDIT)
        intent.setData(mUri)
        startActivity(intent)
    }

    private fun showShareContactDialog() {
        try {
            val mItems = arrayOf(
                getString(R.string.send_v_card_text),
                getString(R.string.send_text)
            )
            val builder = AlertDialog.Builder(this@DetailHistoryActivity)
            builder.setTitle(getString(R.string.share_contact_text))
            builder.setItems(mItems) { dialog, which ->
                when (which) {
                    0 -> {
                        Log.d("myLog", " --- vCard ---")
                        sendVCard()
                    }
                    1 -> {
                        Log.d("myLog", " --- text --- ")
                        sendText()
                    }
                }
            }
            builder.show()
        }catch (e: java.lang.Exception) {
            Log.e("DetailContactLog", "FullContactDetail showShareContactDialog", e)
        }
    }

    private fun sendVCard() {
        var contactId = intent.getStringExtra(Keys.id)!!.toLong()
        var contactName = getContactName(intent.getStringExtra(Keys.id)!!.toString())
        if(null != contactId) {
            val loader = VCardLoader(baseContext, contactId.toString(), contactName)
            loader.makeRequest()
        }else{
            Log.e("DetailContactLog", "sendVCard: contactId = null")
        }
    }

    private fun sendText() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, makeContactsInfoText())
        val shareVia = baseContext.getString(R.string.shared_via_text)
        startActivity(Intent.createChooser(sharingIntent, shareVia))
    }

    private fun makeContactsInfoText(): String? {
        return try {
            var result = ""
            if (null != mContact) {
                for (number in mContact.mPhoneNumbers) {
                    result += """
                        ${baseContext.getString(R.string.phone_text)}: $number
                        
                        """.trimIndent()
                }
                for (number in mContact.mWhatsAppNumbers) {
                    result += """
                        ${baseContext.getString(R.string.type_whatsapp)}: $number
                        
                        """.trimIndent()
                }
                for (number in mContact.mViberNumbers) {
                    result += """
                        ${baseContext.getString(R.string.type_viber)}: $number
                        
                        """.trimIndent()
                }
                if (null != mContact.mEmailAdress) {
                    result += """
                        ${baseContext.getString(R.string.type_e_mail)}: ${mContact.mEmailAdress}
                        
                        """.trimIndent()
                }
                if (null != mContact.mSkypeName) {
                    result += """
                        ${baseContext.getString(R.string.type_skype)}: ${mContact.mSkypeName}
                        
                        """.trimIndent()
                }
            }
            result
        } catch (e: java.lang.Exception) {
            Log.e("myLog", "FullContactDetail makeContactsInfoText", e)
            ""
        }
    }


}


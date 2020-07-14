package com.asinosoft.cdm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.os.Vibrator
import android.provider.ContactsContract
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.databinding.ActivityManagerBinding
import com.github.florent37.runtimepermission.RuntimePermission.askPermission
import com.github.tamir7.contacts.Contact
import com.github.tamir7.contacts.Contacts
import com.jaeger.library.StatusBarUtil
import com.skydoves.powermenu.kotlin.ActivityPowerMenuLazy
import com.skydoves.powermenu.kotlin.powerMenu
import jp.wasabeef.recyclerview.animators.LandingAnimator
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.vibrator
import org.jetbrains.anko.wrapContent
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf

/**
 * Основной класс приложения, отвечает за работу главного экрана (нового) приложения
 */
class ManagerActivity : AppCompatActivity() {

    companion object {
        const val ACTIVITY_PICK_CONTACT = 13
        const val ACTIVITY_SETTINGS = 12
    }

    /**
     * Элемент, хранящий ссылки на все представления привязанного макета
     */
    private lateinit var v: ActivityManagerBinding

    /**
     * ViewModel главного экрана, отвечает за всю фоновую логику
     */
    private lateinit var viewModel: ManagerViewModel
    private val moreMenu by powerMenu(MoreMenuFactory::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(v.root)
        getPermission()
        viewModel = ViewModelProvider(this).get(ManagerViewModel::class.java)
        StatusBarUtil.setTranslucentForImageView(this, v.container)
        startForView()
    }

    private fun getPermission() {
        askPermission(this).onDenied { getPermission() }.ask()
    }


    fun startForView(){
        viewModel.start(v, this, lifecycle, pickedContact = { pickContact() }, settingsOpen = { settingOpen(it) }, activity = this)
        viewModel.initViews()
    }

    private fun pickContact() {
        startActivityForResult(
            Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
            ACTIVITY_PICK_CONTACT
        )
    }

    private fun settingOpen(settings: Settings){
        startActivityForResult(Intent(this, SettingsActivity::class.java).apply {putExtra(Keys.Settings, com.google.gson.Gson().toJson(settings))},
            ACTIVITY_SETTINGS)
    }

    override fun onStop() {
        viewModel.saveCirs()
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("${this.javaClass}", "onDestroy: Destroy!")
        viewModel.onDestroy()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            viewModel.onResult(requestCode, requestCode, data)
        } else if (requestCode == ACTIVITY_SETTINGS && resultCode == Activity.RESULT_OK) {
            viewModel.start(v, this, lifecycle, pickedContact = { pickContact() }, settingsOpen = { settingOpen(it) }, activity = this)
            viewModel.initViews(false)
        }
    }

}

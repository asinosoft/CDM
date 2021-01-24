package com.asinosoft.cdm

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.provider.ContactsContract
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Actions.*
import com.asinosoft.cdm.Metoths.Companion.action
import com.asinosoft.cdm.Metoths.Companion.callPhone
import com.asinosoft.cdm.Metoths.Companion.checkMoving
import com.github.tamir7.contacts.Contact
import com.asinosoft.cdm.Metoths.Companion.setSize
import com.asinosoft.cdm.Metoths.Companion.diff
import com.asinosoft.cdm.Metoths.Companion.diffAction
import com.asinosoft.cdm.Metoths.Companion.diffVisible
import com.asinosoft.cdm.Metoths.Companion.dp
import com.asinosoft.cdm.Metoths.Companion.mailToEmail
import com.asinosoft.cdm.Metoths.Companion.makeTouch
import com.asinosoft.cdm.Metoths.Companion.openCardContact
import com.asinosoft.cdm.Metoths.Companion.openDetailContact
import com.asinosoft.cdm.Metoths.Companion.openTelegram
import com.asinosoft.cdm.Metoths.Companion.openViber
import com.asinosoft.cdm.Metoths.Companion.sendSMS
import com.asinosoft.cdm.Metoths.Companion.setImageAction
import com.asinosoft.cdm.Metoths.Companion.setTranslate
import com.asinosoft.cdm.Metoths.Companion.toPointF
import com.asinosoft.cdm.Metoths.Companion.toVisibility
import com.asinosoft.cdm.Metoths.Companion.translateDiff
import com.asinosoft.cdm.Metoths.Companion.translateTo
import com.asinosoft.cdm.Metoths.Companion.vibrateSafety
import com.google.android.material.snackbar.Snackbar
import com.skydoves.powermenu.PowerMenu
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.vibrator
import java.net.URL

class CircleImage@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val powerMenu: PowerMenu? = null,
    private val swiping: Boolean = false,
    var deleteListener: () -> Unit = {},
    var replaceListenerForHolder: () -> Unit = {},
    var replaceListener: (RecyclerView.ViewHolder) -> Unit = {},
    var pickContactForNum: () -> Unit = {},
    var pickContact: (Int) -> Unit = {},
    var menuEnable: Boolean = false,
    var dragListener: () -> Unit = {},
    var deleteCir: CircularImageView? = null,
    var editCir: CircularImageView? = null,
    var openEditForPos: (Contact, ContactSettings) -> Unit = { _, _ -> },
    var openEdit: (Int, Contact, ContactSettings) -> Unit = {_, _, _ -> },
    var touchDownForIndex: () -> Unit = {},
    var touchDown: (Int) -> Unit = {},
    var lockableNestedScrollView: LockableNestedScrollView? = null,
    var selectedNumber : String? = null
): CircularImageView(context, attrs, defStyleAttr) {//TODO: Добавь в настройках изменение типа управления кнопками (перетаскивание, меню)

    companion object{
        const val CONTACT_UNFOTO = R.drawable.contact_unfoto
    }

    var contactSettings: ContactSettings? = null
    set(value) {
        field = value
        if (value != null) updateSetting(value)
    }
    var contact: Contact? = null
    set(value) {
        field = value
        if (value != null && value.photoUri != null) updatePhoto(value.photoUri.toUri())
        else setImageResource(CONTACT_UNFOTO)
    }

    var size: Int = this.width
    set(value) {
        field = value
        updateRadius()
        onChangeSize(value)
    }
    private var actionImage: CircularImageView? = null
    var animationRadius: Float = size.toFloat()
    private var touchStart: PointF? = null
    private var cirStart: PointF? = null
    var directActions: DirectActions? = null
    var animationDuration = 0L
    private var isMoving = false
    private var isLongClick = false

    init {
        initClick()
        if (swiping) initTouch()
        if (menuEnable) initLongClickWithMenu()
        else initLongClickWithDrag()
        if(contact == null) setImageResource(R.drawable.plus)
        contactSettings?.let {
            updateSetting(it)
        }
    }

    private fun updateSetting(settings: ContactSettings) {
        borderWidth = settings.borderWidth
        borderColor = settings.borderColor
    }

    fun setOptionalCirsVisible(b: Boolean){
        deleteCir?.apply { visibility = b.toVisibility(); bringToFront(); setSize(animationRadius.toInt())}
        editCir?.apply { visibility = b.toVisibility(); bringToFront(); setSize(animationRadius.toInt())}
    }

    private fun initClick() {
        onClick {
            if (isLongClick){
                isLongClick = false
                return@onClick
            }
            if (!isMoving) {
                if (contact == null) {
                    pickContactForNum()
                } else openDetailContact(contact!!.phoneNumbers.first().normalizedNumber, context)
            }
        }
    }

    private fun initLongClickWithDrag() {
        onLongClick {
            if (!isMoving) {
                isLongClick = true
                dragListener()
                setOptionalCirsVisible(true)
                makeTouch(MotionEvent.ACTION_UP)
            }
        }
    }

    private fun updatePhoto(uri: Uri) {
        setImageURI(uri)
    }

    private fun initLongClickWithMenu() {
        powerMenu?.setOnMenuItemClickListener { position, item ->
            when(position){
                0 -> replaceListenerForHolder()
                2 -> deleteListener()
            }
        }
        onLongClick {
            if (!isMoving) {
                powerMenu?.showAsAnchorCenter(this@CircleImage, 200, 200)
                isLongClick = true
                makeTouch(MotionEvent.ACTION_UP)
            }
        }
    }

    fun setActionImage(view: CircularImageView){
        actionImage = view
        actionImage?.setSize((size - shadowRadius * 2).toInt())
    }

    fun updateRadius(){
        animationRadius = size.toFloat() * 0.9f
    }

    private fun onChangeSize(value: Int) {
        val t = ((value - shadowRadius * 2) * 0.8f).toInt()
        actionImage?.layoutParams = ConstraintLayout.LayoutParams(t, t)
        this.layoutParams = ConstraintLayout.LayoutParams(value, value).apply {
            this.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            this.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            this.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            this.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            isFocusable = true
            isClickable = true
        }
    }

    private fun initTouch(){
        onTouch { _, event -> if (contact != null)
            touchEvent(event)
        }
    }


     private fun touchEvent(event: MotionEvent) {
         when (event.action) {
             MotionEvent.ACTION_DOWN -> onTouchDown(event)
             MotionEvent.ACTION_MOVE -> onTouchMove(event)
             MotionEvent.ACTION_UP -> onTouchUp(event)
         }
    }

    private fun onTouchUp(event: MotionEvent) {
        touchStart?.let {
            Log.d("CircleImage", "Action TouchUp -> (${this.x}; ${this.y}) --> (${event.rawX}; ${event.rawY}) ")
            var diff = it.diff(event, animationRadius)
            this.setTranslate(cirStart!!, 500L)
            touchStart = null
            cirStart = null
            directActions?.action(diff.diffAction(animationRadius))?.let{action ->
                if (actionImage?.isVisible == true) try {
                    startAction(action)
                }catch (e: Exception){e.printStackTrace()}
            }
            actionImage?.isVisible = false
//            isMoving = false
        }
        lockableNestedScrollView?.setScrollingEnabled(true)
    }

    private fun onTouchMove(event: MotionEvent) {
        touchStart?.let {
            Log.d("CircleImage", "Action Move -> (${this.x}; ${this.y}) --> start = (${it.x}; ${it.y}); end = (${event.rawX}; ${event.rawY}), radius = $animationRadius ")
            var diff = it.diff(event, animationRadius)
            isMoving = !diff.checkMoving(size / 10f)
            Log.d("${this.javaClass}", "onTouchMove: isMoving = $isMoving")
            this.translateDiff(cirStart!!, diff, animationDuration)
            actionImage?.apply {
                isVisible = diff.diffVisible(animationRadius).also {vis -> if (vis && !isVisible) context.vibrator.vibrateSafety(ManagerViewModel.VIBRO) }
                directActions?.action(diff.diffAction(animationRadius))?.let {action ->
                    this.setImageAction(action)
                }
            }
        }
    }

    private fun startAction(action: Actions) {

        val num = contact!!.phoneNumbers[if (contactSettings?.usedNum != null) contactSettings?.usedNum!! else 0].normalizedNumber
        when (action){
            WhatsApp -> Metoths.openWhatsApp(num, context)
            Viber -> openViber(num, context)
            Telegram -> openTelegram(num, context)
            PhoneCall -> callPhone(num, context)
            Email -> {
                if (!contact!!.emails.isNullOrEmpty()) contact!!.emails.first()?.let { mailToEmail(it.address, context) }
                else Snackbar.make(rootView, "Контакт без почты!", Snackbar.LENGTH_SHORT).show()
            }
            Sms -> sendSMS(num, context)
        }
    }

    private fun onTouchDown(event: MotionEvent) {
        Log.d("CircleImage", "Action TouchDown -> (${this.x}; ${this.y}) --> (${event.rawX}; ${event.rawY}) ")
        val number = contact?.phoneNumbers?.first()?.number
        number?.let {
            this.directActions = Loader(this.context).loadContactSettings(it).toDirectActions()
        }
        touchDownForIndex()
        touchStart = event.toPointF()
        cirStart = this.toPointF()
        isLongClick = false
        actionImage?.apply {
            setSize((size*0.8f).toInt())
            translateTo(this@CircleImage, size * 0.1f)
            isVisible = false
        }
        lockableNestedScrollView?.apply { setScrollingEnabled(false) }
    }
}
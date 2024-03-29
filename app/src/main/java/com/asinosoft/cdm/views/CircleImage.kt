package com.asinosoft.cdm.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.asinosoft.cdm.R
import com.asinosoft.cdm.api.Analytics
import com.asinosoft.cdm.data.Contact
import com.asinosoft.cdm.data.DirectActions
import com.asinosoft.cdm.helpers.AvatarHelper
import com.asinosoft.cdm.helpers.Keys
import com.asinosoft.cdm.helpers.Metoths.Companion.action
import com.asinosoft.cdm.helpers.Metoths.Companion.checkMoving
import com.asinosoft.cdm.helpers.Metoths.Companion.diff
import com.asinosoft.cdm.helpers.Metoths.Companion.diffAction
import com.asinosoft.cdm.helpers.Metoths.Companion.diffVisible
import com.asinosoft.cdm.helpers.Metoths.Companion.makeTouch
import com.asinosoft.cdm.helpers.Metoths.Companion.setImageAction
import com.asinosoft.cdm.helpers.Metoths.Companion.setSize
import com.asinosoft.cdm.helpers.Metoths.Companion.setTranslate
import com.asinosoft.cdm.helpers.Metoths.Companion.toPointF
import com.asinosoft.cdm.helpers.Metoths.Companion.toVisibility
import com.asinosoft.cdm.helpers.Metoths.Companion.translateDiff
import com.asinosoft.cdm.helpers.Metoths.Companion.translateTo
import com.asinosoft.cdm.helpers.Metoths.Companion.vibrateSafety
import com.asinosoft.cdm.helpers.vibrator

class CircleImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CircularImageView(context, attrs, 0) {
    // TODO: Добавь в настройках изменение типа управления кнопками (перетаскивание, меню)
    var openContact: (Contact) -> Unit = {}
    var pickContact: () -> Unit = {}
    var dragListener: () -> Unit = {}
    var deleteCir: CircularImageView? = null
    var editCir: CircularImageView? = null
    var touchDownForIndex: () -> Unit = {}
    var touchDown: (Int) -> Unit = {}
    var lockableNestedScrollView: LockableLayoutManager? = null

    var contact: Contact? = null
        set(value) {
            field = value
            setImageDrawable(value?.getAvatar(context, AvatarHelper.SHORT))
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
    private var animationDuration = 0L
    private var isMoving = false
    private var isLongClick = false

    init {
        initClick()
        initTouch()
        initLongClickWithDrag()
        if (contact == null) setImageResource(R.drawable.ic_add_contact)
    }

    fun setOptionalCirsVisible(b: Boolean) {
        deleteCir?.apply {
            visibility = b.toVisibility(); bringToFront(); setSize(animationRadius.toInt())
        }
        editCir?.apply {
            visibility = b.toVisibility(); bringToFront(); setSize(animationRadius.toInt())
        }
    }

    private fun initClick() {
        setOnClickListener {
            if (isLongClick) {
                isLongClick = false
            } else if (!isMoving) {
                contact?.let {
                    openContact(it)
                } ?: pickContact()
            }
        }
    }

    private fun initLongClickWithDrag() {
        setOnLongClickListener {
            if (isMoving) {
                false
            } else {
                isLongClick = true
                dragListener()
                setOptionalCirsVisible(true)
                makeTouch(MotionEvent.ACTION_UP)
                true
            }
        }
    }

    fun setActionImage(view: CircularImageView) {
        actionImage = view
        actionImage?.setSize((size - shadowRadius * 2).toInt())
    }

    private fun updateRadius() {
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initTouch() {
        setOnTouchListener { _, event ->
            if (contact != null) {
                touchEvent(event)
            }
            false
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
            val diff = it.diff(event, animationRadius)
            this.setTranslate(cirStart!!, 500L)
            touchStart = null
            cirStart = null
            directActions?.action(diff.diffAction(animationRadius))?.let { action ->
                if (actionImage?.isVisible == true) try {
                    Analytics.logFavoriteAction(direction.name)
                    action.perform(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            actionImage?.isVisible = false
        }
        lockableNestedScrollView?.setScrollingEnabled(true)
    }

    private fun onTouchMove(event: MotionEvent) {
        touchStart?.let {
            val diff = it.diff(event, animationRadius)
            isMoving = !diff.checkMoving(size / 10f)
            this.translateDiff(cirStart!!, diff, animationDuration)
            actionImage?.apply {
                isVisible = diff.diffVisible(animationRadius).also { vis ->
                    if (vis && !isVisible) context.vibrator.vibrateSafety(Keys.VIBRO, 255)
                }
                directActions?.action(diff.diffAction(animationRadius))?.let { action ->
                    this.setImageAction(action.type)
                }
            }
        }
    }

    private fun onTouchDown(event: MotionEvent) {
        touchDownForIndex()
        touchStart = event.toPointF()
        cirStart = this.toPointF()
        isLongClick = false
        actionImage?.apply {
            setSize((size * 0.8f).toInt())
            translateTo(this@CircleImage, size * 0.1f)
            isVisible = false
        }
        lockableNestedScrollView?.setScrollingEnabled(false)
    }
}

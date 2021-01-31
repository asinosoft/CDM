package com.asinosoft.cdm.fragments

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.ScrollView
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.*
import kotlinx.android.synthetic.main.contact_settings.*
import kotlinx.android.synthetic.main.settings_layout.*
import kotlinx.android.synthetic.main.settings_layout.cirBottom
import kotlinx.android.synthetic.main.settings_layout.cirChoose1
import kotlinx.android.synthetic.main.settings_layout.cirChoose2
import kotlinx.android.synthetic.main.settings_layout.cirChoose3
import kotlinx.android.synthetic.main.settings_layout.cirLeft
import kotlinx.android.synthetic.main.settings_layout.cirRight
import kotlinx.android.synthetic.main.settings_layout.cirTop
import org.jetbrains.anko.image

interface ScrollViewListener {
    fun onScrolledToTop()
}

interface NumberGetter {
    fun getNumber(): String?
}

class ContactSettingsFragment : Fragment() {
    lateinit var scrollView: LockableScrollView
    lateinit var draggedCir: CircularImageView
    var contacNumber: String? = null
    var contactSettins: Settings? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.contact_settings, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contacNumber = (activity as NumberGetter).getNumber()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scrollView = view.findViewById(R.id.scrollView)
        scrollView.setScrollingEnabled(false)
        number.text = contacNumber

        scrollView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (oldScrollY > 0 && scrollY == 0) {
                (activity as ScrollViewListener).onScrolledToTop()
                //scrollView.setScrollingEnabled(false)
            }
        }
        val loader = Loader(activity as Context)
        contactSettins = Loader(activity as Context).loadContactSettings(contacNumber as String)
        if(contactSettins == null) contactSettins = loader.loadSettings()
        setAllCirs(contactSettins?.borderWidthCirs, contactSettins?.colorBorder)
        setData(contactSettins as Settings)
    }

    private fun setCirData(cir: CircularImageView) {
        cir.setImageResource(getResDrawable(cir.action))
    }

    private fun getResDrawable(action: Actions): Int {
        return when (action) {
            Actions.WhatsApp -> R.drawable.whatsapp_192
            Actions.Sms -> R.drawable.sms_192
            Actions.Email -> R.drawable.email_192
            Actions.PhoneCall -> R.drawable.telephony_call_192
            Actions.Viber -> R.drawable.viber
            Actions.Telegram -> R.drawable.telegram
        }
    }

    private fun setDragListener(cir: CircularImageView) {
        cir.setOnLongClickListener {
            it.bringToFront()
            draggedCir = cir
            val item = ClipData.Item(it.tag as? CharSequence)
            val dragData = ClipData(
                it.tag as? CharSequence,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item
            )
            val myShadow = View.DragShadowBuilder(it)

            it.startDrag(
                dragData,   // the data to be dragged
                myShadow,   // the drag shadow builder
                null,       // no need to use local data
                0           // flags (not currently used, set to 0)
            )
        }

        cir.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Determines if this View can accept the dragged data
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Applies a green tint to the View. Return true; the return value is ignored.
                    cir.swapCir(draggedCir)

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    cir.invalidate()
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION ->
                    // Ignore the event
                    true
                DragEvent.ACTION_DRAG_EXITED -> {
                    // Re-sets the color tint to blue. Returns true; the return value is ignored.
                    cir.swapCir(draggedCir)

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    scrollView.setScrollingEnabled(true)
                    false
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    scrollView.setScrollingEnabled(true)
                    false
                }
                else -> {
                    // An unknown action type was received.
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.")
                    false
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        saveAll()
    }

    private fun CircularImageView.swapCir(c: CircularImageView) {
        this.image = c.image.also { c.image = this.image }
        this.action = c.action.also { c.action = this.action }
    }

    private fun setData(settings: Settings) {

        cirRight.action = settings.rightButton
        cirLeft.action = settings.leftButton
        cirTop.action = settings.topButton
        cirBottom.action = settings.bottomButton
        cirChoose1.action = settings.chooserButton1
        cirChoose2.action = settings.chooserButton2
        cirRight.let(this::setCirData)
        cirLeft.let(this::setCirData)
        cirTop.let(this::setCirData)
        cirBottom.let(this::setCirData)
        cirChoose1.let(this::setCirData)
        cirChoose2.let(this::setCirData)

        cirBottom.let(this@ContactSettingsFragment::setDragListener)
        cirTop.let(this@ContactSettingsFragment::setDragListener)
        cirLeft.let(this@ContactSettingsFragment::setDragListener)
        cirRight.let(this@ContactSettingsFragment::setDragListener)
        cirChoose1.let(this@ContactSettingsFragment::setDragListener)
        cirChoose2.let(this@ContactSettingsFragment::setDragListener)

    }

    fun enableScroll(enable: Boolean) {
        scrollView?.let {
            it.setScrollingEnabled(enable)
        }
    }

    private fun saveAll() {
        Loader(activity as Context).saveContactSettings(
            contacNumber as String, contactSettins?.copy(
                rightButton = cirRight.action,
                leftButton = cirLeft.action,
                topButton = cirTop.action,
                bottomButton = cirBottom.action,
                chooserButton1 = cirChoose1.action,
                chooserButton2 = cirChoose2.action
            ) as Settings
        )
    }

    private fun setAllCirs(width: Int? = null, @ColorInt color: Int? = null) {
        width?.let {
            cirBottom.borderWidth = it.toFloat()
            cirChoose1.borderWidth = it.toFloat()
            cirChoose2.borderWidth = it.toFloat()
            cirChoose3.borderWidth = it.toFloat()
            cirTop.borderWidth = it.toFloat()
            cirRight.borderWidth = it.toFloat()
            cirLeft.borderWidth = it.toFloat()

        }

        color?.let {
            cirBottom.borderColor = it
            cirChoose1.borderColor = it
            cirChoose2.borderColor = it
            cirChoose3.borderColor = it
            cirTop.borderColor = it
            cirRight.borderColor = it
            cirLeft.borderColor = it
        }
    }
}
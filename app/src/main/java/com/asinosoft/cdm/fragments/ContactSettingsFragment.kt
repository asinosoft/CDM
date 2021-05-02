package com.asinosoft.cdm.fragments

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.asinosoft.cdm.CircularImageView
import com.asinosoft.cdm.Metoths
import com.asinosoft.cdm.adapters.ActionListAdapter
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.DirectActions
import com.asinosoft.cdm.data.Settings
import com.asinosoft.cdm.databinding.ContactSettingsBinding
import com.asinosoft.cdm.detail_contact.DetailHistoryViewModel
import org.jetbrains.anko.image

class ContactSettingsFragment : Fragment() {
    private val viewModel: DetailHistoryViewModel by activityViewModels()
    private lateinit var v: ContactSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = ContactSettingsBinding.inflate(inflater)
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        v.number.text = viewModel.getPhoneNumber()
        v.cirLeft.direction = Metoths.Companion.Direction.LEFT
        v.cirRight.direction = Metoths.Companion.Direction.RIGHT
        v.cirTop.direction = Metoths.Companion.Direction.TOP
        v.cirBottom.direction = Metoths.Companion.Direction.DOWN

        v.cirBottom.let(this@ContactSettingsFragment::setDragListener)
        v.cirTop.let(this@ContactSettingsFragment::setDragListener)
        v.cirLeft.let(this@ContactSettingsFragment::setDragListener)
        v.cirRight.let(this@ContactSettingsFragment::setDragListener)

        val settings = viewModel.getGlobalSettings()
        setAllCirs(settings)
        setData(viewModel.getContactActions())

        v.rvActions.layoutManager = GridLayoutManager(requireContext(), 1 + settings.columnsCirs)
        v.rvActions.adapter = ActionListAdapter(
            viewModel.getAvailableActions(),
            viewModel.getGlobalSettings()
        )
    }

    private fun setCirData(cir: CircularImageView) {
        cir.setImageResource(Action.resourceByType(cir.action))
    }

    private fun setDragListener(cir: CircularImageView) {
        cir.setOnLongClickListener {
            it.bringToFront()
            val myShadow = View.DragShadowBuilder(it)

            it.startDrag(
                ClipData.newPlainText(cir.action.name, cir.action.name),
                myShadow,
                cir,
                0
            )
        }

        cir.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    when (event.localState) {
                        is CircularImageView -> true
                        is Action.Type -> true
                        else -> false
                    }
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    val item = event.localState
                    when (item) {
                        is CircularImageView -> {
                            cir.swapCir(item)
                            v.invalidate()
                            cir.invalidate()
                        }
                        is Action.Type -> {
                            cir.setImageResource(Action.resourceByType(item))
                        }
                    }
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    val item = event.localState
                    when (item) {
                        is CircularImageView -> {
                            cir.swapCir(item)
                            v.invalidate()
                        }
                        is Action.Type -> {
                            cir.setImageResource(Action.resourceByType(cir.action))
                        }
                    }
                    true
                }
                DragEvent.ACTION_DROP -> {
                    when (val item = event.localState) {
                        is CircularImageView -> {
                            viewModel.swapContactAction(cir.direction, item.direction)
                            setData(viewModel.getContactActions())
                        }
                        is Action.Type -> {
                            val action = viewModel.getContact().actions.find { it.type == item }
                                ?: Action(0, item, "", "")
                            viewModel.setContactAction(cir.direction, action)
                            setData(viewModel.getContactActions())
                        }
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onStop() {
        viewModel.saveContactSettings()
        super.onStop()
    }

    private fun CircularImageView.swapCir(c: CircularImageView) {
        this.image = c.image.also { c.image = this.image }
        this.action = c.action.also { c.action = this.action }
    }

    private fun setData(directActions: DirectActions) {
        v.cirRight.action = directActions.right.type
        v.cirLeft.action = directActions.left.type
        v.cirTop.action = directActions.top.type
        v.cirBottom.action = directActions.down.type
        v.cirRight.let(this::setCirData)
        v.cirLeft.let(this::setCirData)
        v.cirTop.let(this::setCirData)
        v.cirBottom.let(this::setCirData)

        v.textLeft.text = directActions.left.value
        v.textRight.text = directActions.right.value
        v.textTop.text = directActions.top.value
        v.textBottom.text = directActions.down.value
    }

    private fun setAllCirs(settings: Settings) {
        settings.borderWidthCirs.toFloat().let {
            v.cirBottom.borderWidth = it
            v.cirTop.borderWidth = it
            v.cirRight.borderWidth = it
            v.cirLeft.borderWidth = it
        }

        settings.colorBorder.let {
            v.cirBottom.borderColor = it
            v.cirTop.borderColor = it
            v.cirRight.borderColor = it
            v.cirLeft.borderColor = it
        }
    }
}

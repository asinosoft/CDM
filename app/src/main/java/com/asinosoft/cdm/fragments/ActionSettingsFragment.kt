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
import com.asinosoft.cdm.adapters.ActionsAdapter
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.databinding.FragmentActionSettingsBinding
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.viewmodels.SettingsViewModel
import com.asinosoft.cdm.views.CircularImageView

/**
 * Окно настройки действий по-умолчанию
 */
class ActionSettingsFragment : Fragment() {
    private val model: SettingsViewModel by activityViewModels()
    private var v: FragmentActionSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = FragmentActionSettingsBinding.inflate(inflater, container, false)
        return v!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        v = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initActionButton(
            v!!.cirTop,
            Metoths.Companion.Direction.TOP,
            model.settings.topButton
        )

        initActionButton(
            v!!.cirBottom,
            Metoths.Companion.Direction.DOWN,
            model.settings.bottomButton
        )

        initActionButton(
            v!!.cirLeft,
            Metoths.Companion.Direction.LEFT,
            model.settings.leftButton
        )

        initActionButton(
            v!!.cirRight,
            Metoths.Companion.Direction.RIGHT,
            model.settings.rightButton
        )

        initActionList()
    }

    private fun initActionButton(
        button: CircularImageView,
        direction: Metoths.Companion.Direction,
        action: Action.Type
    ) {
        button.direction = direction
        button.action = action
        button.setImageResource(Action.resourceByType(action))
        setDragListener(button)
    }

    private fun initActionList() {
        v!!.rvActions.layoutManager = GridLayoutManager(requireContext(), 5)
        v!!.rvActions.adapter = ActionsAdapter().apply {
            setActions(Action.Type.values().asList())
        }
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
                    when (val item = event.localState) {
                        is CircularImageView -> {
                            item.setImageResource(Action.resourceByType(cir.action))
                            cir.setImageResource(Action.resourceByType(item.action))
                        }
                        is Action.Type -> {
                            cir.setImageResource(Action.resourceByType(item))
                        }
                    }
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    when (val item = event.localState) {
                        is CircularImageView -> {
                            item.setImageResource(Action.resourceByType(item.action))
                        }
                    }
                    cir.setImageResource(Action.resourceByType(cir.action))
                    true
                }
                DragEvent.ACTION_DROP -> {
                    when (val item = event.localState) {
                        is CircularImageView -> {
                            cir.action.let {
                                cir.action = item.action
                                item.action = it
                            }
                            model.setAction(cir.direction, item.action)
                            model.setAction(item.direction, cir.action)
                        }
                        is Action.Type -> {
                            cir.action = item
                            model.setAction(cir.direction, item)
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
}

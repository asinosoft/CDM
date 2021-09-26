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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.R
import com.asinosoft.cdm.adapters.ActionsAdapter
import com.asinosoft.cdm.adapters.SelectorAdapter
import com.asinosoft.cdm.data.Action
import com.asinosoft.cdm.data.DirectActions
import com.asinosoft.cdm.databinding.ContactSettingsBinding
import com.asinosoft.cdm.helpers.AlertDialogUtils
import com.asinosoft.cdm.helpers.Metoths
import com.asinosoft.cdm.viewmodels.DetailHistoryViewModel
import com.asinosoft.cdm.views.CircularImageView
import org.jetbrains.anko.image

class ContactSettingsFragment : Fragment() {
    private val model: DetailHistoryViewModel by activityViewModels()
    private var v: ContactSettingsBinding? = null
    private var actions: Collection<Action> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = ContactSettingsBinding.inflate(inflater, container, false)
        return v!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        v = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        v!!.cirLeft.direction = Metoths.Companion.Direction.LEFT
        v!!.cirRight.direction = Metoths.Companion.Direction.RIGHT
        v!!.cirTop.direction = Metoths.Companion.Direction.TOP
        v!!.cirBottom.direction = Metoths.Companion.Direction.DOWN

        v!!.cirBottom.let(this@ContactSettingsFragment::setDragListener)
        v!!.cirTop.let(this@ContactSettingsFragment::setDragListener)
        v!!.cirLeft.let(this@ContactSettingsFragment::setDragListener)
        v!!.cirRight.let(this@ContactSettingsFragment::setDragListener)

        v!!.rvActions.layoutManager = GridLayoutManager(requireContext(), 4)
        v!!.rvActions.adapter = ActionsAdapter()

        model.availableActions.observe(viewLifecycleOwner) { actions ->
            actions?.let { (v!!.rvActions.adapter as ActionsAdapter).setActions(it) }
        }

        model.directActions.observe(viewLifecycleOwner) { actions ->
            actions?.let { setData(it) }
        }

        model.contact.observe(viewLifecycleOwner) { contact ->
            contact?.let { actions = contact.actions }
        }
    }

    private fun setCirData(cir: CircularImageView) {
        cir.setImageResource(Action.resourceByType(cir.action))
    }

    private fun setDragListener(cir: CircularImageView) {
        cir.setOnLongClickListener {
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
                            v!!.invalidate()
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
                            v!!.invalidate()
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
                            model.swapContactAction(cir.direction, item.direction)
                        }
                        is Action.Type -> {
                            setContactAction(cir.direction, item)
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
        model.saveContactSettings(requireContext())
        super.onStop()
    }

    private fun CircularImageView.swapCir(c: CircularImageView) {
        this.image = c.image.also { c.image = this.image }
        this.action = c.action.also { c.action = this.action }
    }

    private fun setContactAction(direction: Metoths.Companion.Direction, type: Action.Type) {
        val actions = this.actions.filter { it.type == type }
        when (actions.size) {
            0 -> {
                model.setContactAction(direction, Action(0, type, "", ""))
            }
            1 -> {
                model.setContactAction(direction, actions[0])
            }
            else -> {
                val dialog =
                    AlertDialogUtils.dialogListWithoutConfirm(requireContext(), "Выберите номер")
                val adapter = SelectorAdapter(actions.map { it.value }) { selectedNumber ->
                    model.setContactAction(
                        direction,
                        actions.find { it.value == selectedNumber }!!
                    )
                    dialog.dismiss()
                }
                val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_popup)
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter
                dialog.show()
            }
        }
    }

    private fun setData(directActions: DirectActions) {
        v!!.cirRight.action = directActions.right.type
        v!!.cirLeft.action = directActions.left.type
        v!!.cirTop.action = directActions.top.type
        v!!.cirBottom.action = directActions.down.type
        v!!.cirRight.let(this::setCirData)
        v!!.cirLeft.let(this::setCirData)
        v!!.cirTop.let(this::setCirData)
        v!!.cirBottom.let(this::setCirData)

        v!!.textLeft.text = directActions.left.value
        v!!.textRight.text = directActions.right.value
        v!!.textTop.text = directActions.top.value
        v!!.textBottom.text = directActions.down.value
    }
}

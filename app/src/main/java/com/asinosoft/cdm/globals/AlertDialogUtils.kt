package com.asinosoft.cdm.globals

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.R
import com.google.android.material.tabs.TabLayout
import java.util.*


object AlertDialogUtils {


    fun dialogListWithoutConfirm(
        context: Context,
        titleString: String
    ): Dialog {
        val popup = Dialog(context)
        popup.setCanceledOnTouchOutside(true)
        popup.setContentView(R.layout.alert_dialog_product_search_without_confirm)
        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.window?.attributes?.gravity = Gravity.CENTER
        popup.window?.setLayout(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        val closeBtn = popup.findViewById<ImageView>(R.id.close)
        val title = popup.findViewById<TextView>(R.id.title)

        title.text = titleString
        closeBtn.setOnClickListener { popup.dismiss() }

        return popup
    }

//    fun dialogListWithoutConfirmSmall(
//        context: Context,
//        titleString: String
//    ): Dialog {
//        val popup = Dialog(context, R.style.AlertDialogCustom)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.alert_dialog_product_search_without_confirm_small)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.MATCH_PARENT
//        )
//        val closeBtn = popup.findViewById<ImageView>(R.id.close)
//        val title = popup.findViewById<TextView>(R.id.title)
//
//        title.text = titleString
//        closeBtn.setOnClickListener { popup.dismiss() }
//
//        return popup
//    }
//
//    fun dialogListEmpty(
//        context: Context
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.popup_choice)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//        return popup
//    }
//
//    fun dialogListWithConfirm(
//        context: Context,
//        titleString: String,
//        callbackAlert: (() -> Unit)? = null
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.alert_dialog_list_with_confirm)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT, //todo wrap contnt?
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//        val title = popup.findViewById<TextView>(R.id.title)
//        val cancelBtn = popup.findViewById<TextView>(R.id.cancelBtn)
//        val yesBtn = popup.findViewById<TextView>(R.id.yesBtn)
//        val recyclerView = popup.findViewById<RecyclerView>(R.id.recycler_popup)
//        yesBtn.setOnClickListener {
//            recyclerView.clearFocus()
//            popup.dismiss()
//            callbackAlert?.invoke()
//        }
//        title.text = titleString
//
//        cancelBtn.setOnClickListener {
//            popup.dismiss()
//        }
//
//        return popup
//    }
//
//    fun alertDialogWarning(context: Context, titleString: String): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.alert_dialog_warning)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//        val yesBtn = popup.findViewById<TextView>(R.id.yesBtn)
//        val notificationText = popup.findViewById<TextView>(R.id.notificationText)
//
//        notificationText.text = titleString
//        yesBtn.setOnClickListener { popup.dismiss() }
//
//        return popup
//    }
//
//    fun alertDialogConfirm(
//        context: Context,
//        notificationText: String,
//        callbackConfirm: (() -> Unit),
//        callbackCancel: (() -> Unit)? = null
//    ): Dialog {
//
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.alert_dialog_confirm)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//
//        val notification = popup.findViewById<TextView>(R.id.notificationText)
//        val cancelBtn = popup.findViewById<TextView>(R.id.cancelBtn)
//        val yesBtn = popup.findViewById<TextView>(R.id.yesBtn)
//
//        notification.text = notificationText
//
//        yesBtn.setOnClickListener {
//            callbackConfirm.invoke()
//            popup.dismiss()
//        }
//
//        cancelBtn.setOnClickListener {
//            popup.dismiss()
//            callbackCancel?.invoke()
//        }
//
//        return popup
//    }
//
//    fun claimAlertDialog(
//        context: Context,
//        callbackConfirm: ((claimDescription: String, popup: Dialog) -> Unit)
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.alert_dialog_claim)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//
//        val claimDescription = popup.findViewById<TextView>(R.id.claim_description)
//        val close = popup.findViewById<ImageView>(R.id.close)
//        val saveBtn = popup.findViewById<TextView>(R.id.saveBtn)
//
//        close.setOnClickListener {
//            popup.dismiss()
//        }
//
//        saveBtn.setOnClickListener {
//            callbackConfirm.invoke(claimDescription.text.toString(), popup)
//        }
//
//        return popup
//    }
//
//    fun dialogViewPager(
//        context: Context, listImages: List<RateImageModel?>, currentImage: Int
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.popup_view_pager)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//
//        val close = popup.findViewById<ImageView>(R.id.close)
//        val imagePager = popup.findViewById<ViewPager>(R.id.imagePager)
//        val tabDots = popup.findViewById<TabLayout>(R.id.tabDots)
//        val imagePagerAdapter = ImagePagerAdapter(context, listImages)
//
//        imagePager.adapter = imagePagerAdapter
//        imagePager.currentItem = currentImage
//        tabDots.setupWithViewPager(imagePager)
//        tabDots.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        imagePagerAdapter.notifyDataSetChanged()
//        close.setOnClickListener {
//            popup.dismiss()
//        }
//
//        return popup
//    }
//
//    fun dialogProductColors( context: Context, listSizes: MutableList<Pair<String , Boolean>>  ): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.dialog_colors)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//
//        val okButton = popup.findViewById<View>(R.id.btnApply)
//        val colorsRecycler = popup.findViewById<RecyclerView>(R.id.colorsRecycler)
//        val productColorsViewingAdapter = ProductColorsViewingAdapter()
//        colorsRecycler.layoutManager = LinearLayoutManager(context)
//        colorsRecycler.adapter = productColorsViewingAdapter
//
//        productColorsViewingAdapter.setData(listSizes)
//
//        okButton.setOnClickListener {
//            popup.dismiss()
//        }
//
//
//        return popup
//    }
//
//    fun timePicker(context: Context, callbackPositive: ((time: Date) -> Unit)): TimePickerDialog {
//        val timeListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
//            val calendarBeg = GregorianCalendar(0, 0, 0, hourOfDay, minute, 0)
//            callbackPositive.invoke(calendarBeg.time)
//        }
//        val time = TimePickerDialog(context, timeListener, 9, 0, true)
//        time.setCanceledOnTouchOutside(true)
//        time.window?.setLayout(
//            Toolbar.LayoutParams.WRAP_CONTENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//        return time
//    }
//
//    fun userAgreementDialog(
//        context: Context
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.user_agreement_dialog)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.MATCH_PARENT
//        )
//
//        val close = popup.findViewById<ImageView>(R.id.close)
//
//        close.setOnClickListener {
//            popup.dismiss()
//        }
//
//        return popup
//    }
//
//    fun acceptTermsDialog(
//        context: Context, callback: () -> Unit
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.accept_terms_dialog)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.MATCH_PARENT
//        )
//
//        val cancelBtn = popup.findViewById<TextView>(R.id.cancelBtn)
//        val acceptBtn = popup.findViewById<TextView>(R.id.acceptBtn)
//        val checkBoxNotShowMore = popup.findViewById<CheckBox>(R.id.checkBoxNotShowMore)
//
//        cancelBtn.setOnClickListener {
//            popup.dismiss()
//        }
//
//        acceptBtn.setOnClickListener {
//            if (checkBoxNotShowMore.isChecked) {
//                PreferenceHelper.setUserAgreementStatus(true)
//            } else {
//                PreferenceHelper.setUserAgreementStatus(false)
//            }
//            popup.dismiss()
//            callback.invoke()
//        }
//
//        return popup
//    }
//
//
//    fun infoPreOrder(
//        context: Context, callback: (() -> Unit)? = null
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setContentView(R.layout.preorder_dialog)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.MATCH_PARENT
//        )
//
//        val btnNext = popup.findViewById<TextView>(R.id.btnNext)
//        val btnCancel = popup.findViewById<TextView>(R.id.btnCancel)
//        btnCancel.setOnClickListener {
//            alertDialogConfirm(context, "Вы уверены что хотите отменить заказ?", {
//                Toast.makeText(
//                    context,
//                    "Заказ отменен",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                popup.dismiss()
//            }).show()
//        }
//        btnNext.setOnClickListener {
//            popup.dismiss()
//            callback?.invoke()
//        }
//
//        return popup
//    }
//
//    fun infoPreOrderNotFormalized( //todo remove
//        context: Context, callback: (() -> Unit)? = null
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setContentView(R.layout.preorder_not_formalized_dialog) //todo remove
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.MATCH_PARENT
//        )
//
//        val btnOrder = popup.findViewById<TextView>(R.id.btnOrder)
//        val minus: LinearLayout = popup.findViewById(R.id.minus)
//        val plus: ImageView = popup.findViewById(R.id.plus)
//        val price: TextView = popup.findViewById(R.id.price)
//        val productCount: TextView = popup.findViewById(R.id.product_count)
//        val btnClose = popup.findViewById<ImageView>(R.id.close)
//
//        btnOrder.setOnClickListener {
//            popup.dismiss()
//            callback?.invoke()
//        }
//
//        btnClose.setOnClickListener {
//            popup.dismiss()
//        }
//
//
//        var count = productCount.text.toString().toInt()
//        plus.setOnClickListener {
//            count += 1
//            setCountItem(price, productCount, count)
//        }
//
//        minus.setOnClickListener {
//            if (count > 0) {
//                count -= 1
//                setCountItem(price, productCount, count)
//            }
//        }
//
//        return popup
//    }
//
//    private fun setCountItem(price: TextView, productCount: TextView, count: Int) {
//        productCount.text = count.toString()
//        price.text = "${count * 100}р"
//    }
//
//
//    fun popupTypeSelected(
//        context: Context, items: List<String>, position: Int = 0, callback: (ItemChoice) -> Unit
//    ): Dialog {
//        val itemList = mutableListOf<ItemChoice>()
//
//        for (i in items.indices) {
//            itemList.add(ItemChoice(items[i], false, i))
//        }
//
//        val popup = Dialog(context)
//
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.popup_choice)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//
//        val rv = popup.findViewById<RecyclerView>(R.id.recycler_view)
//        val adapter = ChoiceAdapter { item ->
//            popup.dismiss()
//            callback.invoke(item)
//        }
//
//        adapter.setList(itemList, position)
//        rv.layoutManager = LinearLayoutManager(context)
//        rv.adapter = adapter
//        return popup
//    }
//
//    fun popupCheckbox(
//        context: Context,
//        titleText: String,
//        items: MutableList<String>,
//        selected: MutableList<String>,
//        callback: (String) -> Unit
//    ): Dialog {
//
//        val popup = Dialog(context)
//
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.alert_dialog_list_with_confirm)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//
//        val rv = popup.findViewById<RecyclerView>(R.id.recycler_popup)
//        val cancelBtn = popup.findViewById<TextView>(R.id.cancelBtn)
//        val yesBtn = popup.findViewById<TextView>(R.id.yesBtn)
//        val title = popup.findViewById<TextView>(R.id.title)
//
//        val adapter = CheckboxSelectingAdapter()
//
//        adapter.setData(items)
//        adapter.setSelectedVariants(selected)
//
//        title.text = titleText
//
//        cancelBtn.setOnClickListener {
//            popup.dismiss()
//        }
//
//        yesBtn.setOnClickListener {
//            callback.invoke(adapter.getSelectedVariants().parseToString())
//            popup.dismiss()
//        }
//
//        rv.layoutManager = LinearLayoutManager(context)
//        rv.adapter = adapter
//
//        return popup
//    }
//
//    fun popupSetRole(
//        context: Context,
//        list: List<String>,
//        callback: ((String, String) -> Unit)
//    ): Dialog {
//        val popup = Dialog(context)
//        popup.setCanceledOnTouchOutside(true)
//        popup.setContentView(R.layout.popup_set_role)
//        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popup.window?.attributes?.gravity = Gravity.CENTER
//        popup.window?.setLayout(
//            Toolbar.LayoutParams.MATCH_PARENT,
//            Toolbar.LayoutParams.WRAP_CONTENT
//        )
//
//        val cancelBtn = popup.findViewById<TextView>(R.id.btnCancel)
//        val setRoleBtn = popup.findViewById<TextView>(R.id.setRoleBtn)
//        val phone = popup.findViewById<EditText>(R.id.phone)
//        val role = popup.findViewById<TextView>(R.id.role)
//
//        phone.setPhoneFormatter()
//        role.setOnClickListener {
//            popupTypeSelected(
//                context,
//                list,
//                0
//            ) { item ->
//                role.text = item.text
//
//            }.show()
//        }
//
//
//
//
//
////        setRoleBtn.setOnClickListener {
//////            if (isValidData(phone, role, context)) {
////                popup.dismiss()
////                callback.invoke(
////
////                    phone.splitPhoneForServer(),
////                    role.text.toString()
////                )
//////            }
////        }
//
//        cancelBtn.setOnClickListener {
//            popup.dismiss()
//        }
//
//
//        return popup
//    }



}
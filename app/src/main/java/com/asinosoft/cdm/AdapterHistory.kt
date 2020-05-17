package com.asinosoft.cdm

import android.content.Context
import android.content.Intent
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import javax.security.auth.callback.Callback
import android.widget.TextView
import android.widget.ImageView
import android.widget.RelativeLayout
import com.zerobranch.layout.SwipeLayout
import org.jetbrains.anko.find
import java.util.*


class AdapterHistory(var items: ArrayList<HistoryItem>, val callback: Callback?, val onClick: Boolean = true): RecyclerView.Adapter<AdapterHistory.HolderHistory>(), ItemTouchHelperAdapter {

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
    }

    override fun onItemDismiss(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistory{
        context = parent.context
        return HolderHistory( LayoutInflater.from(parent.context).inflate(R.layout.history_swiping_item,parent, false)
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HolderHistory, position: Int) {
        holder.bind(items[position])
    }

    private fun openDetail(item: HistoryItem) {
        val intent = Intent(this.context, DetailHistoryActivity::class.java)
        intent.putExtra(Keys.number, item.numberContact)
        intent.putExtra(Keys.id, item.contactID)
        context.startActivity(intent)
    }


    inner class HolderHistory(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageCon = itemView.findViewById<CircleImageView>(R.id.imageContact)
        private val imageLeft = itemView.findViewById<CircleImageView>(R.id.imageLeftAction)
        private val imageRight = itemView.findViewById<CircleImageView>(R.id.imageRightAction)
        private val nameCon = itemView.findViewById<TextView>(R.id.name)
        private val numberCon = itemView.findViewById<TextView>(R.id.number)
        private val timeCon = itemView.findViewById<TextView>(R.id.timeContact)
        private val dateCon = itemView.findViewById<TextView>(R.id.dateContact)
        private val typeCall = itemView.findViewById<ImageView>(R.id.typeCall)
        private val swipeL = itemView.find<SwipeLayout>(R.id.swipe_layout)
        private val relaytL = itemView.find<RelativeLayout>(R.id.drag_layout)

        fun bind(item: HistoryItem) {

            imageCon.setImageDrawable(item.image)
            nameCon.text = item.nameContact
            numberCon.text = "${item.numberContact}, ${Metoths.getFormatedTime(item.duration)}"
            timeCon.text = item.time
            dateCon.text = item.date

            when(item.typeCall){
                CallLog.Calls.OUTGOING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_made_24)
                CallLog.Calls.INCOMING_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_received_24)
                CallLog.Calls.MISSED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_missed_24)
                CallLog.Calls.BLOCKED_TYPE -> typeCall.setImageResource(R.drawable.baseline_call_canceled_24)
            }

            swipeL.setOnActionsListener(object : SwipeLayout.SwipeActionsListener{
                override fun onOpen(direction: Int, isContinuous: Boolean) {
                    when (direction){
                        SwipeLayout.RIGHT -> {
//                            imageLeft.visibility = View.VISIBLE
                            Metoths.callPhone(item.numberContact, context)
                        }
                        SwipeLayout.LEFT -> {
//                            imageRight.visibility = View.VISIBLE
                            Metoths.openWhatsApp(item.numberContact, context)
                        }
                        else -> Log.e("AdapterHistory.kt: ", "SwipeLayout direction UNKNOWN = $direction")
                    }
                    swipeL.close()
                }

                override fun onClose() {
                }

            })
            if (onClick)
            relaytL.setOnClickListener {
                openDetail(item)
            }



            /*itemView.onTouch { v, event ->
                Log.d("OnTouchHistory: ", "Event = ${event.actionMasked}")

                //scrollView.setScrollingEnabled(false)

                when(event.actionMasked){
                    MotionEvent.ACTION_DOWN -> {
                        try {
                            backupHistoryItem.reset()
                        }catch (e: Exception){Log.e("AdapterHistory: ", e.message!!)}
                        xPos = event.rawX
                        yPos = event.rawY
                        marStart = imageCon.marginStart
                        marEndTime = timeCon.marginEnd
                        openDetail = true
                        isdown = itemView
                        backupHistoryItem = BackupHistoryItem(imageCon, imageLeft, imageRight, timeCon, dateCon, marStart, marEndTime)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        //scrollView.setScrollingEnabled(false)
                        //if (isdown != itemView) return@onTouch
                        var difX = xPos - event.rawX
                        var difY = yPos - event.rawY
                        openDetail = Math.abs(difX) <= 10
                        if (abs(difY) + (if(priorityY) priority else -priority) >= abs(difX)) {
                            imageCon.setMargins(start = marStart)
                            timeCon.setMargins(end = marEndTime)
                            dateCon.setMargins(end = marEndTime)
                            imageLeft.visibility = View.INVISIBLE
                            imageRight.visibility = View.INVISIBLE
                            scrollView.setScrollingEnabled(true)
                            return@onTouch
                        }

                        //scrollView.setScrollingEnabled(abs(difY) > abs(difX))

                        priorityY = abs(difY) + (if(priorityY) priority else -priority) >= abs(difX)

                        difX = if (Math.abs(difX) > MAXT) MAXT * (difX / Math.abs(difX)) else difX

                        imageCon.setMargins(start = marStart - difX.toInt())
                        timeCon.setMargins(end = marEndTime + difX.toInt())
                        dateCon.setMargins(end = marEndTime + difX.toInt())

                        imageLeft.visibility = if(Math.abs(difX) >= DIF) if(difX / Math.abs(difX) == -1f) View.VISIBLE else View.INVISIBLE else View.INVISIBLE
                        imageRight.visibility = if(Math.abs(difX) >= DIF) if(difX / Math.abs(difX) == 1f) View.VISIBLE else View.INVISIBLE else View.INVISIBLE
                        *//*if (scrollView.context != null) {
                            var difY = (yPos - event.rawX).toInt()
                            scrollView.scrollTo(0, 100)
                        }*//*
                    }

                    MotionEvent.ACTION_UP -> {
                        imageCon.setMargins(start = marStart)
                        timeCon.setMargins(end = marEndTime)
                        dateCon.setMargins(end = marEndTime)
                        if(imageLeft.visibility == View.VISIBLE) Metoths.callPhone(item.numberContact, context)
                        if(imageRight.visibility == View.VISIBLE) Metoths.openWhatsApp(item.numberContact, context)
                        imageLeft.visibility = View.INVISIBLE
                        imageRight.visibility = View.INVISIBLE
                        *//*var difX = xPos - event.rawX
                        openDetail = Math.abs(difX) <= 10*//*
                        if(onClick && openDetail){
                            openDialog(items)
                            openDetail = false
                        }
                        scrollView.setScrollingEnabled(true)
                    }
                }
            }*/
        }
    }

}


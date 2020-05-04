package com.asinosoft.cdm

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.fragment_history.*
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.support.v4.toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HistoryFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var xPos = 0f
    private var yPos = 0f
    private var marStart = 0
    private var marEndTime = 0
    private val DIF = 100
    private val MAXT = 200
    lateinit var dialFrag: CellFragment
    lateinit var listPar: ArrayList<HistoryCell>
    private var openDetail = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dialFrag = CellFragment()
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    fun changeParm(photo: Drawable = imageFrame.drawable, name: String = nameFrame.text.toString()){
        imageFrame.setImageDrawable(photo)
        nameFrame.text = name
    }

    fun setParams(cell: HistoryCell, listCells: ArrayList<HistoryCell> = ArrayList(), onClick: Boolean = true){
        //if(historyFragment_relativeLayout == null) return
        if (listCells.isNotEmpty()) {
            listPar = listCells
        }
        /*historyFragment_relativeLayout.onClick {
            if (onClick && openDetail) openDialog(listCells)
        }*/
            historyFragment_relativeLayout.onTouch { v, event ->
                Log.d("OnTouchHistory: ", "Event = ${event.actionMasked}")
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN -> {
                    xPos = event.rawX
                    yPos = event.rawY
//                    marStart = imageFrame.marginStart
//                    marEndTime = timeFrame.marginEnd
//                    openDetail = true
                }

                MotionEvent.ACTION_MOVE -> {
                    var difX = xPos - event.rawX
                    openDetail = Math.abs(difX) <= 10
                    difX = if (Math.abs(difX) > MAXT) MAXT * (difX / Math.abs(difX)) else difX

                    imageFrame.setMargins(start = marStart - difX.toInt())
                    timeFrame.setMargins(end = marEndTime + difX.toInt())
                    dateFrame.setMargins(end = marEndTime + difX.toInt())

                    imageCall.visibility = if(Math.abs(difX) >= DIF) if(difX / Math.abs(difX) == -1f) View.VISIBLE else View.INVISIBLE else View.INVISIBLE
                    imageWhatsApp.visibility = if(Math.abs(difX) >= DIF) if(difX / Math.abs(difX) == 1f) View.VISIBLE else View.INVISIBLE else View.INVISIBLE
                    /*if (scrollView.context != null) {
                        var difY = (yPos - event.rawX).toInt()
                        scrollView.scrollTo(0, 100)
                    }*/
                }

                MotionEvent.ACTION_UP -> {
                    imageFrame.setMargins(start = marStart)
                    timeFrame.setMargins(end = marEndTime)
                    dateFrame.setMargins(end = marEndTime)
                    if(imageCall.visibility == View.VISIBLE) callPhone(cell.numberContact)
                    if(imageWhatsApp.visibility == View.VISIBLE) openWhatsApp(cell.numberContact)
                    imageWhatsApp.visibility = View.INVISIBLE
                    imageCall.visibility = View.INVISIBLE
                    /*var difX = xPos - event.rawX
                    openDetail = Math.abs(difX) <= 10*/
                    if(onClick && openDetail){
                        openDialog(listCells)
                        openDetail = false
                    }
                }
                 }
            }
        imageFrame.setImageDrawable(cell.image)
        nameFrame.text = cell.nameContact
        numberFrame.text = "${cell.numberContact}, ${getFormatedTime(cell.duration)}"
        timeFrame.text = cell.time
        dateFrame.text = cell.date
        when(cell.typeCall){
            CallLog.Calls.OUTGOING_TYPE -> typeCallFrame.setImageResource(R.drawable.baseline_call_made_24)
            CallLog.Calls.INCOMING_TYPE -> typeCallFrame.setImageResource(R.drawable.baseline_call_received_24)
            CallLog.Calls.MISSED_TYPE -> typeCallFrame.setImageResource(R.drawable.baseline_call_missed_24)
            CallLog.Calls.BLOCKED_TYPE -> typeCallFrame.setImageResource(R.drawable.baseline_call_canceled_24)
        }
    }

    private fun getFormatedTime(duration: String): String {
        var str = "0:00"
        try {
            str = "${duration.toInt() / 60}:${duration.toInt() % 60}"
        }catch (e: Exception){ Log.e("Exception ", e.message ?: "")}
        return str
    }

    fun View.setMargins(start:Int = MainActivity.nullNum, top:Int = MainActivity.nullNum, end:Int = MainActivity.nullNum, bottom:Int = MainActivity.nullNum){
        var layoutParams = this.layoutParams as RelativeLayout.LayoutParams
        layoutParams.marginStart = if(start != MainActivity.nullNum) start else layoutParams.marginStart
        layoutParams.topMargin = if(top != MainActivity.nullNum) top else layoutParams.topMargin
        layoutParams.marginEnd = if(end != MainActivity.nullNum) end else layoutParams.marginEnd
        layoutParams.bottomMargin = if(bottom != MainActivity.nullNum) bottom else layoutParams.bottomMargin
        this.layoutParams = layoutParams
    }

    private fun openDialog(listCells: ArrayList<HistoryCell>) {
       /* val ft = fragmentManager!!.beginTransaction()
        val prev = fragmentManager!!.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
//        ft.commitNow()
        HistoryDialogFragment().show(fragmentManager, "dialog")*/

        val intent = Intent(this.context, DetailHistory::class.java)
        //intent.putExtra(Keys.ListCells, Gson().toJson(listCells))
        intent.putExtra(Keys.ListCells, listCells[0].numberContact)
        startActivity(intent)
    }

    fun callPhone(telNum: String){
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$telNum"))
        startActivity(intent)
    }

    private fun openWhatsApp(num: String) {
        val isAppInstalled = appInstalledOrNot("com.whatsapp")
        if (isAppInstalled) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$num"))
            startActivity(intent)
        } else {
            toast("Ошибка! WhatsApp не установлен!")
        }
        Log.e("Action: ", "WhatsApp open!")
    }


    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = activity!!.packageManager
        return try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun setFontWithFone(color: Int){
        when(color){
            Color.WHITE ->{
                setFont("#1F1F1F", "#6A6A6A")
            }
            resources.getColor(R.color.costomGray) ->{
                setFont("#FAFAFA", "#9B9B9B")
            }
            Color.BLACK ->{
                setFont("#FAFAFA", "#979797")
            }
            else ->{
                setFont("#FAFAFA", "#FAFAFA")
            }
        }
    }

    private fun setFont(nameColor: String, numColor: String) {
        nameFrame.setTextColor(Color.parseColor(nameColor))
        numberFrame.setTextColor(Color.parseColor(numColor))
        timeFrame.setTextColor(Color.parseColor(numColor))
        dateFrame.setTextColor(Color.parseColor(numColor))
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

package com.asinosoft.cdm

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.getPattern
import com.asinosoft.cdm.Metoths.Companion.setColoredText
import com.asinosoft.cdm.databinding.HistorySwipingItemBinding
import com.github.tamir7.contacts.Contact
import com.zerobranch.layout.SwipeLayout
import kotlinx.coroutines.*
import org.jetbrains.anko.runOnUiThread
import kotlin.coroutines.CoroutineContext

class AdapterContacts(var contacts: List<Contact>, val itemClickListerner: View.OnClickListener, val onClick: Boolean = true): RecyclerView.Adapter<AdapterContacts.Holder>(), CoroutineScope {

    private lateinit var context: Context
    private var nums = ""
    private var job: Job = Job()
    private var jobFilter: Job = Job()
    private var regex: Regex? = null
    private var listBackup = contacts

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        context = parent.context
        return Holder(
            HistorySwipingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    fun getItems() = contacts

    fun setItems(list: List<Contact>, nums: String = ""){
        contacts = list
        this.nums = nums
        notifyDataSetChanged()
    }

/*
    private suspend fun List<Contact>.filtered(nums: String) = async {
        var name: String? = null
        var phone: String? = null
        val r: ArrayList<Pair<Contact, Pair<String?, String?>>> = ArrayList()
        this@filtered.forEach { contact ->
            regex?.find( contact.displayName)?.let {
                contact.displayName
            }
            contact.phoneNumbers.forEach{
                if (it.normalizedNumber.contains(nums, true)){
                    phone = nums
                    return@forEach
                }
            }
            if (name != null && phone != null) r.add(Pair(contact, Pair(name, phone)))
        }
        return@async r
    }.await()
*/

    private suspend fun List<Contact>.filtered(nums: String) = async {
        val r = ArrayList<Contact>()
        this@filtered.forEach { contact ->
            regex?.find( contact.displayName)?.let {
                r.add(contact)
                return@forEach
            }
            contact.phoneNumbers.forEach{
                if (it.normalizedNumber.isNullOrEmpty()) return@forEach
                if (it.normalizedNumber.contains(nums, true)) {
                    r.add(contact)
                    return@forEach
                }
            }
        }
        return@async r
    }.await()

    fun setFilter(nums: String = "", context: Context) {
        this.context = context
        this.nums = nums
        regex = Regex(getPattern(nums.replace("1", ""), context), RegexOption.IGNORE_CASE)
        jobFilter = launch {
            runBlocking {
                contacts = listBackup.filtered(nums)
                Log.d(
                    "AdapterContact",
                    "Contacts filtered! -> Contacts = ${contacts.size}; Nums = $nums"
                )

                context.runOnUiThread {
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(contacts[position], itemClickListerner)
    }

    inner class Holder(private val v: HistorySwipingItemBinding) : RecyclerView.ViewHolder(v.root) {

        fun bind(item: Contact, itemClickListerner: View.OnClickListener) {
            item.photoUri?.let { v.imageContact.setImageURI(it.toUri()) }
            v.name.text = item.displayName
            val t = item.phoneNumbers
            var tNum = ""
            if (t.isNotEmpty()) {
                t.filter { !it.normalizedNumber.isNullOrEmpty() }.forEach {
                    if (!tNum.contains(it.normalizedNumber)) tNum =
                        tNum.plus("${it.normalizedNumber}, ")
                }
            }
            v.number.text = tNum.dropLast(2)
            v.timeContact.text = ""
            v.dateContact.text = ""
            v.typeCall.visibility = View.GONE

            if (onClick) v.dragLayout.setOnClickListener {
                itemClickListerner.onClick(it)
            }

//            v.root.visibility = (v.number.text.contains(nums) || getNameContains()).toVisibility(true)
//            if (v.root.visibility == View.GONE) v.root.setSize(0, 0)

            setColors(true)

            v.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                override fun onOpen(direction: Int, isContinuous: Boolean) {
                    when (direction) {
                        SwipeLayout.RIGHT -> {
                            Metoths.callPhone(item.phoneNumbers[0].normalizedNumber, context)
                        }
                        SwipeLayout.LEFT -> {
//                            imageRight.visibility = View.VISIBLE
                            Metoths.openWhatsApp(item.phoneNumbers[0].normalizedNumber, context)
                        }
                        else -> Log.e(
                            "AdapterHistory.kt: ",
                            "SwipeLayout direction UNKNOWN = $direction"
                        )
                    }
                    v.swipeLayout.close()
                }

                override fun onClose() {
                }

            })
        }

        /**
         * Установка выделения текста по номеру телефона.
         * @param name Установка выделения текста также по имени
         */
        private fun setColors(name: Boolean = false) {
            if (nums.isNotEmpty() && v.number.text.contains(nums)) v.number.setColoredText(nums)
            if (name) regex?.find(v.name.text.toString())?.let {
                v.name.setColoredText(it.value)
            }
        }
    }

}


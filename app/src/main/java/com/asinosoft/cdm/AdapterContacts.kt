package com.asinosoft.cdm

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.asinosoft.cdm.Metoths.Companion.getPattern
import com.asinosoft.cdm.Metoths.Companion.setColoredText
import com.asinosoft.cdm.api.Contact
import com.asinosoft.cdm.databinding.HistorySwipingItemBinding
import com.zerobranch.layout.SwipeLayout
import kotlinx.coroutines.*
import org.jetbrains.anko.runOnUiThread
import kotlin.coroutines.CoroutineContext

class AdapterContacts(
    var contacts: List<Contact>,
    val itemClickListerner: View.OnClickListener,
    val onClick: Boolean = true
) : RecyclerView.Adapter<AdapterContacts.Holder>(), CoroutineScope {

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
        val v =
            HistorySwipingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(
            v
        ) {
            val pos = (parent as RecyclerView).getChildAdapterPosition(v.relativeL)
            if (pos < 0) return@Holder
            openDetail(contacts[pos])
        }
    }

    private suspend fun List<Contact>.filtered(nums: String) = async {
        val r = ArrayList<Contact>()
        this@filtered.forEach { contact ->
            regex?.find(contact.name)?.let {
                r.add(contact)
                return@forEach
            }
            contact.phones.forEach {
                if (it.value.contains(nums, true)) {
                    r.add(contact)
                    return@forEach
                }
            }
        }
        r.sortWith(
            compareBy {
                val res = regex?.find(it.name)
                if (res != null) {
                    it.name.indexOf(res.value)
                } else {
                    var index = 0
                    it.phones.forEach {
                        if (it.value.contains(nums, true)) {
                            index = it.value.indexOf(nums)
                            return@forEach
                        }
                    }
                    index
                }
            }
        )
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

    private fun openDetail(item: Contact) {
        val intent = Intent(this.context, DetailHistoryActivity::class.java)
        intent.putExtra(Keys.number, item.phones.first().value)
        intent.putExtra(Keys.id, item.id)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    inner class Holder(private val v: HistorySwipingItemBinding, val itemCallBack: () -> Unit) :
        RecyclerView.ViewHolder(v.root) {

        fun bind(contact: Contact, itemClickListerner: View.OnClickListener) {
            contact.photoUri?.let { v.imageContact.setImageURI(it.toUri()) }
                ?: v.imageContact.setImageResource(R.drawable.contact_unfoto)
            v.name.text = contact.name
            var tNum = ""
            contact.phones.forEach {
                if (!tNum.contains(it.value)) tNum =
                    tNum.plus("${it.value}, ")
            }
            v.number.text = tNum.dropLast(2)
            v.timeContact.text = ""
            v.dateContact.text = ""
            v.typeCall.visibility = View.GONE
            v.relativeL.setOnClickListener {
                itemCallBack()
            }

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
                            contact.phones.firstOrNull()?.let { Metoths.callPhone(it.value, context) }
                        }
                        SwipeLayout.LEFT -> {
                            contact.whatsapps.firstOrNull()?.chatId?.let { Metoths.openWhatsAppChat(it, context) }
                        }
                    }
                    v.swipeLayout.close()
                }

                override fun onClose() {
                }
            })

            v.dragLayout.setOnClickListener {
                openDetail(contact)
            }
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

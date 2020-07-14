package com.asinosoft.cdm.adapters

import androidx.recyclerview.widget.DiffUtil
import com.asinosoft.cdm.HistoryItem
import java.util.ArrayList

class CallLogDiffUtils(val oldList: ArrayList<HistoryItem>, val newList: ArrayList<HistoryItem>): DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].contactID == newList[newItemPosition].contactID

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean{
        oldList[oldItemPosition].let { old ->
            newList[newItemPosition].let { new ->
                return old.nameContact == new.nameContact && old.date == new.date && old.duration == new.duration && old.nameContact == new.nameContact
            }
        }
    }
}
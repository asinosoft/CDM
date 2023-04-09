package com.asinosoft.cdm.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.asinosoft.cdm.data.Action

@Entity(tableName = "actions")
data class Action(
    val contactId: Long,
    @PrimaryKey val id: Long,
    val type: Action.Type,
    val value: String,
    val description: String
)

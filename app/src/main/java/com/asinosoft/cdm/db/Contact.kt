package com.asinosoft.cdm.db

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey
    val id: Long,

    val name: String,

    val photo: Uri?,

    val birthday: Date?,

    val starred: Boolean,
)

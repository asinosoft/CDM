package com.asinosoft.cdm.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface Actions {
    @Upsert
    fun upsert(action: Action)
}

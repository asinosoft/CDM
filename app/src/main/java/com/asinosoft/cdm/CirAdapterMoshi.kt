package com.asinosoft.cdm

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class CirAdapterMoshi {

    @FromJson
    fun fromJson(str: String){

    }

    @ToJson
    fun toJson(list: ArrayList<CircleImage>): String{
        return ""
    }
}
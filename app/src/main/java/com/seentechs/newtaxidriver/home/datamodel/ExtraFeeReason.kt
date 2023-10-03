package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

class ExtraFeeReason : Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("reason")
    @Expose
    lateinit var name: String

    @SerializedName("status")
    @Expose
    lateinit var status: String
}
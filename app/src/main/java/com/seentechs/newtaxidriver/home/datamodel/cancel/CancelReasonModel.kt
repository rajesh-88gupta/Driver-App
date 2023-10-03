package com.seentechs.newtaxidriver.home.datamodel.cancel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CancelReasonModel {

    @SerializedName("id")
    @Expose
    var id: Int? =null
    @SerializedName("reason")
    @Expose
    lateinit var reason: String
    @SerializedName("status")
    @Expose
    lateinit var status: String
}

package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceContent {
    @Expose
    @SerializedName("tooltip")
    lateinit var tooltip: String
    @Expose
    @SerializedName("bar")
    var bar: Boolean = false
    @Expose
    @SerializedName("value")
    var value: String? = null
    @Expose
    @SerializedName("key")
    var key: String? = null
    @Expose
    @SerializedName("colour")
    var colour: String? = null

}

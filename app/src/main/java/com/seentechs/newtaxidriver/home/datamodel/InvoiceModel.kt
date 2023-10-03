package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

class InvoiceModel : Serializable {

    @SerializedName("key")
    @Expose
    var key: String? = null
    @SerializedName("value")
    @Expose
    var value: String? = null

    @SerializedName("bar")
    @Expose
    var bar: String? = null


    @SerializedName("colour")
    @Expose
    var colour: String? = null

    @SerializedName("comment")
    @Expose
    var fareComments: String=""
}

package com.seentechs.newtaxidriver.home.datamodel;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable

class CommonData : Serializable {

    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null

    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null

    @SerializedName("heat_map")
    @Expose
    var heatMap: String? = null

    @SerializedName("status")
    @Expose
    var status: String = ""

    @SerializedName("is_web_payment")
    @Expose
    var isWebPaymentEnable = false
}




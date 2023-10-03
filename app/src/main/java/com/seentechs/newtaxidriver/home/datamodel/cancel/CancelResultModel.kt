package com.seentechs.newtaxidriver.home.datamodel.cancel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CancelResultModel {

    @SerializedName("status_message")
    @Expose
    lateinit var statusMessage: String
    @SerializedName("status_code")
    @Expose
    lateinit var statusCode: String
    @SerializedName("cancel_reasons")
    @Expose
    lateinit var cancelReasons: List<CancelReasonModel>


}

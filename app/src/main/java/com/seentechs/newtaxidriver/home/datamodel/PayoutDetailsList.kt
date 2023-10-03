package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PayoutDetailsList: Serializable {
    @SerializedName("status_code")
    @Expose
    var statusCode: String=""
    @SerializedName("status_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("payout_methods")
    @Expose
    var paymentlist=ArrayList<PayoutDetailsListModel>()


}

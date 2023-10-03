package com.seentechs.newtaxidriver.home.datamodel


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PaymentMethodsModel:Serializable {
    @SerializedName("status_code")
    @Expose
    var statusCode: String=""
    @SerializedName("status_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("payment_list")
    @Expose
    var paymentlist=ArrayList<PaymentMethods>()


    inner class PaymentMethods {
        @SerializedName("key")
        @Expose
        var paymenMethodKey:String=""

        @SerializedName("value")
        @Expose
        var paymenMethodvalue:String=""


        @SerializedName("icon")
        @Expose
        var paymenMethodIcon:String=""


        @SerializedName("is_default")
        @Expose
        var isDefaultPaymentMethod:Boolean=false


    }
}



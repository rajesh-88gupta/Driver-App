package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PayoutData:Serializable {


    @SerializedName("address1")
    @Expose
    var address1:String=""
    @SerializedName("address2")
    @Expose
    var address2:String=""
    @SerializedName("city")
    @Expose
    var city:String=""
    @SerializedName("state")
    @Expose
    var state:String=""
    @SerializedName("country")
    @Expose
    var country:String=""
    @SerializedName("postal_code")
    @Expose
    var postal_code:String=""
    @SerializedName("paypal_email")
    @Expose
    var paypal_email:String=""
    @SerializedName("currency_code")
    @Expose
    var currency_code:String=""
    @SerializedName("routing_number")
    @Expose
    var routing_number:String=""
    @SerializedName("account_number")
    @Expose
    var account_number:String=""
    @SerializedName("holder_name")
    @Expose
    var holder_name:String=""
    @SerializedName("bank_name")
    @Expose
    var bank_name:String=""
    @SerializedName("branch_name")
    @Expose
    var branch_name:String=""
    @SerializedName("branch_code")
    @Expose
    var branch_code:String=""
    @SerializedName("bank_location")
    @Expose
    var bank_location:String=""
}

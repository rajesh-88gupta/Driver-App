package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PayoutDetailsListModel {

    @SerializedName("key")
    @Expose
    var key:String=""

    @SerializedName("value")
    @Expose
    var value:String=""


    @SerializedName("id")
    @Expose
    var id:String=""


    @SerializedName("is_default")
    @Expose
    var isDefault:Boolean=false


    @SerializedName("icon")
    @Expose
    var icon:String=""

    @SerializedName("payout_data")
    @Expose
    lateinit var payoutData:PayoutData


}

package com.seentechs.newtaxidriver.common.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CheckVersionModel {
    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null

    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null

    @SerializedName("force_update")
    @Expose
    var forceUpdate: String? = null

    @SerializedName("enable_referral")
    @Expose
    var enableReferral: Boolean = false

    @SerializedName("otp_enabled")
    @Expose
    var otpEnabled = false

    @SerializedName("support")
    @Expose
    var support = ArrayList<Support>()

}

class Support {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("image")
    @Expose
    var image: String? = null

    @SerializedName("link")
    @Expose
    var link: String =""

}
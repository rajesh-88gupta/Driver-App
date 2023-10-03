package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Seen Technologies on 9/7/18.
 */

class ForgetPwdModel {


    @SerializedName("status_code")
    @Expose
/*<<<<<<< HEAD
    var statusCode: String? = null
    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null

    @SerializedName("otp")
    @Expose
    var otp: String? = null
=======*/
    lateinit var statusCode: String
    @SerializedName("status_message")
    @Expose
    lateinit var statusMessage: String

    @SerializedName("otp")
    @Expose
    lateinit var otp: String

}

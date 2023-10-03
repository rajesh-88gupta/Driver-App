package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * Created by Seen Technologies on 9/7/18.
 */


class LoginDetails : Serializable {

    @SerializedName("status_message")
    @Expose
    lateinit var statusMessage: String
    @SerializedName("status_code")
    @Expose
    lateinit var statusCode: String
    @SerializedName("access_token")
    @Expose
    lateinit var token: String
    @SerializedName("car_details")
    @Expose
    var carDetailModel: ArrayList<CarDetails>? = null
    @SerializedName("user_status")
    @Expose
    lateinit var userStatus: String
    @SerializedName("user_id")
    @Expose
    lateinit var userID: String

    @SerializedName("country_code")
    @Expose
    var country_code: String = ""
    @SerializedName("currency_symbol")
    @Expose
    var currencySymbol: String? = null
    @SerializedName("currency_code")
    @Expose
    lateinit var currencyCode: String
    @SerializedName("vehicle_id")
    @Expose
    lateinit var vehicleId: String
    @SerializedName("payout_id")
    @Expose
    lateinit var payoutId: String
    @SerializedName("mobile_number")
    @Expose
    var mobileNumber: String= ""
    @SerializedName("company_id")
    @Expose
    lateinit var companyId: String
}
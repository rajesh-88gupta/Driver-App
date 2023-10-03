package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by Seen Technologies on 9/11/18.
 */

class EarningModel : Serializable {

    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null
    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null
    @SerializedName("last_trip")
    @Expose
    var lastTrip: String? = null
    @SerializedName("recent_payout")
    @Expose
    var recentPayout: String? = null
    @SerializedName("total_week_amount")
    @Expose
    var totalWeekAmount: String? = null
    @SerializedName("currency_code")
    @Expose
    lateinit var currencyCode: String
    @SerializedName("currency_symbol")
    @Expose
    lateinit var currencySymbol: String
    @SerializedName("trip_details")
    @Expose
    var tripDetails: ArrayList<EarningTripDetailsModel>? = null

/*<<<<<<< HEAD
    var earningList: EarningModel
=======
>>>>>>> origin/master*/
}


package com.seentechs.newtaxidriver.home.payouts.payout_model_classed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.ArrayList

/**
 * Created by Seen Technologies on 3/8/18.
 */

class CountryResult {

    @SerializedName("success_message")
    @Expose
    var statusMessage: String=""
    @SerializedName("status_code")
    @Expose
    var statusCode: String=""

    @SerializedName("country_list")
    @Expose
    var countryList: ArrayList<CountryModel> = ArrayList()
}

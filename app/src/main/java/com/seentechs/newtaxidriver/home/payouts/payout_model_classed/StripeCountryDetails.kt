package com.seentechs.newtaxidriver.home.payouts.payout_model_classed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by Seen Technologies on 9/18/18.
 */

class StripeCountryDetails : Serializable {
    @SerializedName("country_id")
    @Expose
    var countryId: Int = 0

    @SerializedName("country_name")
    @Expose
    var countryName: String = ""

    @SerializedName("country_code")
    @Expose
    var countryCode: String= ""

    @SerializedName("currency_code")
    @Expose
    lateinit var currencyCode :Array<String?>

}

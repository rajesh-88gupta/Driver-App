package com.seentechs.newtaxidriver.home.payouts.payout_model_classed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable
import java.util.ArrayList

/**
 * Stripe Countries Model
 */
class StripeCountriesModel : Serializable {

    @SerializedName("status_code")
    @Expose
    var statusCode: String=""

    @SerializedName("country_list")
    @Expose
    var countryList = ArrayList<StripeCountryDetails>()
}

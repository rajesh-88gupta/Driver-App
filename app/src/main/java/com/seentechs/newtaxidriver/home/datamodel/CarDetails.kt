package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by Seen Technologies on 9/7/18.
 */

class CarDetails : Serializable {

    @SerializedName("id")
    @Expose
    var id: Int? = null

    //Car name should not delete
    @SerializedName("car_name")
    @Expose
    var carName: String? = null

    @SerializedName("currency_code")
    @Expose
    lateinit var currencyCode: String

    @SerializedName("status")
    @Expose
    lateinit var status: String

    @SerializedName("currency_symbol")
    @Expose
    lateinit var currencySymbol: String

}
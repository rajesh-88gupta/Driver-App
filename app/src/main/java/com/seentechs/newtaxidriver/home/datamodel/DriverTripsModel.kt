package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class DriverTripsModel  {

    @SerializedName("status_code")
    @Expose
    var statusCode: String? = null
    @SerializedName("status_message")
    @Expose
    var statusMessage: String? = null
    @SerializedName("total_pages")
    @Expose
    var totalPages: Int? = null
    @SerializedName("data")
    @Expose
    var tripStatusModels: ArrayList<TripDetailsModel>? = null
}
package com.seentechs.newtaxidriver.trips.tripsdetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TripListModel {
    @SerializedName("status_code")
    @Expose
    var statusCode: String = ""

    @SerializedName("status_message")
    @Expose
    var statusMessage: String = ""

    @SerializedName("current_page")
    @Expose
    var currentPage: Int = 0

    @SerializedName("total_pages")
    @Expose
    var totalPages: Int = 0

    @SerializedName("data")
    @Expose
    var data: ArrayList<TripListModelArrayList> = ArrayList()


}
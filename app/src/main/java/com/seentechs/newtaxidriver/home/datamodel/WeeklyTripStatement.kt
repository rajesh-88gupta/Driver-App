package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.SerializedName

class WeeklyTripStatement {
    @SerializedName("status_message")
    var statusMessage: String? = null

    @SerializedName("symbol")
    var symbol: String? = null

    @SerializedName("status_code")
    var statusCode: String? = null

    @SerializedName("currency_code")
    var currencyCode: String? = null

    @SerializedName("trip_week_details")
    var tripWeekDetails: ArrayList<Statement>? = null

    @SerializedName("current_page")
    var currentPage: Int? = null

    @SerializedName("total_page")
    var totalPage: Int? = null

    inner class Statement {
        @SerializedName("date")
        var date: String? = null

        @SerializedName("week")
        var week: String? = null

        @SerializedName("driver_earnings")
        var driverEarnings: String? = null

    }
}
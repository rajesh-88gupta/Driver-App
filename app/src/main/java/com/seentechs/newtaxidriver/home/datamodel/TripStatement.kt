package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TripStatement {

    @Expose
    @SerializedName("status_message")
    var status_message: String? = null
    @Expose
    @SerializedName("status_code")
    var status_code: String? = null

    class Trip_details {
        @Expose
        @SerializedName("invoice")
        var invoice: List<Invoice>? = null
        @Expose
        @SerializedName("total_fare")
        var total_fare: String? = null
        @Expose
        @SerializedName("status")
        var status: String? = null
        @Expose
        @SerializedName("currency_code")
        var currency_code: String? = null
        @Expose
        @SerializedName("end_trip")
        var end_trip: String? = null
        @Expose
        @SerializedName("begin_trip")
        var begin_trip: String? = null
        @Expose
        @SerializedName("vehicleName")
        var vehicle_name: String? = null
        @Expose
        @SerializedName("car_id")
        var car_id: Int = 0
        @Expose
        @SerializedName("drop_location")
        var drop_location: String? = null
        @Expose
        @SerializedName("pickup_location")
        var pickup_location: String? = null
        @Expose
        @SerializedName("trip_id")
        var trip_id: Int = 0
        @Expose
        @SerializedName("id")
        var id: Int = 0
    }

    class Invoice {
        @Expose
        @SerializedName("colour")
        var colour: String? = null
        @Expose
        @SerializedName("bar")
        var bar: String? = null
        @Expose
        @SerializedName("value")
        var value: String? = null
        @Expose
        @SerializedName("key")
        var key: String? = null
    }
}

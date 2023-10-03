package com.seentechs.newtaxidriver.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TripStatusModel {

    @SerializedName("trip_id")
    @Expose
    val tripId: Int? = null
    @SerializedName("trip_path")
    @Expose
    var tripPath: String? = null
    @SerializedName("car_name")
    @Expose
    var carName: String? = null
    @SerializedName("map_image")
    @Expose
    var mapImage: String? = null
    @SerializedName("pickup_latitude")
    @Expose
    var pickupLatitude: String? = null
    @SerializedName("pickup_longitude")
    @Expose
    var pickupLongitude: String? = null
    @SerializedName("drop_latitude")
    @Expose
    var dropLatitude: String? = null
    @SerializedName("drop_longitude")
    @Expose
    var dropLongitude: String? = null
    @SerializedName("booking_type")
    @Expose
    var bookingType: String? = null
    @SerializedName("currency_symbol")
    @Expose
    var currencySymbol: String? = null
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("rider_thumb_image")
    @Expose
    var riderThumbImage: String? = null

    /**
     * Manual booking Details
     * @return
     */

    @SerializedName("pickup_location")
    @Expose
    var pickupLocation: String? = null

    @SerializedName("drop_location")
    @Expose
    var dropLocation: String? = null

    @SerializedName("schedule_display_date")
    @Expose
    var scheduleDisplayDate: String? = null

    @SerializedName("driver_earnings")
    @Expose
    var driverEarnings: String? = null


}
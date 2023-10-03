package com.seentechs.newtaxidriver.trips.tripsdetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TripListModelArrayList {
    @SerializedName("is_pool")
    @Expose
    var isPool: Boolean? = null

    @SerializedName("seats")
    @Expose
    var seats: Int? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("trip_id")
    @Expose
    var tripId: Int? = null

    @SerializedName("pickup")
    @Expose
    var pickup: String? = null

    @SerializedName("drop")
    @Expose
    var drop: String? = null

    @SerializedName("schedule_display_date")
    @Expose
    var scheduleDisplayDate: String? = null

    @SerializedName("map_image")
    @Expose
    var mapImage: String? = null

    @SerializedName("car_type")
    @Expose
    var carType: String? = null

    @SerializedName("total_fare")
    @Expose
    var totalFare: String? = null

    @SerializedName("driver_earnings")
    @Expose
    var driverEarnings: String? = null

    @SerializedName("booking_type")
    @Expose
    var bookingType: String? = null

    @SerializedName("driver_image")
    @Expose
    var driverImage: String? = null
}
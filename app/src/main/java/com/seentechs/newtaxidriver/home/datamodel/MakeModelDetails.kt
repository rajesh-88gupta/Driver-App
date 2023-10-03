package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.seentechs.newtaxidriver.home.managevehicles.FeaturesInCarModel

class MakeModelDetails {
    @Expose
    @SerializedName("status_code")
    lateinit var status_code: String
    @Expose
    @SerializedName("status_message")
    var status_message: String=  ""
    @Expose
    @SerializedName("year")
    var year: Int=  1990
    @SerializedName("make")
    @Expose
    var make = ArrayList<Make>()
    @SerializedName("vehicle_types")
    @Expose
    var vehicleTypes = ArrayList<VehicleTypes>()
    @SerializedName("request_options")
    @Expose
    var requestOptions = ArrayList<FeaturesInCarModel>()


}

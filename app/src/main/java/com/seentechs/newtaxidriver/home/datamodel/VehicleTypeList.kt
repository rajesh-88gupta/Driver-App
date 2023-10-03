package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class VehicleTypeList {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("name")
    @Expose
    var vehicleName: String? = null

    @SerializedName("is_checked")
    @Expose
    var isChecked = false

    @SerializedName("location")
    @Expose
    var location = "All"


}
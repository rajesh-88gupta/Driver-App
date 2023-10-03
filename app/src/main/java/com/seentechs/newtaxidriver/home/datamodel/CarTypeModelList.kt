package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CarTypeModelList {


    @SerializedName("car_name")
    @Expose
    var carName: String? = null
    @SerializedName("car_number")
    @Expose
    var carNumber: String? = null


}

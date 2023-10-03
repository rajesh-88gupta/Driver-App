package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class CarTypeModel {


    @SerializedName("car_type")
    @Expose
    var carTypeModelList: ArrayList<CarTypeModelList>? = null
}

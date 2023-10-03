package com.seentechs.newtaxidriver.home.managevehicles

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

class FeaturesInCarModel : Serializable {

    @SerializedName("id")
    @Expose
    var id: Int = 0

    @SerializedName("name")
    @Expose
    var name: String = ""

    @SerializedName("isSelected")
    @Expose
    var isSelected: Boolean = false
}
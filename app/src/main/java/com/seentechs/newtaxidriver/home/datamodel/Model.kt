package com.seentechs.newtaxidriver.home.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Model: Serializable {


    @Expose
    @SerializedName("id")
    lateinit var id: String
    @Expose
    @SerializedName("name")
    var name: String=  ""
    @Expose
    @SerializedName("is_seleceted")
    var isSelected: Boolean=  false

}
package com.seentechs.newtaxidriver.google.direction

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.seentechs.newtaxidriver.home.datamodel.StepsClass
import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap

class DirectionDataModel : Serializable {
    var points: ArrayList<LatLng> = ArrayList<LatLng>()
    var routes: List<List<HashMap<String, String>>>? = null
    var distances = ""
    var overviewPolyline = ""
    var stepPoints = ArrayList<StepsClass>()
    var totalDuration: Int = 0
    var polyLineType = ""
    var lineOptions: PolylineOptions = PolylineOptions()
}
package com.seentechs.newtaxidriver.home.datamodel

import java.util.ArrayList

class HeatMap {
    var status_message: String? = null

    var status_code: String? = null

    var heat_map_data: ArrayList<RequestsTwoHour>? = null

    inner class RequestsTwoHour {
        var latitude: String? = null

        var longitude: String? = null


    }
}

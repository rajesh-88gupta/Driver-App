package com.seentechs.newtaxidriver.home.map

/**
 * Created by bowshulsheikrahaman on 29/01/18.
 */

class DriverLocation {
    var lat: String = ""
    var lng: String = ""

    // Default constructor required for calls to
    // DataSnapshot.getPrice(User.class)
    constructor() {}

    constructor(lat: String, lng: String) {
        this.lat = lat
        this.lng = lng
    }
}

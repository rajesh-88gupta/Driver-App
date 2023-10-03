/*
 * Copyright 2015 - 2018 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seentechs.newtaxidriver.google.locationmanager

import android.location.Location
import android.location.LocationManager
import android.os.Build
import java.util.*

class Position {
    constructor() {}
    constructor(location: Location, battery: Double) {
        time = Date(location.time)
        latitude = location.latitude
        longitude = location.longitude
        altitude = location.altitude
        speed = location.speed * 1.943844 // speed in knots
        course = location.bearing.toDouble()
        if (location.provider != null && location.provider != LocationManager.GPS_PROVIDER) {
            accuracy = location.accuracy.toDouble()
        }
        this.battery = battery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mock = location.isFromMockProvider
        }
    }

    var id: Long = 0
    var deviceId: String? = null
    var time: Date? = null
    var latitude = 0.0
    var longitude = 0.0
    var altitude = 0.0
    var speed = 0.0
    var course = 0.0
    var accuracy = 0.0
    var battery = 0.0
    var mock = false
}
package com.seentechs.newtaxidriver.home.interfaces

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage interfaces
 * @category YourTripsListener
 * @author Seen Technologies
 *
 */

import android.content.res.Resources

import com.seentechs.newtaxidriver.trips.tripsdetails.YourTrips


/*****************************************************************
 * YourTripsListener
 */

interface YourTripsListener {

    val res: Resources

    val instance: YourTrips
}

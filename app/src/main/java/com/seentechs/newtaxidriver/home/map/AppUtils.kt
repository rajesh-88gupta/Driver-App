
package com.seentechs.newtaxidriver.home.map

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage map
 * @category AppUtils
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils

import android.content.Context.LOCATION_SERVICE

/* ************************************************************
                      AppUtils
Its used get the the general  details
*************************************************************** */

class AppUtils {
    /*
    *  LOCATION_AND_WRITEPERMISSION_ARRAY constants
    */
    object LocationConstants {
        val SUCCESS_RESULT = 0

        val FAILURE_RESULT = 1

        val PACKAGE_NAME = "com.seentechs.newtaxidriver.map"

        val RECEIVER = "$PACKAGE_NAME.RECEIVER"

        val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"

        val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"

        val LOCATION_DATA_AREA = "$PACKAGE_NAME.LOCATION_DATA_AREA"
        val LOCATION_DATA_CITY = "$PACKAGE_NAME.LOCATION_DATA_CITY"
        val LOCATION_DATA_STREET = "$PACKAGE_NAME.LOCATION_DATA_STREET"


    }

    companion object {


        fun hasLollipop(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        }

        fun isLocationEnabled(context: Context): Boolean {
            var locationMode = 0
            val locationProviders: String

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)

                } catch (e: Settings.SettingNotFoundException) {
                    e.printStackTrace()
                }

                return locationMode != Settings.Secure.LOCATION_MODE_OFF
            } else {
                locationProviders = Settings.Secure.getString(context.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
                return !TextUtils.isEmpty(locationProviders)
            }
        }

        fun isGPSEnabled(mContext: Context): Boolean {
            val service = mContext.getSystemService(LOCATION_SERVICE) as LocationManager
            return service.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        fun openLocationEnableScreen(mContext: Context) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(intent)
        }
    }

}

package com.seentechs.newtaxidriver.google.direction

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.datamodel.StepsClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.lang.Double
import java.net.URL
import java.util.*
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection


class GetDirectionData(activity: Context?) {

    private var activity: Context? = null

    @Inject
    lateinit var sessionManager: SessionManager

    init {
        this.activity = activity
        AppController.getAppComponent().inject(this)
    }


    suspend fun directionParse(polyLineType: String, origin: LatLng, dest: LatLng): DirectionDataModel {

        var routes: List<List<HashMap<String, String>>>? = null
        var distances = ""
        var overviewPolyline = ""
        var stepPoints = ArrayList<StepsClass>()
        var totalDuration = 0
        val points: ArrayList<LatLng> = ArrayList()
        val lineOptions = PolylineOptions()

        withContext(Dispatchers.IO) {


            var result: String?


            val url = getDirectionsUrl(origin, dest)

            result = url.let { urlParser(it) }


            val jObject: JSONObject

            try {
                jObject = JSONObject(result)
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
                distances = parser.parseDistance(jObject)
                overviewPolyline = parser.parseOverviewPolyline(jObject)
                stepPoints = parser.parseStepPoints(jObject)
                totalDuration = parser.parseDuration(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }


            if (routes != null) {
                for (i in routes!!.indices) {

                    val path = routes!![i]

                    for (j in path.indices) {
                        val point = path[j]

                        val lat = Double.parseDouble(point["lat"]!!)
                        val lng = Double.parseDouble(point["lng"]!!)
                        val position = LatLng(lat, lng)
                        points.add(position)
                    }

                    lineOptions.addAll(points)
                    lineOptions.width(6f)
                    lineOptions.color(ContextCompat.getColor(activity!!, R.color.newtaxi_app_navy))
                    lineOptions.geodesic(true)

                }

            }

        }

        val directionDataModel = DirectionDataModel()
        directionDataModel.distances = distances
        directionDataModel.polyLineType = polyLineType
        directionDataModel.points = points
        directionDataModel.overviewPolyline = overviewPolyline
        directionDataModel.stepPoints = stepPoints
        directionDataModel.totalDuration = totalDuration
        directionDataModel.lineOptions = lineOptions
        return directionDataModel
    }

    private fun urlParser(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpsURLConnection? = null
        try {
            val url = URL(strUrl)

            urlConnection = url.openConnection() as HttpsURLConnection

            urlConnection.connect()

            iStream = urlConnection.inputStream


            val sb = iStream.bufferedReader().use(BufferedReader::readText)


            data = sb


        } catch (e: Exception) {
            CommonMethods.DebuggableLogD("Exception", e.toString())
        } finally {
            iStream?.close()
            urlConnection?.disconnect()
        }
        return data
    }

    /*
     *  Get direction for given locations
     */
    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        val sensor = "sensor=false"
        val mode = "mode=driving"
        val parameters = "$str_origin&$str_dest&$sensor&$mode"

        val output = "json"

        CommonMethods.DebuggablePrintln("Direction", "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + sessionManager.googleMapKey)
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + sessionManager.googleMapKey
    }
}
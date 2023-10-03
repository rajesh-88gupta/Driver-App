package com.seentechs.newtaxidriver.home.map.drawpolyline

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage map.drawpolyline
 * @category DownloadTask
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AlertDialog
import com.seentechs.newtaxidriver.common.network.AppController

import com.seentechs.newtaxidriver.common.util.CommonMethods

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.URL

import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import kotlin.jvm.Throws

class DownloadTask(polylineOptionsInterface: PolylineOptionsInterface, internal var mContext: Context) : AsyncTask<String, Void, String>() {

    internal var polylineOptionsInterface: PolylineOptionsInterface
    lateinit @Inject
    var commonMethods: CommonMethods
    var dialog: AlertDialog

    init {
        this.polylineOptionsInterface = polylineOptionsInterface
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(mContext)
    }

    override fun doInBackground(vararg url: String): String {

        var data = ""

        try {
            if (commonMethods.isOnline(mContext)) {
                data = downloadUrl(url[0])
            } else {
                //commonMethods.showMessage(mContext, dialog, mContext.getResources().getString(R.string.no_connection));
            }


        } catch (e: Exception) {
            CommonMethods.DebuggableLogD("Background Task", e.toString())
        }

        return data
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        val parserTask = ParserTask(this.polylineOptionsInterface, mContext)

        if (commonMethods.isOnline(mContext)) {
            parserTask.execute(result)
        } else {
            //commonMethods.showMessage(mContext, dialog, mContext.getResources().getString(R.string.no_connection));
        }

    }


    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
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
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    /**
     * A method to download json data from url
     */
   /* @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            urlConnection = url.openConnection() as HttpURLConnection

            urlConnection.connect()

            iStream = urlConnection.inputStream

            val br = BufferedReader(InputStreamReader(iStream))

            val sb = StringBuffer()

            var line = br.readLine()
            if(line != null) {
                sb.append(line)
            }

            data = sb.toString()

            br.close()

        } catch (e: Exception) {
            CommonMethods.DebuggableLogD("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }*/
}
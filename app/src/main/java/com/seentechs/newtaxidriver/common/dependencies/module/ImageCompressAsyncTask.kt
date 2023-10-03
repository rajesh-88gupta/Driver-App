package com.seentechs.newtaxidriver.common.dependencies.module

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage backgroundtask
 * @category ImageCompressAsyncTask
 * @author Seen Technologies
 *
 */

import android.graphics.Bitmap
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.home.interfaces.ImageListener
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/*****************************************************************
 * Compress image while upload in background
 */
class ImageCompressAsyncTask(type: Int?, activity: AppCompatActivity, filePath: String?, private val imageListener: ImageListener?, expirydate: String) : AsyncTask<Void, Void, Void>() {
    @Inject
    lateinit var sessionManager: SessionManager
    private var filePath = ""
    private var compressPath = ""
    private val compressImgWeakRef: WeakReference<AppCompatActivity>?
    private var requestBody: RequestBody? = null
    private var type = 0
    private var docType = ""
    private var uploadType = "image"
    private var expirydate = ""

    init {
        AppController.getAppComponent().inject(this)
        this.compressImgWeakRef = WeakReference(activity)
        if (!filePath.isNullOrEmpty()) {
            this.filePath = filePath
        }
        if (type != null) {
            this.type = type
        }

        if(expirydate.isNotEmpty())
        {
            this.expirydate=expirydate
        }


    }

    /**
     * Call when before call the WS.
     */
    override fun onPreExecute() {
        if (this.compressImgWeakRef == null) {
            this.cancel(true)
        }
    }

    /**
     * action to be performed in background
     */
    override fun doInBackground(vararg params: Void): Void? {
        try {

            if(filePath.isEmpty())
            {
                requestBody = uploadImgParam(compressPath, docType, uploadType,expirydate)
                return null
            }
            var file = File(filePath)
            if (file.exists()) {
                publishProgress()
                file = Compressor.Builder(this.compressImgWeakRef!!.get()).setMaxWidth(1080f).setMaxHeight(1920f).setQuality(75).setCompressFormat(Bitmap.CompressFormat.JPEG).build().compressToFile(file)
                compressPath = file.path
                /**
                 * @param type  1 for licence back,
                 * 2 for licence front,
                 * 3 for insurance
                 * 4 for registeration
                 * 5 for carriage
                 */
                if (type == 1)
                    docType = "license_back"
                else if (type == 2)
                    docType = "license_front"
                else if (type == 3)
                    docType = "insurance"
                else if (type == 4)
                    docType = "rc"
                else if (type == 5)
                    docType = "permit"
                else if (type == 6) {
                    docType = "profile_image"
                    uploadType = "image"
                } else if (type == 7) {
                    docType = "document"
                    uploadType = "document"
                }  else if (type == 8) {
                    docType = "Driver"

                }   else if (type == 9) {
                    docType = "Vehicle"

                } else {
                    docType = "image"
                    uploadType = "image"
                }


                requestBody = uploadImgParam(compressPath, docType, uploadType,expirydate)
            }
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * called after the WS return the response.
     */
    override fun onPostExecute(value: Void?) {
        if (compressImgWeakRef != null && compressImgWeakRef.get() != null && requestBody != null) {
            imageListener?.onImageCompress(compressPath, requestBody)
        } else {
            imageListener?.onImageCompress(compressPath, null)
        }
    }


    /**
     * To upload image
     * @param imagePath path of the image
     * @param docType type  of the document
     * @param uploadType typr of upload
     * @return returns of the type request body
     * @throws IOException
     */


    @Throws(IOException::class)
    fun uploadImgParam(imagePath: String, docType: String, uploadType: String,expirydate: String): RequestBody {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        var file: File?


        multipartBody.addFormDataPart("token", sessionManager.accessToken!!)
        multipartBody.addFormDataPart("expired_date", expirydate)

            try {


                if(type == 8 || type == 9 ){

                    multipartBody.addFormDataPart("document_id", sessionManager.documentId!!)

                    if(type == 9){
                        multipartBody.addFormDataPart("vehicle_id", sessionManager.vehicleId!!)
                        multipartBody.addFormDataPart("type", "Vehicle")

                    }else{
                        multipartBody.addFormDataPart("type", "Driver")
                    }





                    if(imagePath.isNotEmpty()) {

                        file = File(imagePath)
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        multipartBody.addFormDataPart("document_image", "IMG_$timeStamp.jpg", RequestBody.create("image/png".toMediaTypeOrNull(), file))
                        //multipartBody.addFormDataPart("document_image", "IMG_$timeStamp.jpg", RequestBody.create(file, MediaType.parse("image/*")))
                    }

                } else{

                    file = File(imagePath)
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())


                    multipartBody.addFormDataPart(uploadType, "IMG_$timeStamp.jpg", RequestBody.create("image/png".toMediaTypeOrNull(), file))
                    multipartBody.addFormDataPart("document_type", docType)
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }



        return multipartBody.build()
    }
}

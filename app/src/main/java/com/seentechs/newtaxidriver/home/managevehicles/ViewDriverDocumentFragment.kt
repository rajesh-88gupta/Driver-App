package com.seentechs.newtaxidriver.home.managevehicles

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.seentechs.newtaxidriver.BuildConfig
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.dependencies.module.ImageCompressAsyncTask
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.UPLOAD_DRIVER_DOCUMENT
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.util.RuntimePermissionDialogFragment
import com.seentechs.newtaxidriver.databinding.ViewDocumentLayoutBinding
import com.seentechs.newtaxidriver.home.datamodel.AddDocumentDetails
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ImageListener
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.view_document_layout.*
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ViewDriverDocumentFragment : Fragment(), ServiceListener, ImageListener, RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback {


    private var addDocumentDetails = AddDocumentDetails()
    lateinit var viewDocument: View

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog
    private var dialog: AlertDialog? = null
    private lateinit var imageFile: File
    private lateinit var imageUri: Uri
    private var imagePath: String = ""
    private var isImageuupload: Boolean = false
    var docType = 8
    var documentUrl: String? = null
    private lateinit var binding: ViewDocumentLayoutBinding

    @BindView(R.id.rlt_expiredate)
    lateinit var rltExpiryDate: RelativeLayout
    var isExpiryDateChanged: Boolean = false

    @BindView(R.id.tv_expiredate_text)
    lateinit var tvExpiryDate: TextView

    @BindView(R.id.ivImage)
    lateinit var ivImage: ImageView

    /**
     * Add Docs
     */
    @OnClick(R.id.rltTapToAdd)
    fun uploadImage() {
        RuntimePermissionDialogFragment.checkPermissionStatus(activity!!, activity!!.supportFragmentManager, this, RuntimePermissionDialogFragment.CAMERA_PERMISSION_ARRAY, 0, 0)
    }


    @OnClick(R.id.rlt_expiredate)
    fun OnExpiryDate() {
        val c = Calendar.getInstance()
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_datepicker)
        // set the custom dialog components - text, image and button
        val cancel = dialog.findViewById<View>(R.id.datepicker_cancel) as TextView
        val picker = dialog.findViewById<View>(R.id.datePicker) as DatePicker
        val ok = dialog.findViewById<View>(R.id.datepicker_ok) as TextView
        // if button is clicked, close the custom dialog
        cancel.setOnClickListener { dialog.dismiss() }

        picker.minDate = c.getTimeInMillis()
        ok.setOnClickListener {
            c.set(picker.year, picker.month, picker.dayOfMonth);
            val format = SimpleDateFormat("yyyy-MM-dd");
            val datestring = format.format(c.getTime());
            tvExpiryDate.setText(datestring)
            isExpiryDateChanged = true
            dialog.dismiss()
        }
        dialog.show()
        /* val c = Calendar.getInstance(Locale.US)
         val mYear = c.get(Calendar.YEAR)
         val mMonth = c.get(Calendar.MONTH)
         val mDay = c.get(Calendar.DAY_OF_MONTH)
         val datePickerDialog = DatePickerDialog(activity,
                 DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                     c.set(Calendar.YEAR,year)
                     c.set(Calendar.MONTH,monthOfYear)
                     c.set(Calendar.DAY_OF_MONTH,dayOfMonth)
                     val  myFormat = "yyyy-MM-dd";
                     val sdf =  SimpleDateFormat(myFormat, Locale.US)
                     println("Expiry date: "+sdf.format(c.time))
                     tvExpiryDate.text=sdf.format(c.time)
                    *//* val expiryDate = SimpleDateFormat(myFormat, Locale("en"))
                    println("exp Date "+expiryDate.format(c.time))*//*

                }, mYear, mMonth, mDay)

        datePickerDialog.datePicker.minDate = c.getTimeInMillis()

        datePickerDialog.show()*/
    }

    @OnClick(R.id.btnNext)
    fun OnNext() {

        if (!sessionManager.isTrip) {
            if (isExpiryDateChanged || isImageuupload) {
                if (documentUrl != null && !documentUrl.equals("")) {

                    if (!imagePath.equals("") || isExpiryDateChanged) {
                        val expirydate = tvExpiryDate.text.toString()
                        commonMethods.showProgressDialog((activity as DocumentDetails).getAppCompatActivity())
                        sessionManager.documentId = (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentId.toString()
                        ImageCompressAsyncTask(docType, (activity as DocumentDetails).getAppCompatActivity(), imagePath, this, expirydate).execute()

                    } else {
                        (activity as DocumentDetails).onBackPressed()

                    }
                } else {

                    val expirydate = tvExpiryDate.text.toString()
                    if (!TextUtils.isEmpty(imagePath) || expirydate.isNotEmpty()) {
                        commonMethods.showProgressDialog((activity as DocumentDetails).getAppCompatActivity())
                        sessionManager.documentId = (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentId.toString()
                        ImageCompressAsyncTask(docType, (activity as DocumentDetails).getAppCompatActivity(), imagePath, this, expirydate).execute()
                    } else if (TextUtils.isEmpty(imagePath) && isImageuupload) {
                        Toast.makeText(context, resources.getString(R.string.please_upload_legal_document), Toast.LENGTH_SHORT).show()
                    } else if (expirydate.isEmpty()) {
                        Toast.makeText(context, resources.getString(R.string.please_expiry_date), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                (activity as DocumentDetails).onBackPressed()
            }
        } else {
            Toast.makeText((activity as DocumentDetails), "you can't update document While  you are in trip ", Toast.LENGTH_LONG).show()
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        binding = DataBindingUtil.inflate(
                inflater, R.layout.view_document_layout, container, false);


        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, binding.root)

        (activity as DocumentDetails).setHeader((activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentName!!)

        dialog = commonMethods.getAlertDialog(context!!)

        binding.viewDoc = (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!)

        val isexpiryRequired = (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).expiryRequired!!
        documentUrl = (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentUrl

        if (isexpiryRequired.equals("1"))
            rltExpiryDate.visibility = View.VISIBLE
        else
            rltExpiryDate.visibility = View.GONE

        return binding.root
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(context, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            UPLOAD_DRIVER_DOCUMENT ->
                if (jsonResp.isSuccess) {
                    onSuccessDriverDocument(jsonResp)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
                }

            else -> {
            }
        }
    }

    private fun onSuccessDriverDocument(jsonResp: JsonResponse) {
        addDocumentDetails = gson.fromJson(jsonResp.strResponse, AddDocumentDetails::class.java)
        /*addDocumentDetails = gson.fromJson(jsonResp.strResponse, AddDocumentDetails::class.java)
        (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentUrl = addDocumentDetails.document.get((activity as DocumentDetails).documentPosition!!).documentUrl
        (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentStatus = "0"
        (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).expiredDate = addDocumentDetails.document.get((activity as DocumentDetails).documentPosition!!).expiredDate
        */
        imagePath = ""
        isImageuupload = false
        (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentUrl = addDocumentDetails.documentUrl
        (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentStatus = "0"
        (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).expiredDate = tvExpiryDate.text.toString()
        (activity as DocumentDetails).onBackPressed()
    }


    /**
     * Bottom Sheet to choose camera or gallery
     */

    private fun documentUpload() {
        val view = layoutInflater.inflate(R.layout.app_camera_dialog_layout, null)
        val lltCamera = view.findViewById<LinearLayout>(R.id.llt_camera)
        val lltLibrary = view.findViewById<LinearLayout>(R.id.llt_library)
        val lltcancel = view.findViewById<LinearLayout>(R.id.llt_cancel)


        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)
        /* bottomSheetDialog.setCancelable(true)*/
        /*if (bottomSheetDialog.window == null) return
        bottomSheetDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)*/
        if (!bottomSheetDialog.isShowing) {
            bottomSheetDialog.show()
        }

        lltCamera.setOnClickListener {
            cameraIntent()
            bottomSheetDialog.dismiss()
        }

        lltLibrary.setOnClickListener {
            imageFile = commonMethods.getDefaultFileName(requireContext())

            galleryIntent()
            bottomSheetDialog.dismiss()
        }
        lltcancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    /**
     * Intent to camera
     */
    private fun cameraIntent() {
        imageFile = commonMethods!!.cameraFilePath(requireContext())
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageUri = activity?.let { it1 -> imageFile?.let { it2 -> FileProvider.getUriForFile(it1, BuildConfig.APPLICATION_ID + ".provider", it2) } }
        try {
            val resolvedIntentActivities = activity?.packageManager?.queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedIntentActivities != null) {
                for (resolvedIntentInfo in resolvedIntentActivities) {
                    val packageName = resolvedIntentInfo.activityInfo.packageName
                    activity?.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            cameraIntent.putExtra("return-data", true)
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, Constants.PICK_IMAGE_REQUEST_CODE)
        activity?.let { it1 -> commonMethods.refreshGallery(it1, imageFile) }
    }

    /**
     * Intent to gallery page
     */
    private fun galleryIntent() {
        imageFile = commonMethods.getDefaultFileName(requireContext())
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, Constants.SELECT_FILE)
    }

    override fun onFailure(jsonResp: JsonResponse?, data: String?) {

    }


    /**
     * To fetch RequestBody from ImageCompressAsyncTask
     * @param filePath file path of the image
     * @param requestBody request body from image compress async task
     */

    override fun onImageCompress(filePath: String, requestBody: RequestBody?) {
        commonMethods.hideProgressDialog()
        if (requestBody != null) {
            commonMethods.showProgressDialog((activity as DocumentDetails).getAppCompatActivity())
            apiService.updateDocument(requestBody, sessionManager.accessToken!!).enqueue(RequestCallback(UPLOAD_DRIVER_DOCUMENT, this))
        }
    }


    /*
      * Get image path from gallery
      */
    private fun onSelectFromGalleryResult(data: Intent?) {

        var bm: Bitmap? = null
        if (data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = context!!.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor?.moveToFirst()
            val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
            //   val picturePath = cursor.getString(columnIndex)
            val picturePath = columnIndex?.let { cursor.getString(it) }
            cursor?.close()
            var exif: ExifInterface? = null
            try {
                exif = picturePath?.let { ExifInterface(it) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val orientation: Int = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED) ?: 1
            bm = BitmapFactory.decodeFile(picturePath)
            val bmRotated: Bitmap? = rotateBitmap(bm, orientation)
            ivImage.setImageBitmap(bmRotated)
            tvTapToAdd.setText(context!!.resources.getString(R.string.taptochange))
            imagePath = picturePath.toString()
            isImageuupload = true

            /* if (!TextUtils.isEmpty(imagePath)) {
                 commonMethods.showProgressDialog((activity as DocumentDetails).getAppCompatActivity())
                 sessionManager.documentId = (activity as DocumentDetails).documentDetails.get((activity as DocumentDetails).documentPosition!!).documentId.toString()

                 ImageCompressAsyncTask(docType, (activity as DocumentDetails).getAppCompatActivity(), imagePath, this, expirydate).execute()
             }*/
        }
    }

    fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale((-1).toFloat(), 1F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180F)
                matrix.postScale((-1).toFloat(), 1F)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90F)
                matrix.postScale((-1).toFloat(), 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate((-90).toFloat())
                matrix.postScale((-1).toFloat(), 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate((-90).toFloat())
            else -> return bitmap
        }
        return try {
            val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            bmRotated
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return null
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.PICK_IMAGE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                    if (imageFile == null) return
                    imagePath = imageFile.path
                    isImageuupload = true
                    var exif: ExifInterface? = null
                    try {
                        exif = ExifInterface(imagePath)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val orientation: Int = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED) ?: 1
                    val bm = BitmapFactory.decodeFile(imagePath)
                    val bmRotated: Bitmap? = rotateBitmap(bm, orientation)
                    ivImage.setImageBitmap(bmRotated)
                    tvTapToAdd.text = context!!.resources.getString(R.string.taptochange)
                }
                Constants.SELECT_FILE -> try {
                    val inputStream = activity?.contentResolver?.openInputStream(data!!.data!!)
                    val fileOutputStream = FileOutputStream(imageFile)
                    commonMethods.copyStream(inputStream, fileOutputStream)
                    fileOutputStream.close()
                    inputStream?.close()
                    if (imageFile == null) return
                    imagePath = imageFile.path
                    var exif: ExifInterface? = null
                    try {
                        exif = imagePath?.let { ExifInterface(it) }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val orientation: Int = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED) ?: 1
                    val bm = BitmapFactory.decodeFile(imagePath)
                    val bmRotated: Bitmap? = rotateBitmap(bm, orientation)
                    ivImage.setImageBitmap(bmRotated)
                    tvTapToAdd.text = requireContext().resources.getString(R.string.taptochange)
                    isImageuupload = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                else -> {
                }
            }
        }
    }


    override fun permissionGranted(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {
        documentUpload()
    }

    override fun permissionDenied(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {

    }


}

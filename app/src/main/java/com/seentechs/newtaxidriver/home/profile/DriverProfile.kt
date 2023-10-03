package com.seentechs.newtaxidriver.home.profile

/**
 * @package com.seentechs.newtaxidriver.home.profile
 * @subpackage profile model
 * @category DriverProfile
 * @author Seen Technologies
 *
 */


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.dependencies.module.ImageCompressAsyncTask
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.helper.Constants.Female
import com.seentechs.newtaxidriver.common.helper.Constants.Male
import com.seentechs.newtaxidriver.common.helper.Constants.PICK_IMAGE_REQUEST_CODE
import com.seentechs.newtaxidriver.common.helper.Constants.SELECT_FILE
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.helper.RunTimePermission
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.REQ_DRIVER_PROFILE
import com.seentechs.newtaxidriver.common.util.Enums.REQ_UPDATE_PROFILE
import com.seentechs.newtaxidriver.common.util.Enums.REQ_UPLOAD_PROFILE_IMG
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.util.RuntimePermissionDialogFragment
import com.seentechs.newtaxidriver.common.util.RuntimePermissionDialogFragment.Companion.CAMERA_PERMISSION_ARRAY
import com.seentechs.newtaxidriver.common.util.RuntimePermissionDialogFragment.Companion.checkPermissionStatus
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.facebookAccountKit.FacebookAccountKitActivity
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ImageListener
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.app_activity_driver_profile.*
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject


/* ************************************************************
                DriverProfile
Its used to get driver profile details to show view on screen
*************************************************************** */

class DriverProfile : CommonActivity(), ServiceListener, ImageListener, RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback {
    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var runTimePermission: RunTimePermission

    @Inject
    lateinit var sessionManager: SessionManager

    /*@BindView(R.id.editicon)
    lateinit var editicon: ImageView*/
    /*   @BindView(R.id.cameraicon)
       lateinit var cameraicon: ImageView*/
    @BindView(R.id.arrow)
    lateinit var arrow: ImageView

    @BindView(R.id.input_first)
    lateinit var input_first: EditText

    @BindView(R.id.input_last)
    lateinit var input_last: EditText

    @BindView(R.id.emaitext)
    lateinit var emaitext: EditText

    @BindView(R.id.mobile_number)
    lateinit var mobile_number: TextView

    @BindView(R.id.addresstext)
    lateinit var addresstext: EditText

    @BindView(R.id.addresstext2)
    lateinit var addresstext2: EditText

    @BindView(R.id.citytext)
    lateinit var citytext: EditText

    @BindView(R.id.posttext)
    lateinit var posttext: EditText

    @BindView(R.id.statetext)
    lateinit var statetext: EditText

    /*  @BindView(R.id.input_layout_first)
      lateinit var input_layout_first: TextInputLayout
      @BindView(R.id.input_layout_last)
      lateinit var input_layout_last: TextInputLayout
      @BindView(R.id.emailName)
      lateinit var emailName: TextInputLayout
      @BindView(R.id.addressName)
      lateinit var addressName: TextInputLayout
      @BindView(R.id.addressName2)
      lateinit var addressName2: TextInputLayout
      @BindView(R.id.cityName)
      lateinit var cityName: TextInputLayout
      @BindView(R.id.stateName)
      lateinit var stateName: TextInputLayout
      @BindView(R.id.postName)
      lateinit var postName: TextInputLayout*/
    /*@BindView(R.id.close)
    lateinit var close: TextView*/
    @BindView(R.id.savebutton)
    lateinit var savebutton: Button

    @BindView(R.id.mobile_code)
    lateinit var ccp: CountryCodePicker

    @BindView(R.id.profile_image1)
    lateinit var profile_image1: ImageView

    @BindView(R.id.flaglayout)
    lateinit var flaglayout: RelativeLayout

    @BindView(R.id.rg_gender)
    lateinit var radiogroupGender: RadioGroup


    lateinit var first_name: String
    lateinit var last_name: String
    lateinit var mobile_numbers: String
    lateinit var country_code: String
    lateinit var email_id: String
    lateinit var user_thumb_image: String
    lateinit var address_line1: String
    lateinit var address_line2: String
    lateinit var city: String
    lateinit var state: String
    lateinit var postal_code: String
    lateinit var gender: String
    var bm: Bitmap? = null
    var docType = 6          //By Default Set 6 for Profile Image
    private var isUpdate = false
    private lateinit var dialog: AlertDialog
    private var imagePath: String = ""
    lateinit private var imageFile: File
    private var imageUri: Uri? = null

    /**
     * Getting Driver Deatils to Update
     *
     * @return hashmap Datas
     */
    private val details: LinkedHashMap<String, String>
        get() {
            val hashMap = LinkedHashMap<String, String>()
            hashMap["first_name"] = first_name
            hashMap["last_name"] = last_name
            hashMap["email_id"] = email_id
            hashMap["mobile_number"] = mobile_numbers
            hashMap["country_code"] = ccp.selectedCountryNameCode.replace("\\+".toRegex(), "")
            hashMap["address_line1"] = address_line1
            hashMap["address_line2"] = address_line2
            hashMap["city"] = city
            hashMap["state"] = state
            hashMap["postal_code"] = postal_code
            hashMap["token"] = sessionManager.accessToken!!
            hashMap["profile_image"] = user_thumb_image
            return hashMap
        }

    @OnClick(R.id.back)
    fun backpressed() {
        onBackPressed()
    }


    @OnClick(R.id.savebutton)
    fun savebutton() {
        getValidation()
    }

    /*
     * upload profile image
     */
    @OnClick(R.id.edit_image)
    fun profileimage1() {
        checkAllPermission()
    }

/*    @OnClick(R.id.cameraicon)
    fun cameraicon() {
        checkAllPermission()
    }*/

    @OnClick(R.id.flaglayout)
    fun flag() {
        /*sessionManager.setisEdit(true);
        Intent mobile = new Intent(getApplicationContext(), MobileActivity.class);
        startActivity(mobile);*/

        FacebookAccountKitActivity.openFacebookAccountKitActivity(this)
    }

    @OnClick(R.id.mobile_number)
    fun mobilenumber() {
        FacebookAccountKitActivity.openFacebookAccountKitActivity(this)
    }

    /*@OnClick(R.id.mobile_code)
    public void mobilecode() {
        sessionManager.setisEdit(true);
        Intent mobile = new Intent(getApplicationContext(), MobileActivity.class);
        startActivity(mobile);
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_driver_profile)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.profile), common_header)

        dialog = commonMethods.getAlertDialog(this)

        input_first.addTextChangedListener(NameTextWatcher(input_first))
        input_last.addTextChangedListener(NameTextWatcher(input_last))
        emaitext.addTextChangedListener(NameTextWatcher(emaitext))
        addresstext.addTextChangedListener(NameTextWatcher(addresstext))
        addresstext2.addTextChangedListener(NameTextWatcher(addresstext2))
        citytext.addTextChangedListener(NameTextWatcher(citytext))
        statetext.addTextChangedListener(NameTextWatcher(statetext))
        posttext.addTextChangedListener(NameTextWatcher(posttext))

        for (i in 0 until radiogroupGender.childCount) {
            radiogroupGender.getChildAt(i).isClickable = false
        }
        ccp.setCcpClickable(false)

        /*
         * Get driver profile API call
         */
        val profiledetails = sessionManager.profileDetail
        if (profiledetails == null) {
            getDriver()
        } else {
            /*
             *  Load driver profile API
             *
             */
            loadData(profiledetails)
        }

        input_first.isFocusableInTouchMode = true
        input_last.isFocusableInTouchMode = true
        emaitext.isFocusableInTouchMode = true
        addresstext.isFocusableInTouchMode = true
        addresstext2.isFocusableInTouchMode = true
        citytext.isFocusableInTouchMode = true
        statetext.isFocusableInTouchMode = true
        posttext.isFocusableInTouchMode = true
        /*  cameraicon.visibility = View.VISIBLE*/


    }

    private fun getValidation() {
        if (!validateText(input_first)) {
            return
        }
        if (!validateText(input_last)) {
            return
        }

        if (!validateEmail()) {
            return
        }
        if (!validatePhone()) {
            return
        }
        if (!validateText(addresstext)) {
            return
        }
        /*if (!validateText(addresstext2)) {
            return
        }*/
        if (!validateText(citytext)) {
            return
        }
        if (!validateText(statetext)) {
            return
        }
        if (!validateText(posttext)) {
            return
        }

        /*
         *  get Driver profile data
         */
        first_name = input_first.text.toString()
        last_name = input_last.text.toString()
        email_id = emaitext.text.toString()
        mobile_numbers = mobile_number.text.toString()
        address_line1 = addresstext.text.toString()
        address_line2 = addresstext2.text.toString()
        city = citytext.text.toString()
        state = statetext.text.toString()
        postal_code = posttext.text.toString()
        isUpdate = true

        updateDriver()
    }

    private fun updateDriver() {
        commonMethods.showProgressDialog(this)
        apiService.updateDriverProfile(details).enqueue(RequestCallback(REQ_UPDATE_PROFILE, this))
    }

    /**
     * Api call to fetch profile details
     */

    private fun getDriver() {
        commonMethods.showProgressDialog(this)
        apiService.getDriverProfile(sessionManager.accessToken!!).enqueue(RequestCallback(REQ_DRIVER_PROFILE, this))
    }

    /**
     * Upload Image using Post Method
     */
    private fun onSuccessUploadImage(jsonResponse: JsonResponse) {
        Toast.makeText(this, R.string.image_uploaded, Toast.LENGTH_SHORT).show()
        user_thumb_image = commonMethods.getJsonValue(jsonResponse.strResponse, Constants.PROFILEIMAGE, String::class.java) as String
        loadImage(user_thumb_image)
    }

    /**
     * Load Image
     */
    private fun loadImage(imageurl: String) {
        commonMethods.hideProgressDialog()
        Picasso.get().load(imageurl).into(profile_image1)

    }

    private fun checkAllPermission() {

        checkPermissionStatus(this, supportFragmentManager, this, CAMERA_PERMISSION_ARRAY, 0, 0)
    }

    /**
     * If Deny open and Enable the permission
     */
    /* private fun showEnablePermissionDailog(type: Int, message: String) {
         if (!customDialog.isVisible) {
             customDialog = CustomDialog("Alert", message, getString(R.string.ok), CustomDialog.btnAllowClick {
                 override fun clicked() {
                     if (type == 0)
                         callPermissionSettings()
                     else
                         startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 101)
                 }
             })
             customDialog.show(supportFragmentManager, "")
         }
     }*/

    /**
     * Opens the APP Permission Settings Page
     */
    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
    }

    /**
     * Bottom Sheet to choose camera or gallery
     */

    fun pickProfileImg() {
        val view = layoutInflater.inflate(R.layout.app_camera_dialog_layout, null)
        val lltCamera = view.findViewById<LinearLayout>(R.id.llt_camera)
        val lltLibrary = view.findViewById<LinearLayout>(R.id.llt_library)
        val lltcancel = view.findViewById<LinearLayout>(R.id.llt_cancel)


        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)
        /* bottomSheetDialog.setCancelable(true)*/
        /*if (bottomSheetDialog.window == null) return
        bottomSheetDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)*/
        if (!bottomSheetDialog.isShowing) {
            bottomSheetDialog.show()
        }

        lltCamera.setOnClickListener {
            bottomSheetDialog.dismiss()
            cameraIntent()
            bottomSheetDialog.dismiss()
        }

        lltLibrary.setOnClickListener {
            bottomSheetDialog.dismiss()
            imageFile = commonMethods.getDefaultFileName(this@DriverProfile)

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
        imageFile = commonMethods!!.cameraFilePath(this)
        commonMethods.cameraIntent(imageFile, this)
    }

    /**
     * Intent to gallery page
     */
    private fun galleryIntent() {
        imageFile = commonMethods.getDefaultFileName(this)
        commonMethods.galleryIntent(this)
    }

    /*
     *  Get data from camera and gallery
     */


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                if (imageFile == null) return
                imagePath = imageFile.path

                if (!TextUtils.isEmpty(imagePath)) {
                    commonMethods.showProgressDialog(this)
                    ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
                }
            }
            SELECT_FILE -> try {
                val inputStream = this.contentResolver.openInputStream(data!!.data!!)
                val fileOutputStream = FileOutputStream(imageFile)
                commonMethods.copyStream(inputStream, fileOutputStream)
                fileOutputStream.close()
                inputStream?.close()
                if (imageFile == null) return
                imagePath = imageFile.path
                if (!TextUtils.isEmpty(imagePath)) {
                    commonMethods.showProgressDialog(this)
                    ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT -> {
                /*  if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_NEW_USER) {
                    updateDriverPhoneNumber(data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY), data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY));
                } else if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_OLD_USER) {
                    commonMethods.showMessage(this, dialog, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY));

                }*/

                if (resultCode == Activity.RESULT_OK) {
                    updateDriverPhoneNumber(data!!.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY)!!, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY)!!)
                }

            }
            else -> {
            }
        }

    }

    fun updateDriverPhoneNumber(phoneNumber: String, countryCode: String) {
        if (phoneNumber != null) {
            mobile_number.setText(phoneNumber)
            ccp.setCountryForNameCode(countryCode)

        }

    }

    /*
     * Get image path from gallery
     */
    private fun onSelectFromGalleryResult(data: Intent?) {

        bm = null
        if (data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            /*  cursor.close()*/
            bm = BitmapFactory.decodeFile(picturePath)

            imagePath = picturePath

            if (!TextUtils.isEmpty(imagePath)) {
                commonMethods.showProgressDialog(this)
                ImageCompressAsyncTask(docType, this, imagePath, this, "").execute()
            }
        }
    }


    override fun onImageCompress(filePath: String, requestBody: RequestBody?) {
        requestBody?.let { apiService.uploadProfileImage(it, sessionManager.accessToken!!).enqueue(RequestCallback(REQ_UPLOAD_PROFILE_IMG, this)) }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_UPDATE_PROFILE -> {
                val statusmessage = jsonResp.statusMsg  //(String) commonMethods.getJsonValue(jsonResp.getStrResponse(), "status_message", String.class);
                if (jsonResp.isSuccess) {
                    sessionManager.profileDetail = jsonResp.strResponse

                    if (isUpdate) {
                        isUpdate = false
                        commonMethods.showMessage(this, dialog, resources.getString(R.string.update_successfully))

                    } else {
                        loadData(jsonResp.strResponse)
                        //jsonResp.strResponse?.let { loadData(it) }
                    }
                } else {
                    commonMethods.showMessage(this, dialog, statusmessage)
                }
            }
            REQ_UPLOAD_PROFILE_IMG -> if (jsonResp.isSuccess) {
                onSuccessUploadImage(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            REQ_DRIVER_PROFILE -> {
                val currency_code = commonMethods.getJsonValue(jsonResp.strResponse, "currency_code", String::class.java) as String
                val dialcode = commonMethods.getJsonValue(jsonResp.strResponse, "country_code", String::class.java) as String
                var currency_symbol = commonMethods.getJsonValue(jsonResp.strResponse, "currency_symbol", String::class.java) as String
                //val carid = commonMethods.getJsonValue(jsonResp.strResponse, "car_id", String::class.java) as String
                val oweAmount = commonMethods.getJsonValue(jsonResp.strResponse, "owe_amount", String::class.java) as String
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    currency_symbol = Html.fromHtml(currency_symbol, Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    currency_symbol = Html.fromHtml(currency_symbol).toString()
                }
                sessionManager.currencyCode = currency_code
                sessionManager.countryCode = dialcode
                sessionManager.currencySymbol = currency_symbol
                //sessionManager.vehicle_id = carid
                sessionManager.oweAmount = oweAmount
            }
            else -> {
            }
        }


    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    fun loadData(profiledetails: String) {
        try {
            val jsonObj = JSONObject(profiledetails)

            first_name = jsonObj.getString("first_name")
            last_name = jsonObj.getString("last_name")
            mobile_numbers = jsonObj.getString("mobile_number")
            country_code = jsonObj.getString("country_code")
            CommonMethods.DebuggableLogI("Country code from api", country_code)
            email_id = jsonObj.getString("email_id")
            user_thumb_image = jsonObj.getString("profile_image")
            address_line1 = jsonObj.getString("address_line1")
            address_line2 = jsonObj.getString("address_line2")
            city = jsonObj.getString("city")
            state = jsonObj.getString("state")
            postal_code = jsonObj.getString("postal_code")
            gender = jsonObj.getString("gender")



            if ("" != first_name) input_first.setText(first_name)

            if ("" != last_name) input_last.setText(last_name)

            if ("" != email_id) emaitext.setText(email_id)

            if ("" != mobile_numbers) mobile_number.setText(mobile_numbers)
            sessionManager.phoneNumber = mobile_numbers

            if ("" != address_line1) addresstext.setText(address_line1)

            if ("" != address_line2) addresstext2.setText(address_line2)

            if ("" != city) citytext.setText(city)

            if ("" != state) statetext.setText(state)

            if ("" != postal_code) posttext.setText(postal_code)
            if ("" != sessionManager.countryCode) {
                ccp.setCountryForNameCode(sessionManager.countryCode)
            }
            Picasso.get().load(user_thumb_image).into(profile_image1)

            if (gender.isNotEmpty()) {
                if (gender.equals(Male, true)) {
                    radiogroupGender.check(R.id.rd_male)
                } else if (gender.equals(Female, true)) {
                    radiogroupGender.check(R.id.rd_female)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun validateText(input: EditText): Boolean {
        if (input.text.toString().trim { it <= ' ' }.isEmpty()) {
            if (input.id == R.id.input_first)
                return false
            else if (input.id == R.id.input_last)
                return false
            else if (input.id == R.id.addresstext)
                return false
            /* else if (input.id == R.id.addresstext2)
                 return false*/
            else if (input.id == R.id.citytext)
                return false
            else if (input.id == R.id.statetext)
                return false
            else if (input.id == R.id.posttext)
                return false
            requestFocus(input)
            return false
        } else {
        }

        return true
    }

    /*
     *  Validate Email address
     */
    private fun validateEmail(): Boolean {
        val email = emaitext.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {
            /*emailName.error = getString(R.string.error_msg_email)
            requestFocus(emaitext)*/
            return false
        } else {
            /* emailName.isErrorEnabled = false*/
        }

        return true
    }

    /*
     *  Validate phone number
     */
    private fun validatePhone(): Boolean {
        if (mobile_number.text.toString().trim { it <= ' ' }.isEmpty() || mobile_number.text.toString().length < 6) {
            commonMethods.showMessage(this, dialog, getString(R.string.error_msg_phone))
            requestFocus(mobile_number)
            return false
        }

        return true
    }

    /*
     * Edit address request focus
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.getisEdit()) {
            mobile_number.setText(sessionManager.phoneNumber)
            CommonMethods.DebuggableLogI("Country code from session", sessionManager.countryCode)
            ccp.setCountryForNameCode(sessionManager.countryCode)
            sessionManager.setisEdit(false)
        }
    }

    override fun permissionGranted(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {
        pickProfileImg()
    }

    override fun permissionDenied(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {

    }

    /*
     *  edit taxtwatcher
     */
    private inner class NameTextWatcher internal constructor(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            CommonMethods.DebuggableLogV("Do", "Nothing")
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            CommonMethods.DebuggableLogV("Do", "Nothing")
            if (validateEmail() && validateText(input_first) && validateText(input_last) && validateText(addresstext) && validateText(citytext) && validateText(statetext) && validateText(posttext)) {
                savebutton.isEnabled = true
                savebutton.background = ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_navy)
            } else {
                savebutton.isEnabled = false
                savebutton.background = ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_navy_disable)
            }/*&& validateText(addresstext) && validateText(citytext) && validateText(statetext) && validateText(posttext)*/
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {

                R.id.input_first -> validateText(input_first)
                R.id.input_last -> validateText(input_last)
                R.id.emaitext -> validateEmail()
                R.id.mobile_number -> validatePhone()
                R.id.addresstext -> validateText(addresstext)
                //  R.id.addresstext2 -> validateText(addresstext2)
                R.id.citytext -> validateText(citytext)
                R.id.statetext -> validateText(statetext)
                R.id.posttext -> validateText(posttext)
                else -> {
                }
            }
        }
    }

    companion object {

        /*
     * check is valid email or not
     */
        private fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

}

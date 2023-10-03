package com.seentechs.newtaxidriver.home.signinsignup

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage signinsignup model
 * @category Register
 * @author Seen Technologies
 *
 */

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.legacy.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.Constants.Female
import com.seentechs.newtaxidriver.common.helper.Constants.Male
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.REQ_REG
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.LoginDetails
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.app_activity_register.*
import kotlinx.android.synthetic.main.app_common_button_large.*
import java.util.*
import javax.inject.Inject

/* ************************************************************
                Register
Its used to get the driver register detail function
*************************************************************** */
class Register : CommonActivity(), ServiceListener {
    lateinit var dialog: AlertDialog
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var customDialog: CustomDialog
//    @BindView(R.id.input_first)
//    lateinit var input_first: EditText
    @BindView(R.id.input_referral)
    lateinit var input_referral: EditText
    @BindView(R.id.input_layout_referral)
    lateinit var input_layout_referral: TextInputLayout
//    @BindView(R.id.input_last)
//    lateinit var input_last: EditText
    @BindView(R.id.emaitext)
    lateinit var emaitext: EditText
    @BindView(R.id.passwordtext)
    lateinit var passwordtext: EditText
    @BindView(R.id.cityText)
    lateinit var cityText: EditText
    @BindView(R.id.mobile_number)
    lateinit var mobile_number: EditText
    /*@BindView(R.id.input_layout_first)
    lateinit var input_layout_first: TextInputLayout*/
   /* @BindView(R.id.input_layout_last)
    lateinit var input_layout_last: TextInputLayout*/
   /* @BindView(R.id.emailName)
    lateinit var emailName: TextInputLayout
    @BindView(R.id.passwordName)
    lateinit var passwordName: TextInputLayout*/
    @BindView(R.id.cityName)
    lateinit var cityName: TextInputLayout
    @BindView(R.id.mobile_code)
    lateinit var ccp: CountryCodePicker
    /*@BindView(R.id.btn_continue)
    lateinit var btn_continue: Button*/
    @BindView(R.id.loginlink)
    lateinit var loginlink: TextView
    /*@BindView(R.id.dochome_back)
    lateinit var dochome_back: ImageView*/
   /* @BindView(R.id.scrollView)
    lateinit var scrollView: ScrollView*/
    @BindView(R.id.location_placesearch)
    lateinit var mRecyclerView: RecyclerView

    @BindView(R.id.rg_gender)
    lateinit var genderRadioGroup: RadioGroup

    private var selectedGender : String?= null


    protected lateinit var mGoogleApiClient: GoogleApiClient
    protected var isInternetAvailable: Boolean = false
    private var oldstring = ""
    private var isCity = false


    var facebookKitVerifiedMobileNumber = ""
    var facebookVerifiedMobileNumberCountryCode = ""
    var facebookVerifiedMobileNumberCountryNameCode = ""
    /*
     *   Text watcher for city search
     */


    @OnClick(R.id.loginlink)
    fun loginLink() {
        val intent = Intent(applicationContext, SigninActivity::class.java)
        startActivity(intent)
        finish()
    }

    @OnClick(R.id.button)
    fun btnContinue() {
        numberRegister()
    }

    @OnClick(R.id.arrow)
    fun dochomeBack() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.app_activity_register)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)

        /**Commmon Header Text View  and Button View*/
        commonMethods.setheaderText(resources.getString(R.string.register),common_header)
        commonMethods.setButtonText(resources.getString(R.string.continues),common_button)

        //commonMethods.imageChangeforLocality(this,dochome_back)
        getMobileNumerFromIntentAndSetToEditText()
        dialog = commonMethods.getAlertDialog(this)

        showOrHideReferralAccordingToSessionData()
        isInternetAvailable = commonMethods.isOnline(this)

        //error_mob.visibility = View.GONE

        input_first.addTextChangedListener(NameTextWatcher(input_first))
        input_last.addTextChangedListener(NameTextWatcher(input_last))
        emaitext.addTextChangedListener(NameTextWatcher(emaitext))
        passwordtext.addTextChangedListener(NameTextWatcher(passwordtext))
        cityText.addTextChangedListener(NameTextWatcher(cityText))
        mobile_number.addTextChangedListener(NameTextWatcher(mobile_number))


        mRecyclerView.visibility = View.GONE


        //mobile_number.setText(sessionManager.getPhoneNumber());
        //ccp.setCountryForPhoneCode(Integer.parseInt(sessionManager.getCountryCode()));
        //mobile_number.setKeyListener(null);
        //mobile_number.setEnabled(false);

        genderRadioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            val radioButton = radioGroup.findViewById(i) as RadioButton
            if (radioButton.id == R.id.rd_male){
                selectedGender  = Male
            }else if (radioButton.id == R.id.rd_female){
                selectedGender = Female
            }
            validAllViews()
        })

        /*
         *   Redirect to signin page
         */
        loginlink.setOnClickListener {
            val intent = Intent(applicationContext, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }

        /*
         *   Validate registration fields
         */
        button.setOnClickListener { numberRegister() }

        //dochome_back.setOnClickListener { onBackPressed() }

        ccp.setOnCountryChangeListener {
            ccp.selectedCountryName //  Toast.makeText(getApplicationContext(), "Updated " + ccp.getSelectedCountryName(), Toast.LENGTH_SHORT).show();
        }
        if (Locale.getDefault().language == "fa") {
            ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        }
        /** Setting mobile number depends upon country code */

        button.isClickable = false
        button.background = ContextCompat.getDrawable(this,R.drawable.app_curve_button_navy_disable)
    }

    private fun showOrHideReferralAccordingToSessionData() {
        if (sessionManager.isReferralOptionEnabled) {
            input_layout_referral.visibility = View.VISIBLE
        } else {
            input_layout_referral.visibility = View.GONE
        }
    }

    private fun getMobileNumerFromIntentAndSetToEditText() {
        if (intent != null) {
            facebookKitVerifiedMobileNumber =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY).toString()
            facebookVerifiedMobileNumberCountryCode =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY).toString()
            facebookVerifiedMobileNumberCountryNameCode =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY).toString()
        }
        mobile_number.setText(facebookKitVerifiedMobileNumber)
        mobile_number.isEnabled = false

        ccp.setCountryForNameCode(facebookVerifiedMobileNumberCountryNameCode)
        ccp.setCcpClickable(false)


    }

    private fun numberRegister() {


        isInternetAvailable = commonMethods.isOnline(this)
        if (!validateFirst()) {
            return
        }
        if (!validateLast()) {
            return
        }
        if (!validateEmail()) {      //setting error message in submit button
            //.error = getString(R.string.error_msg_email)
            return
        } else {
           // emailName.error = null
        }
        if (!validatePhone()) {
            //error_mob.visibility = View.VISIBLE
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.error_red))
            ViewCompat.setBackgroundTintList(mobile_number, colorStateList)
            return
        } else {
            //error_mob.visibility = View.GONE
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.app_continue))

           // val colorStateList = ColorStateList.valueOf(resources.getColor(R.color.app_continue))
            ViewCompat.setBackgroundTintList(mobile_number, colorStateList)
        }
        if (selectedGender.isNullOrEmpty()){
            Toast.makeText(this, resources.getString(R.string.error_gender), Toast.LENGTH_SHORT).show();
            return
        }
        if (!validatePassword()) {
            passwordName.error = getString(R.string.error_msg_password)
            return
        } else {
            passwordName.error = null
        }

        if (!validateCity()) {
            return
        }


        /*sessionManager.setFirstName(input_first.getText().toString());
        sessionManager.setLastName(input_last.getText().toString());
        sessionManager.setEmail(emaitext.getText().toString());
        sessionManager.setTemporaryPhonenumber(mobile_number.getText().toString());
        sessionManager.setPassword(passwordtext.getText().toString());
        sessionManager.setTemporaryCountryCode(ccp.getSelectedCountryCodeWithPlus().replaceAll("\\+", ""));
        sessionManager.setCity(cityText.getText().toString());*/

        if (isInternetAvailable) {
            commonMethods.showProgressDialog(this@Register)
            //apiService.numberValidation(sessionManager.getType(), sessionManager.getTemporaryPhonenumber(), sessionManager.getTemporaryCountryCode(), "", sessionManager.getLanguageCode()).enqueue(new RequestCallback(REQ_VALIDATE_NUMBER),this);
            apiService.registerOtp(
                sessionManager.type!!,
                facebookKitVerifiedMobileNumber,
                facebookVerifiedMobileNumberCountryNameCode,
                emaitext.text.toString(),
                input_first.text.toString(),
                input_last.text.toString(),
                passwordtext.text.toString(),
                cityText.text.toString(),
                sessionManager.deviceId!!,
                sessionManager.deviceType!!,
                sessionManager.languageCode!!,
                input_referral.text.toString().trim { it <= ' ' },"email","", selectedGender!!)
                .enqueue(RequestCallback(REQ_REG, this))

        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.Interneterror))
        }
    }

    /*
     *   Validate first name
     */
    private fun validateFirst(): Boolean {
        if (input_first.text.toString().trim { it <= ' ' }.isEmpty()) {
            //input_layout_first.error = getString(R.string.error_msg_firstname)
            //requestFocus(input_first)
            return false
        } else {
            //input_layout_first.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Validate last name
     */
    private fun validateLast(): Boolean {
        if (input_last.text.toString().trim { it <= ' ' }.isEmpty()) {
            //input_layout_last.error = getString(R.string.error_msg_lastname)
            //requestFocus(input_last)
            return false
        } else {
            //input_layout_last.isErrorEnabled = false
        }
        return true
    }

    /*
     *   Validate email address
     */
    private fun validateEmail(): Boolean {
        val email = emaitext.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {
            //emailName.setError(getString(R.string.error_msg_email));
            //requestFocus(emaitext)
            return false
        } else {
            //emailName.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Validate phone number
     */
    private fun validatePhone(): Boolean {
        if (mobile_number.text.toString().trim { it <= ' ' }.isEmpty() || mobile_number.text.toString().length < 6) {

            //requestFocus(mobile_number)
            return false
        } else {
           //error_mob.visibility = View.GONE
            val colorStateList = ColorStateList.valueOf(resources.getColor(R.color.newtaxi_app_navy))
            ViewCompat.setBackgroundTintList(mobile_number, colorStateList)
        }
        return true
    }

    /*
     *   Validate city
     */
    private fun validateCity(): Boolean {

        isCity = cityText.text.toString() != ""


        if (!isCity) {
            if (cityText.text.toString() == "") {
                //cityName.error = getString(R.string.error_msg_city)
            }else{
                //cityName.isErrorEnabled = false
            }
            //requestFocus(cityText)
            return false
        } else {
            //cityName.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Validate password
     */
    private fun validatePassword(): Boolean {
        if (passwordtext.text.toString().trim { it <= ' ' }.isEmpty() || passwordtext.text.toString().length < 6) {
            //requestFocus(passwordtext)
            return false
        } else {
            passwordName.isErrorEnabled = false
        }

        return true
    }

    private fun validReferral(): Boolean {
        if (input_referral.text.toString().trim { it <= ' ' }.isEmpty() || input_referral.text.toString().length < 6) {
            //requestFocus(input_first)
            return false
        } else {
            input_layout_referral.isErrorEnabled = false
        }

        return true
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_REG -> {
                onSuccessRegisterPwd(jsonResp)
            }
        }


    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val signin = Intent(applicationContext, SigninSignupHomeActivity::class.java)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }




    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }

    /*
     *
     *   Edit text, Text watcher
     */
    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            CommonMethods.DebuggableLogI("i Check", Integer.toString(i))
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
           /* if (mobile_number.text.toString().startsWith("0")) {
                mobile_number.setText("")
            }*/
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                R.id.input_first -> validateFirst()
                R.id.input_last -> validateLast()
                R.id.emaitext -> validateEmail()
                R.id.passwordtext -> validatePassword()
                R.id.mobile_number -> validatePhone()
                R.id.cityText -> validateCity()
                else -> {
                }
            }
            validAllViews()
        }
    }

    fun validAllViews(){
        if(validateFirst() && validateLast() && validateEmail() && validatePhone() && !selectedGender.isNullOrEmpty() && validatePassword() && validateCity()){
            button.isClickable = true
            button.background = ContextCompat.getDrawable(this,R.drawable.app_curve_button_navy)
        }else{
            button.isClickable = false
            button.background = ContextCompat.getDrawable(this,R.drawable.app_curve_button_navy_disable)
        }
    }

    private fun onSuccessRegisterPwd(jsonResp: JsonResponse) {
        commonMethods.hideProgressDialog()

        val signInUpResultModel = gson.fromJson(jsonResp.strResponse, LoginDetails::class.java)

        if (signInUpResultModel != null) {

            try {

                if (signInUpResultModel.statusCode.matches("1".toRegex())) {


                    val carDeailsModel = gson.toJson(signInUpResultModel.carDetailModel)
                    sessionManager.carType = carDeailsModel

                    /*JSONArray cardetails = new JSONArray(carDeailsModel);

                    StringBuilder type = new StringBuilder();
                    type.append(getResources().getString(R.string.vehicle_type)).append(",");
                    for (int i = 0; i < cardetails.length(); i++) {
                        JSONObject cartype = cardetails.getJSONObject(i);
                        type.append(cartype.getString("car_name")).append(",");
                    }*/

                    sessionManager.userId = signInUpResultModel.userID

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        sessionManager.currencySymbol =
                            Html.fromHtml(signInUpResultModel.currencySymbol,Html.FROM_HTML_MODE_LEGACY).toString()
                    }else{
                        Html.fromHtml(signInUpResultModel.currencySymbol).toString()
                    }
                    sessionManager.currencyCode = signInUpResultModel.currencyCode
                    sessionManager.paypalEmail = signInUpResultModel.payoutId
                    sessionManager.driverSignupStatus = signInUpResultModel.userStatus
                    /*sessionManager.setType(type.toString());*/
                    sessionManager.setAcesssToken(signInUpResultModel.token)
                    sessionManager.isRegister = true
                    commonMethods.hideProgressDialog()
                    startMainActivity()


                } else {
                    commonMethods.showMessage(this, dialog, signInUpResultModel.statusMessage)

                }


            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


    }


    private fun startMainActivity() {
        val x = Intent(applicationContext, MainActivity::class.java)
        x.putExtra("signinup", true)
        val bndlanimation = ActivityOptions.makeCustomAnimation(
                applicationContext,
                R.anim.cb_fade_in,
                R.anim.cb_face_out
        ).toBundle()
        startActivity(x, bndlanimation)
        finish()
    }

    companion object {

        private val BOUNDS_INDIA = LatLngBounds(
            LatLng(-0.0, 0.0), LatLng(0.0, 0.0)
        )
        private val TAG = "Register"

        /*
     *   Check email is valid or not
     */
        private fun isValidEmail(email: String): Boolean {
            return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }
}

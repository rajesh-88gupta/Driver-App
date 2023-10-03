package com.seentechs.newtaxidriver.home.signinsignup

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage signinsignup model
 * @category SigninActivity
 * @author Seen Technologies
 *
 */

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.LoginDetails
import com.seentechs.newtaxidriver.home.facebookAccountKit.FacebookAccountKitActivity
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.app_activity_signin.*
import kotlinx.android.synthetic.main.app_common_button_large.*
import org.json.JSONException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

/* ************************************************************
                SigninActivity
Its used to  get the signin detail function
*************************************************************** */
class SigninActivity : CommonActivity(), ServiceListener {


    lateinit var dialog: AlertDialog
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

    internal lateinit var context: Context


    /*@BindView(R.id.input_layout_username)
    lateinit var input_layout_username: TextInputLayout*/
   /* @BindView(R.id.input_layout_mobile)
    lateinit var input_layout_mobile: TextInputLayout*/
    /*@BindView(R.id.input_layout_passsword)
     lateinit var input_layout_passsword: TextInputLayout*/
    /*@BindView(R.id.user_edit)
     lateinit var user_edit: EditText*/
    @BindView(R.id.phone)
     lateinit var phone: EditText
    @BindView(R.id.ccp)
     lateinit var ccp: CountryCodePicker
    @BindView(R.id.password_edit)
    lateinit var password_edit: EditText
   /* @BindView(R.id.sigin_button)
     lateinit var sigin_button: Button*/
    @BindView(R.id.forgot_password)
   lateinit  var forgot_password: RelativeLayout
  /*  @BindView(R.id.dochome_back)
     lateinit var dochome_back: ImageView*/
    protected var isInternetAvailable: Boolean = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT) {
            /*if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_NEW_USER) {
                commonMethods.showMessage(this, dialog, data.getStringExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY));
            } else if (resultCode == CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_OLD_USER) {
                openPasswordResetActivity(data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY), data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY));
            }*/

            if (resultCode == Activity.RESULT_OK) {
                openPasswordResetActivity(
                    data!!.getStringExtra(
                        FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
                    ).toString(), data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY).toString(),
                        data.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY).toString()
                )
            }
        }


    }

    private fun openPasswordResetActivity(phoneNumber: String, countryCode: String,countryNamecode:String) {

        val signin = Intent(applicationContext, ResetPassword::class.java)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY, phoneNumber)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY, countryCode)
        signin.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY, countryNamecode)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
        finish()

    }

    @OnClick(R.id.forgot_password)
    fun forgetPassword() {
        /*Intent intent = new Intent(getApplicationContext(), MobileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);*/
        FacebookAccountKitActivity.openFacebookAccountKitActivity(this, 1)
    }

    @OnClick(R.id.arrow)
    fun backPressed() {
        onBackPressed()
    }

    @OnClick(R.id.button)
    fun signIn() {
        var input_password_str = password_edit.text.toString()

        isInternetAvailable = commonMethods.isOnline(this)
        val phonenumber_str = phone.text.toString()

        try {
            input_password_str = URLEncoder.encode(input_password_str, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        sessionManager.password = input_password_str
        sessionManager.phoneNumber = phonenumber_str
        sessionManager.countryCode =
                ccp.selectedCountryNameCode.replace("\\+".toRegex(), "")

        isInternetAvailable = commonMethods.isOnline(this)
        if (isInternetAvailable) {


            if (!validateMobile("check")) {
               // input_layout_mobile.error = getString(R.string.error_msg_mobilenumber)
            }
            getUserProfile()
            // new SigninSignup().execute(url);
        } else {
            commonMethods.showMessage(
                applicationContext,
                dialog,
                resources.getString(R.string.no_connection)
            )

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_signin)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        //commonMethods.imageChangeforLocality(this,dochome_back)

        /**Commmon Header Text View and */
        commonMethods.setheaderText(resources.getString(R.string.signin),common_header)
        commonMethods.setButtonText(resources.getString(R.string.signin),common_button)

        dialog = commonMethods.getAlertDialog(this)
        ccp.setAutoDetectedCountry(true)
        if (Locale.getDefault().language == "fa" || Locale.getDefault().language == "ar") {
            ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        }

        context = this

        button.isEnabled = false
        //user_edit.addTextChangedListener(NameTextWatcher(user_edit))
        password_edit.addTextChangedListener(NameTextWatcher(password_edit))
        phone.addTextChangedListener(NameTextWatcher(phone))

        button.isEnabled = false
        button.background = resources.getDrawable(R.drawable.app_curve_button_navy_disable)


    }


    fun getUserProfile() {
        commonMethods.showProgressDialog(this)

        val input_password_str = password_edit.text.toString().trim { it <= ' ' }
        sessionManager.password = input_password_str


        apiService.login(
            sessionManager.phoneNumber!!,
            sessionManager.type!!,
            sessionManager.countryCode!!,
            input_password_str,
            sessionManager.deviceId!!,
            sessionManager.deviceType!!,
            sessionManager.languageCode!!
        ).enqueue(RequestCallback(this))

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val signin = Intent(applicationContext, SigninSignupHomeActivity::class.java)
        startActivity(signin)
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            try {
                onSuccessLogin(jsonResp)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }


    @Throws(JSONException::class)
    private fun onSuccessLogin(jsonResp: JsonResponse) {

        val signInUpResultModel = gson.fromJson(jsonResp.strResponse, LoginDetails::class.java)
        if (signInUpResultModel != null) {

            val driverStatus = signInUpResultModel.userStatus
            sessionManager.userId = signInUpResultModel.userID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sessionManager.currencySymbol =
                    Html.fromHtml(signInUpResultModel.currencySymbol,Html.FROM_HTML_MODE_LEGACY).toString()
            }else{
                Html.fromHtml(signInUpResultModel.currencySymbol).toString()
            }
            sessionManager.currencyCode = signInUpResultModel.currencyCode
            sessionManager.countryCode = signInUpResultModel.country_code
            sessionManager.paypalEmail = signInUpResultModel.payoutId
            sessionManager.driverSignupStatus = signInUpResultModel.userStatus
            sessionManager.setAcesssToken(signInUpResultModel.token)
            sessionManager.isRegister = true
            sessionManager.countryCode = signInUpResultModel.country_code
            sessionManager.phoneNumber = signInUpResultModel.mobileNumber
            sessionManager.userType = signInUpResultModel.companyId
            if (driverStatus == "Car_details") {

                val carDeailsModel = gson.toJson(signInUpResultModel.carDetailModel)


                sessionManager.carType = carDeailsModel
                startMainActivity()
                finishAffinity()
            } else if (driverStatus == "Document_details") {
                // If driver status is document_details then redirect to document upload page

                startMainActivity()
                finishAffinity()
            } else if (driverStatus.equals("Pending", ignoreCase = true)) {

                sessionManager.vehicle_id = signInUpResultModel.vehicleId


                startMainActivity()
            } else if (driverStatus == "Active") {
                // If driver status is active check paypal email is exists then redirect to home page otherwise redirect to paypal email address page
                sessionManager.vehicle_id = signInUpResultModel.vehicleId
                startMainActivity()
            } else {
                // Redirect to sign in signup home page
                val x = Intent(applicationContext, SigninSignupHomeActivity::class.java)
                val bndlanimation = ActivityOptions.makeCustomAnimation(
                    applicationContext,
                    R.anim.cb_fade_in,
                    R.anim.cb_face_out
                ).toBundle()
                startActivity(x, bndlanimation)
                finishAffinity()

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
        finishAffinity()
    }

    /*
     *   Validate phone number
     */
    private fun validateMobile(type: String): Boolean {
        if (phone.text.toString().trim { it <= ' ' }.isEmpty() || phone.text.length < 6) {
            if ("check" == type) {
                //input_layout_mobile.setError(getString(R.string.error_msg_mobilenumber));
                requestFocus(phone)
            }
            return false
        } else {
           // input_layout_mobile.isErrorEnabled = false
        }


        return true
    }

    /*
     *   validate password
     */
    private fun validateLast(type: String): Boolean {
        if (password_edit.text.toString().trim { it <= ' ' }.isEmpty() || password_edit.text.length < 6) {
            if ("check" == type) {
                //input_layout_passsword.setError(getString(R.string.error_msg_password));
                requestFocus(password_edit)
            }
            return false
        } else {
            input_layout_passsword.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Validate user name
     */
    /*private fun validateFirst(): Boolean {
        if (user_edit.text.toString().trim { it <= ' ' }.isEmpty()) {
            input_layout_username.error = getString(R.string.error_msg_firstname)
            requestFocus(user_edit)
            return false
        } else {
            input_layout_username.isErrorEnabled = false
        }

        return true
    }*/

    /*
     *   focus edit text
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }


    /*
     *   Text watcher for validate signin fields
     */
    private inner class NameTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (validateLast("validate") && validateMobile("validate")) {
                button.isEnabled = true
                button.background = resources.getDrawable(R.drawable.app_curve_button_navy)
            } else {

                button.isEnabled = false
                button.background = resources.getDrawable(R.drawable.app_curve_button_navy_disable)
            }
           /* if (phone.text.toString().startsWith("0")) {
                phone.setText("")
            }*/
        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {
                //R.id.user_edit -> validateFirst()
                R.id.password_edit -> validateLast("check")
                R.id.phone -> validateMobile("check")
            }
        }
    }


}

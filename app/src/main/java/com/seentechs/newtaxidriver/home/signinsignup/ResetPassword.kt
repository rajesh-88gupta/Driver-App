package com.seentechs.newtaxidriver.home.signinsignup

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage signinsignup model
 * @category ResetPassword
 * @author Seen Technologies
 *
 */

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.LoginDetails
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.activity_resetpassword.*
import kotlinx.android.synthetic.main.app_activity_register.common_header
import org.json.JSONArray
import org.json.JSONException
import javax.inject.Inject

/* ************************************************************
                ResetPassword
Its used to get the reset password detail function
*************************************************************** */
class ResetPassword : CommonActivity(), ServiceListener {


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

    @BindView(R.id.next)
    lateinit var next: CardView
    @BindView(R.id.input_password)
    lateinit var input_password: EditText
    @BindView(R.id.input_confirmpassword)
    lateinit var input_confirmpassword: EditText
    @BindView(R.id.input_layout_password)
    lateinit var input_layout_password: TextInputLayout
    @BindView(R.id.input_layout_confirmpassword)
    lateinit var input_layout_confirmpassword: TextInputLayout
    @BindView(R.id.progressBar)
    lateinit var progressBar: ProgressBar
    /*@BindView(R.id.backArrow)
    lateinit var backArrow: ImageView*/
    protected var isInternetAvailable: Boolean = false

    @BindView(R.id.nextArrow)
    lateinit var nextArrow: ImageView

    var facebookKitVerifiedMobileNumber = ""
    var facebookVerifiedMobileNumberCountryCode = ""
    var facebookVerifiedMobileNumberCountryNameCode = ""

    @OnClick(R.id.next)
    fun forgetPassword() {
        forgotPwd()
    }

    @OnClick(R.id.back)
    fun backPressed() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpassword)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.resetpasswords),common_header)
        //commonMethods.imageChangeforLocality(this,backArrow)
        //commonMethods.imageChangeforLocality(this,nextArrow)
        getMobileNumerAndCountryCodeFromIntent()
        dialog = commonMethods.getAlertDialog(this)

        isInternetAvailable = commonMethods.isOnline(this)


    }

    private fun getMobileNumerAndCountryCodeFromIntent() {
        if (intent != null) {
            facebookKitVerifiedMobileNumber =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY).toString()
            facebookVerifiedMobileNumberCountryCode =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY).toString()
            facebookVerifiedMobileNumberCountryNameCode =
                    intent.getStringExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY).toString()
        }


    }

    private fun forgotPwd() {

        isInternetAvailable = commonMethods.isOnline(this)


        if (!validateFirst()) {
            return
        } else if (!validateconfrom()) {
            return
        } else {
            val input_password_str = input_password.text.toString().trim { it <= ' ' }
            val input_password_confirmstr =
                input_confirmpassword.text.toString().trim { it <= ' ' }
            if (input_password_str.length > 5 && input_password_confirmstr.length > 5 && input_password_confirmstr == input_password_str) {
                sessionManager.password = input_password_str


                if (isInternetAvailable) {
                    progressBar.visibility = View.VISIBLE
                    nextArrow.visibility = View.GONE

                    apiService.forgotpassword(
                        facebookKitVerifiedMobileNumber,
                        sessionManager.type!!,
                        facebookVerifiedMobileNumberCountryNameCode,
                        input_password_str,
                        sessionManager.deviceType!!,
                        sessionManager.deviceId!!,
                        sessionManager.languageCode!!
                    ).enqueue(RequestCallback(this))

                } else {
                    commonMethods.showMessage(
                        this,
                        dialog,
                        resources.getString(R.string.Interneterror)
                    )
                }

            } else {
                if (input_password_confirmstr != input_password_str) {
                    commonMethods.showMessage(
                        this,
                        dialog,
                        resources.getString(R.string.Passwordmismatch)
                    )
                } else {
                    commonMethods.showMessage(
                        this,
                        dialog,
                        resources.getString(R.string.InvalidPassword)
                    )
                }
            }
        }
    }

    /*
     *   Validate password field
     */
    private fun validateFirst(): Boolean {
        if (input_password.text.toString().trim { it <= ' ' }.isEmpty()) {
            //input_layout_password.error = getString(R.string.Enteryourpassword)
                error_password.visibility = View.VISIBLE
            requestFocus(input_password)
            return false
        } else {
            //input_layout_password.isErrorEnabled = false
            error_password.visibility = View.GONE
        }

        return true
    }

    /*
     *   Validate Confirm password field
     */
    private fun validateconfrom(): Boolean {
        if (input_confirmpassword.text.toString().trim { it <= ' ' }.isEmpty()) {
            error_confirm_password.visibility = View.VISIBLE
            //input_layout_confirmpassword.error = getString(R.string.Confirmyourpassword)
            requestFocus(input_confirmpassword)
            return false
        } else {
            error_confirm_password.visibility = View.GONE
            //input_layout_confirmpassword.isErrorEnabled = false
        }

        return true
    }

    /*
     *   Focus edit text field
     */
    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onBackPressed() {

        val intent = Intent(this, SigninSignupHomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
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
                onSuccessResetPWd(jsonResp)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            progressBar.visibility = View.GONE
            nextArrow.visibility = View.VISIBLE
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    @Throws(JSONException::class)
    private fun onSuccessResetPWd(jsonResp: JsonResponse) {
        progressBar.visibility = View.GONE
        nextArrow.visibility = View.VISIBLE
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
            sessionManager.paypalEmail = signInUpResultModel.payoutId
            sessionManager.driverSignupStatus = signInUpResultModel.userStatus
            sessionManager.setAcesssToken(signInUpResultModel.token)
            sessionManager.isRegister = true
            sessionManager.userType = signInUpResultModel.companyId

            if (driverStatus == "Car_details") {

                val carDeailsModel = gson.toJson(signInUpResultModel.carDetailModel)

                val cardetails = JSONArray(carDeailsModel)

                val carType = StringBuilder()
                carType.append(resources.getString(R.string.vehicle_type)).append(",")
                for (i in 0 until cardetails.length()) {
                    val cartype = cardetails.getJSONObject(i)

                    carType.append(cartype.getString("car_name")).append(",")
                }
                sessionManager.carType = carType.toString()
                startMainActivity()
                finish()
            } else if (driverStatus == "Document_details") {
                // If driver status is document_details then redirect to document upload page
                startMainActivity()
                finish()
            } else if (driverStatus == "pending") {

                // If driver status is pending check paypal email is exists then redirect to home page otherwise redirect to paypal email address page
                sessionManager.vehicle_id = signInUpResultModel.vehicleId
                /*if (sessionManager.getPaypalEmail().length() > 0) {

                    Intent x = new Intent(getApplicationContext(), MainActivity.class);
                    x.putExtra("signinup", true);
                    Bundle bndlanimation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.cb_fade_in, R.anim.cb_face_out).toBundle();
                    startActivity(x, bndlanimation);
                    finish();
                } else {
                    Intent signin = new Intent(getApplicationContext(), PaymentPage.class);
                    startActivity(signin);
                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
                }*/
                openMainActivity()
            } else if (driverStatus == "Active") {
                // If driver status is active check paypal email is exists then redirect to home page otherwise redirect to paypal email address page
                sessionManager.vehicle_id = signInUpResultModel.vehicleId
                /* if (sessionManager.getPaypalEmail().length() > 0) {
                    Intent x = new Intent(getApplicationContext(), MainActivity.class);
                    x.putExtra("signinup", true);
                    Bundle bndlanimation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.cb_fade_in, R.anim.cb_face_out).toBundle();
                    startActivity(x, bndlanimation);
                    finish();
                } else {
                    Intent signin = new Intent(getApplicationContext(), PaymentPage.class);
                    startActivity(signin);
                    overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);
                }*/openMainActivity()
            } else {
                // Redirect to sign in signup home page
                val x = Intent(applicationContext, SigninSignupHomeActivity::class.java)
                val bndlanimation = ActivityOptions.makeCustomAnimation(
                    applicationContext,
                    R.anim.cb_fade_in,
                    R.anim.cb_face_out
                ).toBundle()
                startActivity(x, bndlanimation)
                finish()

            }


        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        progressBar.visibility = View.GONE
        nextArrow.visibility = View.VISIBLE
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

    fun openMainActivity() {
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


}

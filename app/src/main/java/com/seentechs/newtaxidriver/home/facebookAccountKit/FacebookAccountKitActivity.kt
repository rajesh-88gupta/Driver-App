package com.seentechs.newtaxidriver.home.facebookAccountKit


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY
import com.seentechs.newtaxidriver.common.util.CommonKeys.FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.DebuggableLogD
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.DebuggableLogI
import com.seentechs.newtaxidriver.common.util.Enums
import com.seentechs.newtaxidriver.common.util.Enums.REQ_OTP_VERIFIACTION
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.splash.SplashActivity.Companion.checkVersionModel
import kotlinx.android.synthetic.main.app_activity_mobile_number_verification.*
import java.util.*
import javax.inject.Inject


class FacebookAccountKitActivity : CommonActivity(), ServiceListener {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    internal var isPhoneNumberLayoutIsVisible = true

    @BindView(R.id.tv_mobile_heading)
    lateinit internal var mobileNumberHeading: TextView

    @BindView(R.id.tv_otp_resend_label)
    lateinit internal var tvResendOTPLabel: TextView

    @BindView(R.id.tv_otp_resend_countdown)
    lateinit internal var tvResendOTPCountdown: TextView

    @BindView(R.id.tv_resend_button)
    lateinit internal var tvResendOTP: TextView

    @BindView(R.id.tv_otp_error_field)
    lateinit internal var tvOTPErrorMessage: TextView

    @BindView(R.id.cl_phone_number_input)
    lateinit internal var ctlPhoneNumber: ConstraintLayout

    @BindView(R.id.cl_otp_input)
    lateinit internal var ctlOTP: ConstraintLayout

    @BindView(R.id.pb_number_verification)
    lateinit internal var pbNumberVerification: ProgressBar

    @BindView(R.id.imgv_next)
    lateinit internal var imgvArrow: ImageView

    @BindView(R.id.rl_edittexts)
    lateinit internal var rlEdittexts: RelativeLayout

    @BindView(R.id.one)
    lateinit internal var edtxOne: EditText

    @BindView(R.id.two)
    lateinit internal var edtxTwo: EditText

    @BindView(R.id.three)
    lateinit internal var edtxThree: EditText

    @BindView(R.id.four)
    lateinit internal var edtxFour: EditText

    lateinit @BindView(R.id.phone)
    internal var edtxPhoneNumber: EditText

    lateinit @BindView(R.id.ccp)
    var ccp: CountryCodePicker

    lateinit @BindView(R.id.fab_verify)
    internal var cvNext: CardView

    @BindView(R.id.otp_verify)
    lateinit var cvvNext: CardView

    private var isForForgotPassword = 0
    private var otp = ""
    private var receivedOTPFromServer: String? = null
    private val resendOTPWaitingSecond: Long = 120000
    private var resentCountdownTimer: CountDownTimer? = null
    private var backPressCounter: CountDownTimer? = null
    private var isDeletable = true

    /*lateinit @BindView(R.id.tv_back_phone_arrow)
    internal var tvPhoneBack: TextView

    lateinit @BindView(R.id.tv_back_otp_arrow)
    internal var tvOTPback: TextView*/


    var dialog: AlertDialog? = null

    val FACEBOOK_ACCOUNTKIT_REQUEST_CODE = 157

    lateinit internal var facebookVerifiedPhoneNumber: String
    lateinit internal var facebookVerifiedCountryCode: String

    @OnClick(R.id.fab_verify)
    fun startAnimationd() {
        //startAnimation();
        //callSendOTPAPI()
        if (isPhoneNumberLayoutIsVisible && edtxPhoneNumber.text.toString().length > 5) {
            if (checkVersionModel.otpEnabled) {
                callSendOTPAPI()
            } else {
                redirectToActivity()
            }
        }
        /*showOTPfield();
        showOTPMismatchIssue();*/
    }

    @OnClick(R.id.otp_verify)
    fun otpverify() {
        if (!isPhoneNumberLayoutIsVisible) {
            verifyOTP()
        }
    }


    @OnClick(R.id.tv_resend_button)
    fun resendOTP() {
        callSendOTPAPI()
        //runCountdownTimer();

    }

    private fun redirectToActivity() {
        val returnIntent = Intent()
        returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY, edtxPhoneNumber.text.toString().trim { it <= ' ' })
        returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY, ccp.selectedCountryCodeWithPlus.replace("\\+".toRegex(), ""))
        returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_Name_CODE_KEY, ccp.selectedCountryNameCode)
        //returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY, numberValidationModel.getStatusMessage());
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun verifyOTP() {
        showProgressBarAndHideArrow(true)
        otpVerificationAPI()
    }

    private fun otpVerificationAPI() {
        val otpParams = HashMap<String, String>()
        otpParams["otp"] = otp
        otpParams["country_code"] = ccp.selectedCountryCode
        otpParams["mobile_number"] = edtxPhoneNumber.text.toString()
        apiService.otpVerification(otpParams).enqueue(RequestCallback(Enums.REQ_OTP_VERIFIACTION, this))

    }


    private fun shakeEdittexts() {
        val shake = TranslateAnimation(0f, 20f, 0f, 0f)
        shake.duration = 500
        shake.interpolator = CycleInterpolator(3f)
        rlEdittexts.startAnimation(shake)

    }

    fun showOTPMismatchIssue() {
        shakeEdittexts()
        tvOTPErrorMessage.visibility = View.VISIBLE
    }

    fun runCountdownTimer() {
        tvResendOTP.visibility = View.GONE
        tvResendOTPCountdown.visibility = View.VISIBLE
        tvResendOTPLabel.text = resources.getString(R.string.send_OTP_again_in)
        if (resentCountdownTimer != null) {
            resentCountdownTimer!!.cancel()
        }
        resentCountdownTimer = object : CountDownTimer(resendOTPWaitingSecond, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                tvResendOTPCountdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                tvResendOTPCountdown.visibility = View.GONE
                tvResendOTPLabel.text = resources.getString(R.string.resend_otp)
                tvResendOTP.visibility = View.VISIBLE
            }
        }.start()
    }


    @OnClick(R.id.back)
    fun showPhoneNumberField() {
        if (ctlOTP.visibility == View.VISIBLE) {
            ctlPhoneNumber.visibility = View.VISIBLE
            ctlOTP.visibility = View.GONE
            isPhoneNumberLayoutIsVisible = true
            tvResendOTP.visibility = View.GONE
            tvResendOTPLabel.visibility = View.GONE
            tvResendOTPCountdown.visibility = View.GONE
            resentCountdownTimer!!.cancel()

            if (edtxPhoneNumber.text.toString().length > 5) {
                cvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_navy))
                //cvNext.setCardBackgroundColor(resources.getColor(R.color.light_blue_button_color))
            } else {
                //cvNext.setCardBackgroundColor(resources.getColor(R.color.quantum_grey400))
                cvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_navy_disable))
            }
        } else {
            super.onBackPressed()
        }
    }

    /*@OnClick(R.id.tv_back_phone_arrow)
    fun finishThisActivity() {
        super.onBackPressed()
    }*/

    fun showOTPfield() {
        ctlPhoneNumber.visibility = View.GONE
        ctlOTP.visibility = View.VISIBLE
        isPhoneNumberLayoutIsVisible = false
        runCountdownTimer()
        tvResendOTPLabel.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_mobile_number_verification)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)
        //commonMethods.setheaderText(resources.getString(R.string.register),common_header)
        initViews()
        initOTPTextviewListener()
        //startCallingFacebookKit();
    }

    private fun initViews() {
        getIntentValues()
        ccp.setAutoDetectedCountry(true)
        if (Locale.getDefault().language == "fa" || Locale.getDefault().language == "ar") {
            ccp.changeDefaultLanguage(CountryCodePicker.Language.ARABIC)
        }
        edtxPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (edtxPhoneNumber.text.toString().length > 5) {
                    cvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_navy))
                    //cvNext.setCardBackgroundColor(resources.getColor(R.color.light_blue_button_color))
                } else {
                    //cvNext.setCardBackgroundColor(resources.getColor(R.color.quantum_grey400))
                    cvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_navy_disable))
                }
            }
        })

        initDirectionChanges()
    }

    private fun initDirectionChanges() {
        val laydir = resources.getString(R.string.layout_direction)

        if ("1" == laydir) {
            cvNext.rotation = 180f
            cvvNext.rotation = 180f
            //tvPhoneBack.rotation = 180f
            //tvOTPback.rotation = 180f

        }
    }

    private fun getIntentValues() {
        try {
            isForForgotPassword = intent.getIntExtra("usableType", 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun initOTPTextviewListener() {
        edtxOne.addTextChangedListener(OtpTextWatcher())
        edtxTwo.addTextChangedListener(OtpTextWatcher())
        edtxThree.addTextChangedListener(OtpTextWatcher())
        edtxFour.addTextChangedListener(OtpTextWatcher())

        edtxOne.setOnKeyListener(OtpTextBackWatcher())
        edtxTwo.setOnKeyListener(OtpTextBackWatcher())
        edtxThree.setOnKeyListener(OtpTextBackWatcher())
        edtxFour.setOnKeyListener(OtpTextBackWatcher())


    }

    private inner class OtpTextWatcher : TextWatcher {


        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            DebuggableLogI("Newtaxi", "Textchange")
            if (edtxOne.isFocused) {
                if (edtxOne.text.toString().length > 0)
                //size as per your requirement
                {
                    edtxTwo.requestFocus()
                    edtxTwo.setSelectAllOnFocus(true)
                    //one.setBackgroundResource(R.drawable.d_buttomboardermobilenumber);
                }
            } else if (edtxTwo.isFocused) {
                if (edtxTwo.text.toString().length > 0)
                //size as per your requirement
                {
                    edtxThree.requestFocus()
                    edtxThree.setSelectAllOnFocus(true)
                    //two.setBackgroundResource(R.drawable.d_buttomboardermobilenumber);
                } else {
                    edtxOne.requestFocus()
                    edtxOne.setSelectAllOnFocus(true)
                    // edtxOne.setSelection(1);
                }
            } else if (edtxThree.isFocused) {
                if (edtxThree.text.toString().length > 0)
                //size as per your requirement
                {
                    edtxFour.requestFocus()
                    edtxFour.setSelectAllOnFocus(true)
                    //three.setBackgroundResource(R.drawable.d_buttomboardermobilenumber);
                } else {
                    edtxTwo.requestFocus()
                    edtxTwo.setSelectAllOnFocus(true)
                    //edtxTwo.setSelection(1);
                }
            } else if (edtxFour.isFocused) {
                if (edtxFour.text.toString().length == 0) {
                    edtxThree.requestFocus()
                }
            }

            if (edtxOne.text.toString().trim { it <= ' ' }.length > 0 && edtxTwo.text.toString().trim { it <= ' ' }.length > 0 && edtxThree.text.toString().trim { it <= ' ' }.length > 0 && edtxFour.text.toString().trim { it <= ' ' }.length > 0) {
                otp = edtxOne.text.toString().trim { it <= ' ' } + edtxTwo.text.toString().trim { it <= ' ' } + edtxThree.text.toString().trim { it <= ' ' } + edtxFour.text.toString().trim { it <= ' ' }
                //cvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext,R.color.light_blue_button_color))
                cvvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_navy))
            } else {
                otp = ""
                //cvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext,R.color.quantum_grey400))
                cvvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_navy_disable))
            }
            tvOTPErrorMessage.visibility = View.GONE
        }

        override fun afterTextChanged(editable: Editable) {
            DebuggableLogI("Newtaxi", "Textchange")

        }
    }

    private inner class OtpTextBackWatcher : View.OnKeyListener {

        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {

            DebuggableLogD("keycode", keyCode.toString() + "")
            DebuggableLogD("keyEvent", event.toString())
            if (keyCode == KeyEvent.KEYCODE_DEL && isDeletable) {
                when (v.id) {
                    R.id.one -> {
                        edtxOne.text.clear()
                    }
                    R.id.two -> {
                        edtxTwo.text.clear()
                        edtxOne.requestFocus()
                        edtxOne.setSelectAllOnFocus(true)
                    }
                    R.id.three -> {
                        edtxThree.text.clear()
                        edtxTwo.requestFocus()
                        edtxTwo.setSelectAllOnFocus(true)
                    }
                    R.id.four -> {
                        edtxFour.text.clear()
                        edtxThree.requestFocus()
                        edtxThree.setSelectAllOnFocus(true)
                    }//edtxThree.setSelection(1);
                }
                countdownTimerForOTPBackpress()
                return true
            } else {
                return false
            }

        }
    }

    fun countdownTimerForOTPBackpress() {
        isDeletable = false
        if (backPressCounter != null) backPressCounter!!.cancel()
        backPressCounter = object : CountDownTimer(100, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                //tvResendOTPCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            override fun onFinish() {
                isDeletable = true
            }
        }.start()
    }


    fun startAnimation() {
        /* View redLayout = findViewById(R.id.tv_mobile_heading);
        ViewGroup parent = findViewById(R.id.cl_phone_number_input);

        Transition transition = new Slide(Gravity.START);
        transition.setDuration(600);
        transition.addTarget(R.id.tv_mobile_heading);

        TransitionManager.beginDelayedTransition(parent, transition);
        redLayout.setVisibility(false ? View.VISIBLE : View.GONE);*/

        val RightSwipe = AnimationUtils.loadAnimation(this, R.anim.ub__slide_out_left)
        mobileNumberHeading.startAnimation(RightSwipe)
        RightSwipe.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                mobileNumberHeading.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
    }

    fun callSendOTPAPI() {
        showProgressBarAndHideArrow(true)
        apiService.numbervalidation(edtxPhoneNumber.text.toString(), ccp.selectedCountryNameCode.replace("\\+".toRegex(), ""), sessionManager.type!!, sessionManager.languageCode!!, isForForgotPassword.toString()).enqueue(RequestCallback(this))
    }

    fun showProgressBarAndHideArrow(status: Boolean) {
        if (status) {
            pbNumberVerification.visibility = View.VISIBLE
            imgvArrow.visibility = View.GONE
            //For Otp view
            ph_number_verification.visibility = View.VISIBLE
            img_next.visibility = View.GONE
        } else {
            pbNumberVerification.visibility = View.GONE
            imgvArrow.visibility = View.VISIBLE
            //For Otp view
            ph_number_verification.visibility = View.GONE
            img_next.visibility = View.VISIBLE
        }
    }

/*
    private fun startCallingFacebookKit() {
        val intent = Intent(this, AccountKitActivity::class.java)
        val configurationBuilder = AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN)
        // PhoneNumber phoneNumber = new PhoneNumber(sessionManager.getTemporaryCountryCode(), sessionManager.getTemporaryPhonenumber());
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build())
        startActivityForResult(intent, FACEBOOK_ACCOUNTKIT_REQUEST_CODE)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }*/
    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == FACEBOOK_ACCOUNTKIT_REQUEST_CODE) { // confirm that this response matches your request
             val loginResult = data!!.getParcelableExtra<AccountKitLoginResult>(AccountKitLoginResult.RESULT_KEY)
             if (loginResult.error != null || loginResult.wasCancelled()) {
                 //showErrorMessageAndCloseActivity();
                 finish()

             } else {
                 getPhoneNumber()
             }
         }
     }*/

    /*   fun getPhoneNumber() {
           AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
               override fun onSuccessResponse(account: Account) {
                   val phoneNumbers: String
                   val countryCode: String
                   val phoneNumberWihtoutPlusSign: String
                  // val temporaryPhoneNumber: String

                   // Get phone number
                   val phoneNumber = account.phoneNumber
                   phoneNumbers = phoneNumber.phoneNumber.toString()
                   phoneNumberWihtoutPlusSign = phoneNumbers.replace("+", "")
                   countryCode = phoneNumber.countryCode
                   callPhoneNumberValidationAPI(phoneNumberWihtoutPlusSign, countryCode)
               }

               override fun onError(error: AccountKitError) {
                   showErrorMessageAndCloseActivity()

                   // Handle Error
               }
           })
       }*/

    /*public void phoneNumberChangedErrorMessage() {
        //commonMethods.showMessage(this, dialog, getString(R.string.InvalidMobileNumber));
        Toast.makeText(this, getString(R.string.InvalidMobileNumber), Toast.LENGTH_SHORT).show();
    }
*/

    /*void facebookAccountKitNumberVerificationSuccess() {
        setResult(ApiSessionAppConstants.FACEBOOK_ACCOUNT_KIT_VERIFACATION_SUCCESS);
        finish();
    }

    void facebookAccountKitNumberVerificationFailure() {
        setResult(ApiSessionAppConstants.FACEBOOK_ACCOUNT_KIT_VERIFACATION_FAILURE);
        finish();
    }*/

    // api call
    internal fun callPhoneNumberValidationAPI(facebookVerifiedPhoneNumber: String, facebookVerifiedCountryCode: String) {
        this.facebookVerifiedCountryCode = facebookVerifiedCountryCode
        this.facebookVerifiedPhoneNumber = facebookVerifiedPhoneNumber
        commonMethods.showProgressDialog(this)
        apiService.numbervalidation(facebookVerifiedPhoneNumber, facebookVerifiedCountryCode, sessionManager.type!!, sessionManager.languageCode!!, "").enqueue(RequestCallback(this))
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        showProgressBarAndHideArrow(false)
        /*Intent returnIntent = new Intent();
        returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_KEY, facebookVerifiedPhoneNumber);
        returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_PHONE_NUMBER_COUNTRY_CODE_KEY, facebookVerifiedCountryCode);

        NumberValidationModel numberValidationModel = gson.fromJson(jsonResp.getStrResponse(), NumberValidationModel.class);

        if (numberValidationModel.getStatusCode().equals(CommonKeys.NUMBER_VALIDATION_API_RESULT_OLD_USER)) {

            returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY, numberValidationModel.getStatusMessage());
            setResult(CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_OLD_USER, returnIntent);
            finish();

        } else if (numberValidationModel.getStatusCode().equals(CommonKeys.NUMBER_VALIDATION_API_RESULT_NEW_USER)) {

            returnIntent.putExtra(FACEBOOK_ACCOUNT_KIT_MESSAGE_KEY, numberValidationModel.getStatusMessage());
            setResult(CommonKeys.FACEBOOK_ACCOUNT_KIT_RESULT_NEW_USER, returnIntent);
            finish();
        } else {
            CommonMethods.DebuggableLogI(numberValidationModel.getStatusCode(), numberValidationModel.getStatusMessage());
        }*/
        //val numberValidationModel = gson.fromJson(jsonResp.strResponse, NumberValidationModel::class.java)
        /*

        if (numberValidationModel.getStatusCode().equals(CommonKeys.NUMBER_VALIDATION_API_RESULT_OLD_USER)) {

        } else if (numberValidationModel.getStatusCode().equals(CommonKeys.NUMBER_VALIDATION_API_RESULT_NEW_USER)) {
        }
*/
        when (jsonResp.requestCode) {
            REQ_OTP_VERIFIACTION -> {
                if (jsonResp.isSuccess) {
                    redirectToActivity()
                } else {
                    commonMethods.hideProgressDialog()
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                    //showSettingsAlert(jsonResp.statusMsg)
                }
            }
            else -> {
                if (jsonResp.isSuccess) {
                    clearEditText()
                    receivedOTPFromServer = commonMethods.getJsonValue(jsonResp.strResponse, "otp", String::class.java) as String
                    cvvNext.setCardBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_navy_disable))
                    showOTPfield()
                    /**
                     * For Test Propose
                     */
                    if (!receivedOTPFromServer.isNullOrEmpty() && getString(R.string.otp_enable).equals("true",true)) {
                        setOtp(receivedOTPFromServer)
                    } else {
                        edtxOne.requestFocus()
                    }

                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.hideProgressDialog()
                    //commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                    showSettingsAlert(jsonResp.statusMsg)
                }
            }
        }


    }

    /**
     * Setting otp on the Fields
     * @param otp
     */
    private fun setOtp(otp: String?) {
        if (otp != null) {
            edtxOne.setText(otp.substring(0, 1))
            edtxTwo.setText(otp.substring(1, 2))
            edtxThree.setText(otp.substring(2, 3))
            edtxFour.setText(otp.substring(3, 4))
        }
    }

    private fun clearEditText() {
        edtxOne.setText("")
        edtxTwo.setText("")
        edtxThree.setText("")
        edtxFour.setText("")
    }

    fun showSettingsAlert(statusMsg: String) {
        val alertDialog = AlertDialog.Builder(
                this)
        //alertDialog.setTitle(statusMsg);
        alertDialog.setMessage(statusMsg)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(resources.getString(R.string.ok)
        ) { _, _ -> finish() }

        alertDialog.show()
    }


    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        showErrorMessageAndCloseActivity()
    }

    private fun showErrorMessageAndCloseActivity() {
        CommonMethods.showServerInternalErrorMessage(this)
        finish()
    }

    override fun onBackPressed() {
        if (isPhoneNumberLayoutIsVisible) {
            super.onBackPressed()
        } else {
            showPhoneNumberField()
        }
    }

    companion object {


        fun openFacebookAccountKitActivity(activity: Activity) {
            val facebookIntent = Intent(activity, FacebookAccountKitActivity::class.java)
            facebookIntent.putExtra("usableType", 0)
            activity.startActivityForResult(facebookIntent, CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT)
        }

        fun openFacebookAccountKitActivity(activity: Activity, type: Int) {
            val facebookIntent = Intent(activity, FacebookAccountKitActivity::class.java)
            facebookIntent.putExtra("usableType", type)
            activity.startActivityForResult(facebookIntent, CommonKeys.ACTIVITY_REQUEST_CODE_START_FACEBOOK_ACCOUNT_KIT)
        }
    }
}


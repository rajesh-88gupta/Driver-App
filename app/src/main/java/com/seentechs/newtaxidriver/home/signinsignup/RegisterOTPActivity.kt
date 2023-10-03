package com.seentechs.newtaxidriver.home.signinsignup

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage signinsignup model
 * @category RegisterOTPActivity
 * @author Seen Technologies
 *
 */

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.*
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.REQ_NUM_VALID
import com.seentechs.newtaxidriver.common.util.Enums.REQ_REG
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.ForgetPwdModel
import com.seentechs.newtaxidriver.home.datamodel.LoginDetails
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import org.json.JSONArray
import org.json.JSONException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
                RegisterOTPActivity
Its used to get the register mobile number OTP detail function
*************************************************************** */
class RegisterOTPActivity : CommonActivity(), ServiceListener {


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

    @BindView(R.id.resend)
    lateinit var resend: TextView
    @BindView(R.id.codetext)
    lateinit var codetext: TextView
    @BindView(R.id.resend_timer)
    lateinit var resend_timer: TextView
    lateinit var countDownTimer: CountDownTimer
    lateinit var otp: String
    lateinit var phoneno: String
    var resetpassword: Boolean = false
    var checkresend = false
    @BindView(R.id.progressBar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.backArrow)
    lateinit var backArrow: ImageView
    @BindView(R.id.one)
    lateinit var one: EditText
    @BindView(R.id.two)
    lateinit var two: EditText
    @BindView(R.id.three)
    lateinit var three: EditText
    @BindView(R.id.four)
    lateinit var four: EditText
    protected var isInternetAvailable: Boolean = false
    /*
     *   Message broadcast receiver
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

        }
    }

    @OnClick(R.id.next)
    operator fun next() {
        reigsterOtp()
    }

    @OnClick(R.id.resend)
    fun resend() {
        isInternetAvailable = commonMethods.isOnline(this)
        checkresend = true
        if (isInternetAvailable) {
            numberValidation()
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.Interneterror))
        }
    }

    @OnClick(R.id.back)
    fun back() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registerotp)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)

        isInternetAvailable = commonMethods.isOnline(this)
        val x = intent
        otp = x.getStringExtra("otp").toString()

       // if (otp != null) {
            one.setText(otp.substring(0, 1))
            two.setText(otp.substring(1, 2))
            three.setText(otp.substring(2, 3))
            four.setText(otp.substring(3, 4))
       // }


        resetpassword = x.getBooleanExtra("resetpassword", false)
        phoneno = sessionManager.phoneNumber!!
        if (sessionManager.getisEdit()) {
            phoneno = x.getStringExtra("phone_number").toString()
        }


        //TO set edit text select all
        one.setSelectAllOnFocus(true)
        two.setSelectAllOnFocus(true)
        three.setSelectAllOnFocus(true)
        four.setSelectAllOnFocus(true)

        //Setting the edit text cursor at Start
        val position = one.selectionStart
        val position2 = two.selectionStart
        val position3 = three.selectionStart
        val position4 = four.selectionStart
        one.setSelection(position)
        two.setSelection(position2)
        three.setSelection(position3)
        four.setSelection(position4)

        two.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {

                two.requestFocus()
            }
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL
                && two.text.toString().length == 0
            ) {

                one.requestFocus()
                one.text.clear()
            }
            false
        }

        three.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {

                three.requestFocus()
            }
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL
                && three.text.toString().length == 0
            ) {

                two.requestFocus()
                two.text.clear()
            }
            false
        }
        four.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                four.requestFocus()
            }
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL
                && four.text.toString().length == 0
            ) {
                three.requestFocus()
                three.text.clear()
            }
            false
        }


        var strmi: Int
        val str1 = getString(R.string.enter4digit) + " " + phoneno
        val str = str1.length
        strmi = phoneno.length
        val start = str1.length - strmi

        /*
         *  Countdown for resent OTP button enable
         */
        countdown()

        val str3 = SpannableStringBuilder(str1)
        str3.setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            start,
            str,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        codetext.text = str3

        /*
         *   Text watcher for OTP fields
         */
        one.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // TODO Auto-generated method stub
                if (one.text.toString().length == 1)
                //size as per your requirement
                {
                    two.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub

            }

            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
                /*  if (one.getText().toString().length()==1){
                    two.requestFocus();
                }*/

            }

        })

        two.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // TODO Auto-generated method stub
                if (two.text.toString().length == 1)
                //size as per your requirement
                {
                    three.requestFocus()

                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub

            }

            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub

                /* if (two.getText().toString().length() == 0)     //size as per your requirement
                {
                    one.requestFocus();
                }*/
            }

        })

        three.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // TODO Auto-generated method stub
                if (three.text.toString().length == 1)
                //size as per your requirement
                {
                    four.requestFocus()
                    //three.setBackgroundResource(R.drawable.d_buttomboardermobilenumber);
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub

            }

            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub

                /* if (three.getText().toString().length() == 0)     //size as per your requirement
                {
                    two.requestFocus();
                }*/
            }

        })


        four.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // TODO Auto-generated method stub
                if (three.text.toString().length == 1)
                //size as per your requirement
                {
                    four.requestFocus()
                    //four.setBackgroundResource(R.drawable.d_buttomboardermobilenumber);
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub

            }

            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub

                /* if (four.getText().toString().length() == 0)     //size as per your requirement
                {
                    three.requestFocus();
                }*/
            }

        })
    }

    private fun numberValidation() {

        isInternetAvailable = commonMethods.isOnline(this)
        checkresend = true

        if (isInternetAvailable) {

            if (!resetpassword) {

                apiService.numberValidation(
                    sessionManager.type!!,
                    sessionManager.phoneNumber!!,
                    sessionManager.countryCode!!,
                    "",
                    sessionManager.languageCode!!
                ).enqueue(RequestCallback(REQ_NUM_VALID, this))
            } else {

                apiService.numberValidation(
                    sessionManager.type!!,
                    sessionManager.phoneNumber!!,
                    sessionManager.countryCode!!,
                    "1",
                    sessionManager.languageCode!!
                ).enqueue(RequestCallback(REQ_NUM_VALID, this))

            }
        } else {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.Interneterror))
        }


    }

    fun reigsterOtp() {

        isInternetAvailable = commonMethods.isOnline(this)
        val emtytextone = one.text.toString().trim { it <= ' ' }
        val emtytexttwo = two.text.toString().trim { it <= ' ' }
        val emtytextthree = three.text.toString().trim { it <= ' ' }
        val emtytextfour = four.text.toString().trim { it <= ' ' }

        if (emtytextone.isEmpty() || emtytexttwo.isEmpty() || emtytextthree.isEmpty() || emtytextfour.isEmpty()) {
            commonMethods.showMessage(this, dialog, resources.getString(R.string.otp_emty))
        } else {

            val otpcode =
                one.text.toString() + "" + two.text.toString() + "" + three.text.toString() + "" + four.text.toString() + ""
            if (otp == otpcode) {
                if (sessionManager.getisEdit()) {
                    sessionManager.password = intent.getStringExtra("phone_number")

                    onBackPressed()
                } else {
                    if (!resetpassword) {
                        checkresend = false

                        var firstnamestr = sessionManager.firstName!!
                        var lastnamestr = sessionManager.lastName!!
                        var citystr = sessionManager.city!!
                        var passwordstr = sessionManager.password!!


                        try {
                            firstnamestr = URLEncoder.encode(firstnamestr, "UTF-8")
                            lastnamestr = URLEncoder.encode(lastnamestr, "UTF-8")
                            passwordstr = URLEncoder.encode(passwordstr, "UTF-8")
                            citystr = URLEncoder.encode(citystr, "UTF-8")
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }

                        if (isInternetAvailable) {
                            commonMethods.showProgressDialog(this)
                            apiService.registerOtp(
                                sessionManager.type!!,
                                sessionManager.phoneNumber!!,
                                sessionManager.countryCode!!,
                                sessionManager.getemail()!!,
                                firstnamestr,
                                lastnamestr,
                                passwordstr,
                                citystr,
                                sessionManager.deviceId!!,
                                sessionManager.deviceType!!,
                                sessionManager.languageCode!!,
                                null,
                            "email","","").enqueue(RequestCallback(REQ_REG, this))
                        } else {
                            commonMethods.showMessage(
                                this,
                                dialog,
                                resources.getString(R.string.Interneterror)
                            )
                        }
                    } else {
                        // below code moved to MobileActivity, due to Facebook Kit implementation
                        /*Intent intent = new Intent(getApplicationContext(), ResetPassword.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left);*/
                    }
                }
            } else {

                commonMethods.showMessage(
                    this,
                    dialog,
                    resources.getString(R.string.otp_mismatch)
                )
            }

        }


    }


    private fun onSuccessForgetPwd(jsonResp: JsonResponse) {

        progressBar.visibility = View.GONE
        backArrow.visibility = View.VISIBLE

        val forgetPwdModel = gson.fromJson(jsonResp.strResponse, ForgetPwdModel::class.java)
        if (forgetPwdModel != null) {
            if (forgetPwdModel.statusCode.matches("1".toRegex())) {
                progressBar.visibility = View.GONE
                backArrow.visibility = View.VISIBLE
                if (checkresend) {
                    countdown()
                    otp = forgetPwdModel.otp


                  //  if (otp != null) {
                        one.setText(otp.substring(0, 1))
                        two.setText(otp.substring(1, 2))
                        three.setText(otp.substring(2, 3))
                        four.setText(otp.substring(3, 4))
                  //  }


                }
            } else {
                if (forgetPwdModel.statusMessage == "Message sending Failed,please try again..") {
                    countdown()
                    otp = forgetPwdModel.otp

                  //  if (otp != null) {
                        one.setText(otp.substring(0, 1))
                        two.setText(otp.substring(1, 2))
                        three.setText(otp.substring(2, 3))
                        four.setText(otp.substring(3, 4))
                  //  }


                } else {
                    commonMethods.showMessage(this, dialog, forgetPwdModel.statusMessage)
                }
                progressBar.visibility = View.GONE
                backArrow.visibility = View.VISIBLE
            }
        }

    }


    private fun onSuccessRegisterPwd(jsonResp: JsonResponse) {

        val signInUpResultModel = gson.fromJson(jsonResp.strResponse, LoginDetails::class.java)

        if (signInUpResultModel != null) {

            try {

                if (signInUpResultModel.statusCode.matches("1".toRegex())) {
                    progressBar.visibility = View.GONE
                    backArrow.visibility = View.VISIBLE

                    val carDeailsModel = gson.toJson(signInUpResultModel.carDetailModel)

                    val cardetails = JSONArray(carDeailsModel)

                    val carType = StringBuilder()
                    carType.append(resources.getString(R.string.vehicle_type)).append(",")
                    for (i in 0 until cardetails.length()) {
                        val cartype = cardetails.getJSONObject(i)
                        carType.append(cartype.getString("car_name")).append(",")
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        sessionManager.currencySymbol =
                            Html.fromHtml(signInUpResultModel.currencySymbol,Html.FROM_HTML_MODE_LEGACY).toString()
                    }else{
                        Html.fromHtml(signInUpResultModel.currencySymbol).toString()
                    }
                    sessionManager.currencyCode = signInUpResultModel.currencyCode
                    sessionManager.paypalEmail = signInUpResultModel.payoutId
                    sessionManager.driverSignupStatus = signInUpResultModel.userStatus
                    sessionManager.carType = carType.toString()
                    sessionManager.setAcesssToken(signInUpResultModel.token)
                    sessionManager.isRegister = true
                    commonMethods.hideProgressDialog()
                    startMainActivity()


                } else {
                    commonMethods.showMessage(this, dialog, signInUpResultModel.statusMessage)

                }
                progressBar.visibility = View.GONE
                backArrow.visibility = View.VISIBLE


            } catch (e: JSONException) {
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    public override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("otp"))
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
    /*
         *   Resend notification count down started
         */

    fun countdown() {
        resend.isEnabled = false
        resend_timer.visibility = View.VISIBLE
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val f = NumberFormat.getNumberInstance(Locale.US)
                val formatter = f as DecimalFormat
                formatter.applyPattern("00")
                resend_timer.text = "00:" + formatter.format(millisUntilFinished / 1000)
            }

            override fun onFinish() {
                resend.isEnabled = true
                resend_timer.text = "00:00"
                resend_timer.visibility = View.INVISIBLE
                //resend_timer.setText("done!");
            }
        }.start()
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }


        when (jsonResp.requestCode) {

            REQ_REG -> if (jsonResp.isSuccess) {

                onSuccessRegisterPwd(jsonResp)

            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {

                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)

            }
            REQ_NUM_VALID -> if (jsonResp.isSuccess) {

                onSuccessForgetPwd(jsonResp)

            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {

                onSuccessForgetPwd(jsonResp)

            }
            else -> {
            }
        }


    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        progressBar.visibility = View.GONE
        backArrow.visibility = View.VISIBLE
    }


}
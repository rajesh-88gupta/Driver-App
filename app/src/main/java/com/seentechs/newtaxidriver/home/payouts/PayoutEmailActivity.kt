package com.seentechs.newtaxidriver.home.payouts

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.ConnectionDetector
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.activity_payout_email.*
import java.util.*
import javax.inject.Inject

/* ************************************************************
                   Payout Get Email Page
Get  PayPal email address for payout option
*************************************************************** */
class PayoutEmailActivity : CommonActivity(), View.OnClickListener, ServiceListener {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var sessionManager: SessionManager

    lateinit var payout_submit: Button
    lateinit var payoutemail_edittext: EditText
    lateinit var payoutemail: String
    lateinit var address1: String
    lateinit var address2: String
    lateinit var city: String
    lateinit var state: String
    lateinit var pincode: String
    lateinit var email: String
    lateinit var country: String

    @Inject
    lateinit var customDialog: CustomDialog

    lateinit var dialog: AlertDialog

    protected var isInternetAvailable: Boolean = false

    // Check network is avalable or not
    val networkState: ConnectionDetector
        get() = ConnectionDetector(this)

    @OnClick(R.id.arrow)
    fun onBackPress() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_email)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)

        dialog = commonMethods.getAlertDialog(this)
        val x = intent
        commonMethods.setheaderText(resources.getString(R.string.payout), common_header)
        address1 = x.getStringExtra("address1").toString()
        address2 = x.getStringExtra("address2").toString()
        city = x.getStringExtra("city").toString()
        state = x.getStringExtra("state").toString()
        country = x.getStringExtra("country").toString()
        pincode = x.getStringExtra("postal_code").toString()
        email = x.getStringExtra("email").toString()
        payout_submit = findViewById<View>(R.id.payout_submit) as Button
        payout_submit.setOnClickListener(this)

        payoutemail_edittext = findViewById<View>(R.id.payoutemail_edittext) as EditText
        payoutemail_edittext.setText(email)
        disableSubmitButton()
        payoutemail_edittext.addTextChangedListener(EmailTextWatcher(payoutemail_edittext))


        enableDisableSubmitBtn()

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            commonMethods.snackBar(jsonResp.statusMsg, "", false, 2, payoutemail_edittext, payoutemail_edittext, resources, this)
            finish()
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.snackBar(jsonResp.statusMsg, "", false, 2, payoutemail_edittext, payoutemail_edittext, resources, this)
    }

    // Validate email field
    private inner class EmailTextWatcher(private val view: View) : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun afterTextChanged(editable: Editable) {
            when (view.id) {

            }
            enableDisableSubmitBtn()


        }
    }

    private fun enableDisableSubmitBtn() {
        if (validateEmail()) {
            enableSubmitButton()
        } else {
            disableSubmitButton()
        }
    }

    private fun validateEmail(): Boolean {

        val email = payoutemail_edittext.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || !isValidEmail(email)) {

            return false
        }
        return true
    }

    private fun enableSubmitButton() {
        payout_submit.isEnabled = true
        payout_submit.setBackgroundResource(R.drawable.app_curve_button_navy)
    }

    private fun disableSubmitButton() {
        payout_submit.isEnabled = false
        payout_submit.setBackgroundResource(R.drawable.app_curve_button_navy_disable)
    }


    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.payout_submit -> {
                payoutemail = payoutemail_edittext.text.toString()

                hideSoftKeyboard()  // Hide keyboard
                isInternetAvailable = networkState.isConnectingToInternet
                if (isInternetAvailable) {
                    val imageObject = HashMap<String, String>()

                    imageObject["address1"] = address1
                    imageObject["address2"] = address2
                    imageObject["token"] = sessionManager.accessToken!!
                    imageObject["email"] = payoutemail
                    imageObject["city"] = city
                    imageObject["state"] = state
                    imageObject["country"] = country
                    imageObject["postal_code"] = pincode
                    imageObject["payout_method"] = "paypal"
                    apiService.UpdatePayoutDetails(imageObject).enqueue(RequestCallback(this))
                    // updateProf()
                    // updateProfile(imageObject); // Call update API to update Email and address details
                } else {
                    snackBar(resources.getString(R.string.Interneterror))
                    commonMethods.snackBar(resources.getString(R.string.Interneterror), "", false, 2, payoutemail_edittext, payoutemail_edittext, resources, this)
                }
            }
        }
    }

    // Hide keyboard function
    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    // Show network error and exception
    fun snackBar(statusmessage: String) {
        // Create the Snackbar
        val snackbar = Snackbar.make(payout_submit, "", Snackbar.LENGTH_LONG)
        // Get the Snackbar's layout view
        val layout = snackbar.view as Snackbar.SnackbarLayout
        // Hide the text
        val textView = layout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE

        // Inflate our custom view
        val snackView = layoutInflater.inflate(R.layout.snackbar, null)
        // Configure the view

        val snackbar_background = snackView.findViewById<View>(R.id.snackbar) as RelativeLayout
        snackbar_background.setBackgroundColor(ContextCompat.getColor(this, R.color.app_background))

        val button = snackView.findViewById<View>(R.id.snackbar_action) as Button
        button.visibility = View.GONE
        button.text = resources.getString(R.string.showpassword)
        button.setTextColor(ContextCompat.getColor(this, R.color.app_background))
        button.setOnClickListener { }

        val textViewTop = snackView.findViewById<View>(R.id.snackbar_text) as TextView
        if (isInternetAvailable) {
            textViewTop.text = statusmessage
        } else {
            textViewTop.text = resources.getString(R.string.Interneterror)
        }

        // textViewTop.setTextSize(getResources().getDimension(R.dimen.midb));
        textViewTop.setTextColor(ContextCompat.getColor(this, R.color.white))

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.app_background))
        snackbar.show()

    }
}

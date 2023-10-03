package com.seentechs.newtaxidriver.home.fragments.payment

/**
 * @package com.newtaxi
 * @subpackage views.main.paytoadmin
 * @category PayToAdminActivity
 * @author Seen Technologies
 *
 */

/*import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentConfirmation*/
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.Selection
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.PayPal
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.braintreepayments.api.interfaces.BraintreeErrorListener
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener
import com.braintreepayments.api.models.PayPalRequest
import com.braintreepayments.api.models.PaymentMethodNonce
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.model.StripeIntent
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.custompalette.FontTextView
import com.seentechs.newtaxidriver.common.helper.Constants.PayToAdmin
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.REQ_CURRENCY_CONVERT
import com.seentechs.newtaxidriver.common.util.Enums.REQ_PAY_TO_ADMIN
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.common.views.PaymentWebViewActivity
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.activity_pay_to_admin.*
import kotlinx.android.synthetic.main.add_wallet_amount.*
import org.json.JSONObject
import java.util.*
import javax.inject.Inject


/*************************************************************
 * PayToAdminActivity
 * Its used to pay Owe amount to admin by driver
 */
class PayToAdminActivity : CommonActivity(), View.OnClickListener, ServiceListener, PaymentMethodNonceCreatedListener,BraintreeErrorListener {

    lateinit @Inject
    var apiService: ApiService

    lateinit @Inject
    var commonMethods: CommonMethods

    lateinit @Inject
    var customDialog: CustomDialog

    lateinit @Inject
    var sessionManager: SessionManager

    lateinit @BindView(R.id.tvOweAmount)
    var tvOweAmount: TextView

    lateinit @BindView(R.id.button)
    var btnAddAmount: Button
    private var mBraintreeFragment: BraintreeFragment? = null

    lateinit var dialogInject: DialogInject
    lateinit private var dialog: BottomSheetDialog
    lateinit private var alertDialog: AlertDialog
    private var walletAddedAmount: String = ""
    private var isReferalAmount = "0"
    private val REQUEST_CODE = 101
    //private lateinit var  config: PayPalConfiguration

    val REQUEST_CODE_PAYMENT = 1

    lateinit var defaultEditTextString: String
    lateinit var paymentMethod: RelativeLayout

    @OnClick(R.id.back)
    fun onBack() {
        onBackPressed()
    }

    /**
     * Add AMount to pay to Admin
     */
    @OnClick(R.id.button)
    fun addToWallet() {
        showAddWallet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_to_admin)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this)

        /**Commmon Header Text View */
        commonMethods.setheaderText(resources.getString(R.string.pay_to), common_header)
        commonMethods.setButtonText(resources.getString(R.string.pay_to),common_button)

        alertDialog = commonMethods.getAlertDialog(this)
        enableViews()

        dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        dialog.setContentView(R.layout.add_wallet_amount)
        paymentMethod = dialog.findViewById(R.id.rltPaymentAddMethod)!!
        dialogInject = DialogInject()
        // 5. We bind the elements of the included layouts.
        ButterKnife.bind(dialogInject, dialog)


        defaultEditTextString = sessionManager.currencySymbol + " " + ""

    }

    private var mLastClickTime: Long = 0
    /**
     * Dialog to add Amount in wallet
     */
    private fun showAddWallet() {
        if (sessionManager.payementModeWebView!!) {
            paymentMethod.visibility = View.GONE
        } else {
            paymentMethod.visibility = View.VISIBLE
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        dialogInject.setMethod()
        dialog.show()
    }

    /**
     * Pay owe amount to admin
     */
    fun addStripeToWallet(amount: String, referApply: String) {
        commonMethods.showProgressDialog(this)
        apiService.payToAdmin(amount, referApply, sessionManager.accessToken!!)
                .enqueue(RequestCallback(REQ_PAY_TO_ADMIN, this))
    }


    /**
     * Paypal payment completed to update transcation id
     */
    private fun paymentCompleted(payKey: String) {
        commonMethods.showProgressDialog(this)
        apiService.payToAdmin(payToAdmin(payKey)).enqueue(RequestCallback(REQ_PAY_TO_ADMIN, this))

    }

    /**
     * Pay owe amount to admin
     */
    fun addStripeToWallet(confirmStripeParams: LinkedHashMap<String, String>) {
        commonMethods.showProgressDialog(this)
        apiService.payToAdmin(confirmStripeParams).enqueue(RequestCallback(REQ_PAY_TO_ADMIN, this))
    }


    /**
     * To get nounce from brain tree paypal
     */
    override fun onPaymentMethodNonceCreated(paymentMethodNonce: PaymentMethodNonce) {


        commonMethods.hideProgressDialog()
        val nonce: String = paymentMethodNonce.nonce
        paymentCompleted(nonce)
    }

    /**
     * Success On API
     */
    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()

        if (dialog.isShowing) {
            dialog.dismiss()
        }
        val statuscode = commonMethods.getJsonValue(jsonResp.strResponse, "status_code", String::class.java) as String
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, alertDialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_CURRENCY_CONVERT -> if (jsonResp.isSuccess) {
                commonMethods.hideProgressDialog()
                val amount = commonMethods.getJsonValue(jsonResp.strResponse, "amount", String::class.java) as String
                if (sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_BRAINTREE)) {
                    sessionManager.brainTreeClientToken = commonMethods.getJsonValue(jsonResp.strResponse, "braintree_clientToken", String::class.java) as String
                    callBrainTreeUI(amount)
                } else if (sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_PAYPAL)) {

                    commonMethods.showProgressDialog(this)
                    val mAuthorization: String = commonMethods.getJsonValue(jsonResp.strResponse, "braintree_clientToken", String::class.java) as String

                    try {
                        mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization)
                    } catch (e: Exception) {

                    }

                    mBraintreeFragment?.addListener(this)

                    val currency_code = commonMethods.getJsonValue(jsonResp.strResponse, "currency_code", String::class.java) as String

                    setupBraintreeAndStartExpressCheckout(amount, currency_code)
                    //payment(amount, currency_code,sessionManager.paypal_mode,sessionManager.paypal_app_id);
                }

            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, alertDialog, jsonResp.statusMsg)
            }
            REQ_PAY_TO_ADMIN -> if (jsonResp.isSuccess) {
                dialog.dismiss()
                onSuccessGiveOweAmount(jsonResp)
            } else if (statuscode.equals("2")) {
                commonMethods.showProgressDialog(this@PayToAdminActivity)
                commonMethods.getClientSecret(jsonResp, this)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.hideProgressDialog()
                commonMethods.showMessage(this, alertDialog, jsonResp.statusMsg)
            }
        }

    }


    fun setupBraintreeAndStartExpressCheckout(amount: String, currencycode: String) {
        val request = PayPalRequest(amount)
                    .currencyCode(currencycode)
                .intent(PayPalRequest.INTENT_SALE)
        PayPal.requestOneTimePayment(mBraintreeFragment, request)
    }

    /**
     * PayPal configuration called
     */
    /*fun loadPayPal(paypal_mode:Int,paypal_app_id:String) {


        //paypal_app_id = paypal_key;
        //paypal_app_id=getResources().getString(R.string.paypal_client_id);

        if (0==paypal_mode) {
            CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
        } else {
            CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
        }

        CONFIG_CLIENT_ID = paypal_app_id;


        config = PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(CONFIG_CLIENT_ID)
                // The following are only used in PayPalFuturePaymentActivity.
                .merchantName(getResources().getString(R.string.pay_to))
                .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));

        config.acceptCreditCards(false);

        intent =  Intent(this, PayPalService::class.java)

        var servicerRunningCheck:Boolean = isMyServiceRunning(PayPalService::class.java)


        System.out.println("Service Running check : "+servicerRunningCheck);
        if(servicerRunningCheck){
            if(intent!=null){
                stopService(intent);
            }
        }
        servicerRunningCheck = isMyServiceRunning(PayPalService::class.java)

        System.out.println("Service Running check twice : "+servicerRunningCheck);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
    }*/
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    /**
     * Paypal payment
     */
    /*fun payment(amount:String,currencycode:String,paypal_mode:Int, paypal_app_id:String) {
        *//*
        * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
        * Change PAYMENT_INTENT_SALE to
        *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
        *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
        *     later via calls from your server.
        *
        * Also, to include additional payment details and an item list, see getStuffToBuy() below.
        *//*
        loadPayPal(paypal_mode,paypal_app_id);
        val thingToBuy: PayPalPayment = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE, amount, currencycode);
        //PayPalPayment thingToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
        *//*
         * See getStuffToBuy(..) for examples of some available payment options.
         *//*

        //addAppProvidedShippingAddress(thingToBuy);   /// Add shipping address
        //enableShippingAddressRetrieval(thingToBuy,true);  //  Enable retrieval of shipping addresses from buyer's PayPal account

        intent =  Intent(this, com.paypal.android.sdk.payments.PaymentActivity::class.java);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }*/

    /* private fun getThingToBuy( paymentIntent:String,  amount:String,  currencycode:String): PayPalPayment {

         return  PayPalPayment( BigDecimal(amount), currencycode, getResources().getString(R.string.payment),
                 paymentIntent);
     }*/

    private fun callBrainTreeUI(payableWalletAmount: String) {
        val dropInRequest = DropInRequest()
                .requestThreeDSecureVerification(true)
                .threeDSecureRequest(commonMethods.threeDSecureRequest(payableWalletAmount))
                .clientToken(sessionManager.brainTreeClientToken)
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE)
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }


    override fun onResume() {
        super.onResume()
        println("paymentmethod ${sessionManager.paymentMethod}")
        commonMethods.hideProgressDialog()
        if (sessionManager.paymentMethodkey.equals("") || sessionManager.paymentMethodkey.isEmpty()) {
            dialogInject.tvPaymentMethod.text = resources.getString(R.string.Add_payment_type)
            dialogInject.ivPaymentImage.visibility = View.GONE
            dialogInject.tvChange.text = resources.getString(R.string.choose)
        } else {
            if (sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_CARD)) {
                if (sessionManager.cardValue != "") {
                    dialogInject.tvPaymentMethod.text = "•••• " + sessionManager.cardValue
                    dialogInject.ivPaymentImage.setImageResource(R.drawable.card)
                } else {
                    dialogInject.tvPaymentMethod.text = sessionManager.paymentMethod
                    Picasso.get().load(sessionManager.paymentMethodImage).into(dialogInject.ivPaymentImage)
                    CommonMethods.DebuggableLogE("payment Image", sessionManager.paymentMethodImage)
                }
            } else {
                Picasso.get().load(sessionManager.paymentMethodImage).into(dialogInject.ivPaymentImage)
                dialogInject.tvPaymentMethod.text = sessionManager.paymentMethod
                CommonMethods.DebuggableLogE("payment Image", sessionManager.paymentMethodImage)
            }
            dialogInject.ivPaymentImage.visibility = View.VISIBLE
            dialogInject.tvChange.text = resources.getString(R.string.change)

        }
        /*  else if(sessionManager.paymentMethod.equals(CommonKeys.PAYMENT_PAYPAL))
          {
              dialogInject.tvPaymentMethod.text=resources.getString(R.string.paypal)
              dialogInject.ivPaymentImage.setImageResource(R.drawable.paypal)
          }else if(sessionManager.paymentMethod.equals(CommonKeys.PAYMENT_CARD))
          {
              dialogInject.tvPaymentMethod.text= "•••• ${sessionManager.cardValue}"
              dialogInject.ivPaymentImage.setImageResource(R.drawable.card)
          }
  */
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mBraintreeFragment?.removeListener(this)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYMENT) {
            /*if (resultCode == Activity.RESULT_OK) {
                val confirm : PaymentConfirmation?=
                        data?.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        DebuggableLogI(TAG, confirm.toJSONObject().toString(4));
                        DebuggableLogI(TAG, confirm.toJSONObject().getJSONObject("response").get("id").toString());
                        DebuggableLogI(TAG, confirm.getPayment().toJSONObject().toString(4));
                        */
            /**
             *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
             * or consent completion.
             * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
             * for more details.
             *
             * For sample mobile backend interactions, see
             * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
             *//*
                        paymentCompleted(confirm.toJSONObject().getJSONObject("response").get("id").toString());
                        // displayResultText("PaymentConfirmation info received from PayPal");

                    } catch ( e: JSONException) {
                        DebuggableLogE(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                DebuggableLogI(TAG, "The user canceled.");
            } else if (resultCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
                DebuggableLogI(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }*/
        } else if (requestCode == REQUEST_CODE) {
            if (data != null) {
                val result = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                if (result != null) {
                    val nonce = result.paymentMethodNonce!!.nonce
                    addStripeToWallet(payToAdmin(nonce))
                } else {
                    Toast.makeText(this, resources.getString(R.string.payment), Toast.LENGTH_LONG).show()
                }
            }
        } else if (requestCode == PayToAdmin) {
            if (data != null) {
                try {
                    commonMethods.hideProgressDialog()
                    val jsonResponse = data.extras?.getString("response")
                    val response = JSONObject(jsonResponse)
                    if (response != null) {
                        val statusCode = response.getString("status_code")
                        val statusMessage = response.getString("status_message")
                        if (statusCode.equals("1", true)) {
                            val oweAmount = response.getString("owe_amount")
                            val remainingReferralAmount = response.getString("referral_amount")
                            sessionManager.oweAmount = oweAmount
                            sessionManager.driverReferral = remainingReferralAmount
                            enableViews()
                        }
                        commonMethods.showMessage(this, alertDialog, statusMessage)
                    }
                } catch (e: Exception) {
                    commonMethods.showMessage(this, alertDialog, e.message!!)
                    e.printStackTrace()
                }

            }
        } else {
            if (data != null) {
                commonMethods.hideProgressDialog()
                commonMethods.stripeInstance()!!.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
                    override fun onError(e: Exception) {
                        e.printStackTrace()
                    }

                    override fun onSuccess(result: PaymentIntentResult) {
                        val paymentIntent = result.intent
                        if (paymentIntent.status == StripeIntent.Status.Succeeded) {

                            addStripeToWallet(payToAdmin(paymentIntent.id!!))
                        } else {
                            commonMethods.showMessage(this@PayToAdminActivity, alertDialog, "Failed")
                        }
                    }

                })
            }
        }
    }

    private fun payToAdmin(paykey: String): LinkedHashMap<String, String> {
        val payToAdminParams = LinkedHashMap<String, String>()
        payToAdminParams["amount"] = walletAddedAmount
        payToAdminParams["pay_key"] = paykey
        payToAdminParams["payment_type"] = sessionManager.paymentMethodkey
        payToAdminParams["applied_referral_amount"] = isReferalAmount
        payToAdminParams["token"] = sessionManager.accessToken!!
        return payToAdminParams
    }

    /**
     * Result After Success
     */
    private fun onSuccessGiveOweAmount(jsonResp: JsonResponse) {
        val oweAmount = commonMethods.getJsonValue(jsonResp.strResponse, "owe_amount", String::class.java) as String
        val remainingReferralAmount = commonMethods.getJsonValue(jsonResp.strResponse, "referral_amount", String::class.java) as String
        sessionManager.oweAmount = oweAmount
        sessionManager.driverReferral = remainingReferralAmount
        dialogInject.edtOweAmount.setText("")
        enableViews()
        commonMethods.hideProgressDialog()
        commonMethods.showMessage(this, alertDialog, jsonResp.statusMsg)

    }

    private fun enableViews() {
        if ("0".equals(resources.getString(R.string.layout_direction), ignoreCase = true))
            tvOweAmount.text = sessionManager.currencySymbol + " " + sessionManager.oweAmount
        else
            tvOweAmount.text = sessionManager.oweAmount + " " + sessionManager.currencySymbol

        if (sessionManager.oweAmount == "0" || sessionManager.oweAmount == "0.00") {
            btnAddAmount.isEnabled = false
            btnAddAmount.background = ContextCompat.getDrawable(this, R.drawable.app_curve_button_navy_disable)
        } else {
            btnAddAmount.isEnabled = true
            btnAddAmount.background = ContextCompat.getDrawable(this, R.drawable.app_curve_button_navy)
        }
    }

    /**
     * To show error or information
     */
    fun snackBar(message: String, buttonmessage: String, buttonvisible: Boolean, duration: Int) {
        // Create the Snackbar
        val snackbar: Snackbar
        val snackbar_background: RelativeLayout
        val snack_button: TextView
        val snack_message: TextView

        // Snack bar visible duration
        if (duration == 1)
            snackbar = Snackbar.make(tvOweAmount, "", Snackbar.LENGTH_INDEFINITE)
        else if (duration == 2)
            snackbar = Snackbar.make(tvOweAmount, "", Snackbar.LENGTH_LONG)
        else
            snackbar = Snackbar.make(tvOweAmount, "", Snackbar.LENGTH_SHORT)

        // Get the Snackbar's layout view
        val layout = snackbar.view as Snackbar.SnackbarLayout
        // Hide the text
        val textView = layout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.visibility = View.INVISIBLE

        // Inflate our custom view
        val snackView = layoutInflater.inflate(R.layout.snackbar, null)
        // Configure the view

        snackbar_background = snackView.findViewById(R.id.snackbar)
        snack_button = snackView.findViewById(R.id.snack_button)
        snack_message = snackView.findViewById(R.id.snackbar_text)

        snackbar_background.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_black)) // Background Color

        if (buttonvisible)
        // set Right side button visible or gone
            snack_button.visibility = View.VISIBLE
        else
            snack_button.visibility = View.GONE

        snack_button.setTextColor(ContextCompat.getColor(applicationContext, R.color.white)) // set right side button text color
        snack_button.text = buttonmessage // set right side button text
        snack_button.setOnClickListener {
            snackbar.dismiss()
        }

        snack_message.setTextColor(ContextCompat.getColor(applicationContext, R.color.white)) // set left side main message text color
        snack_message.text = message  // set left side main message text

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_black))
        snackbar.show()
    }

    override fun onClick(v: View) {

    }


    /**
     * Annotation  using ButterKnife lib to Injection and OnClick for Accept or pause dialog
     */
    inner class DialogInject {
        lateinit @BindView(R.id.rl_referral_amt)
        var rlReferralAmt: RelativeLayout

        lateinit @BindView(R.id.tv_referral_amt)
        var tvReferralAmt: FontTextView

        lateinit @BindView(R.id.cb_referral)
        var cbReferral: CheckBox

        lateinit @BindView(R.id.edtOweAmount)
        var edtOweAmount: EditText

        lateinit @BindView(R.id.tvChange)
        var tvChange: TextView

        lateinit @BindView(R.id.tvPaymentMethod)
        var tvPaymentMethod: TextView

        lateinit @BindView(R.id.ivPaymentImage)
        var ivPaymentImage: ImageView

        lateinit @BindView(R.id.btnAddMoney)
        var btnAddMoney: Button


        @OnClick(R.id.tvChange)
        fun changePaymentMethod() {
            val change = Intent(applicationContext, PaymentActivity::class.java)
            startActivity(change)
        }

        @OnClick(R.id.btnAddMoney)
        fun addMoney() {
            walletAddedAmount = edtOweAmount.text.substring(2)

            edtOweAmount.setSelection(edtOweAmount.text.length)
            if (TextUtils.isEmpty(sessionManager.oweAmount) && java.lang.Float.valueOf(sessionManager.oweAmount!!) <= 0) {

                snackBar(getString(R.string.enter_pendg_amount_empty), "", false, 3)

            } else if (!TextUtils.isEmpty(sessionManager.driverReferral) && cbReferral.isChecked) {

                var enterAmount: Float = 0f
                if (!TextUtils.isEmpty(walletAddedAmount)) {
                    enterAmount = java.lang.Float.valueOf(edtOweAmount.text.toString())
                    println("Entered Amount" + enterAmount)
                }
                var oweAmount: Float = 0f
                if (!TextUtils.isEmpty(sessionManager.oweAmount!!)) {
                    oweAmount = java.lang.Float.valueOf(sessionManager.oweAmount!!)
                    println("Owe Amount" + oweAmount)
                }
                var driReferral: Float? = 0f
                if (!TextUtils.isEmpty(sessionManager.driverReferral!!)) {
                    driReferral = java.lang.Float.valueOf(sessionManager.driverReferral!!)
                    println("Driver Referral Amount" + driReferral!!)
                }

                if (enterAmount > 0 && enterAmount + driReferral!! > oweAmount) {
                    dialog.dismiss()
                    edtOweAmount.setText("")
                    commonMethods.showMessage(this@PayToAdminActivity, alertDialog, getString(R.string.entered_amt_msg))
                } else {
                    if (TextUtils.isEmpty(sessionManager.paymentMethodkey) && !sessionManager.payementModeWebView!!) {
                        snackBar(getString(R.string.choose_payment_type), "", false, 3)
                    } else {
                        if (enterAmount > 0) {
                            isReferalAmount = "1"
                            if (sessionManager.payementModeWebView!!) {
                                val webview = Intent(this@PayToAdminActivity, PaymentWebViewActivity::class.java)
                                webview.putExtra("payableAmount", walletAddedAmount)
                                webview.putExtra("isReferralAmount", isReferalAmount)
                                startActivityForResult(webview, PayToAdmin)
                            } else {
                                if (sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_CARD, ignoreCase = true)) {
                                    addStripeToWallet(payToAdmin(""))
                                } else {
                                    currencyConversion(walletAddedAmount)
                                }
                            }
                        } else {
                            addStripeToWallet("0", "1")
                        }
                    }
                }

            } else {
                if (!TextUtils.isEmpty(walletAddedAmount) && java.lang.Float.valueOf(walletAddedAmount) > 0) {
                    val enterAmount = java.lang.Float.valueOf(walletAddedAmount)
                    val oweAmount = java.lang.Float.valueOf(sessionManager.oweAmount!!)

                    if (enterAmount > oweAmount) {
                        dialog.dismiss()
                        edtOweAmount.setText("")
                        commonMethods.showMessage(this@PayToAdminActivity, alertDialog, getString(R.string.entered_amt_msg))
                    } else {
                        if (TextUtils.isEmpty(sessionManager.paymentMethodkey) && !sessionManager.payementModeWebView!!) {
                            snackBar(getString(R.string.choose_payment_type), "", false, 3)
                        } else {
                            if (sessionManager.payementModeWebView!!) {
                                val webview = Intent(this@PayToAdminActivity, PaymentWebViewActivity::class.java)
                                webview.putExtra("payableAmount", walletAddedAmount)
                                webview.putExtra("isReferralAmount", isReferalAmount)
                                startActivityForResult(webview, PayToAdmin)
                            } else {
                                if (sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_PAYPAL) || sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_BRAINTREE)) {
                                    currencyConversion(walletAddedAmount)
                                } else if (sessionManager.paymentMethodkey.equals(CommonKeys.PAYMENT_CARD, ignoreCase = true)) {
                                    addStripeToWallet(payToAdmin(""))
                                }
                            }
                        }

                    }

                } else {
                    snackBar(getString(R.string.enter_amount_empty), "", false, 3)
                }
            }
            hideKeyboard(this@PayToAdminActivity)
            dialog.dismiss()
            //            else if (sessionManager.getWalletCard() == 1) {
            //                    //Stripe
            //                    Float enterAmount = 0f;
            //                    if (!TextUtils.isEmpty(edtOweAmount.getText().toString())) {
            //                        enterAmount = Float.valueOf(edtOweAmount.getText().toString());
            //                        System.out.println("Entered Amount" + enterAmount);
            //                    }
            //                    Float oweAmount = 0f;
            //                    if (!TextUtils.isEmpty(sessionManager.getOweAmount())) {
            //                        oweAmount = Float.valueOf(sessionManager.getOweAmount());
            //                        System.out.println("Owe Amount" + oweAmount);
            //                    }
            //                    Float driReferral = 0f;
            //                    if (!TextUtils.isEmpty(sessionManager.getDriverReferral())) {
            //                        driReferral = Float.valueOf(sessionManager.getDriverReferral());
            //                        System.out.println("Driver Referral Amount" + driReferral);
            //                    }
            //
            //                    if (enterAmount > 0 && enterAmount + driReferral > oweAmount) {
            //                        commonMethods.showMessage(PayToAdminActivity.this, alertDialog, getString(R.string.entered_amt_msg));
            //                    } /*else {*/
            //                        if (enterAmount > 0) {
            //                            addStripeToWallet(edtOweAmount.getText().toString(), "1");
            //                        } else {
            //                            addStripeToWallet("0", "1");
            //                        //}
            //                    }
            //
            //            }
            //            else if(!TextUtils.isEmpty(sessionManager.getDriverReferral())&& cbReferral.isChecked())
            //            {
            //                System.out.println("API condition Worked"+sessionManager.getDriverReferral());
            //                Float enterAmount = 0f;
            //                if (!TextUtils.isEmpty(edtOweAmount.getText().toString())) {
            //                    enterAmount = Float.valueOf(edtOweAmount.getText().toString());
            //                    System.out.println("Entered Amount" + enterAmount);
            //                }
            //                if (enterAmount > 0 && (Float.valueOf(sessionManager.getDriverReferral()) < Float.valueOf(sessionManager.getOweAmount()))) {
            //                    addStripeToWallet(edtOweAmount.getText().toString(), "1");
            //                } else {
            //                    addStripeToWallet("0", "1");
            //                }
            //            }
            //            else if (!TextUtils.isEmpty(edtOweAmount.getText().toString()) &&
            //                        Float.valueOf(edtOweAmount.getText().toString()) > 0) {
            //
            //                if (sessionManager.getWalletCard() == 1) {
            //                    //Stripe
            //                    Float enterAmount = Float.valueOf(edtOweAmount.getText().toString());
            //                    Float oweAmount = Float.valueOf(sessionManager.getOweAmount());
            //
            //                    if (enterAmount > oweAmount) {
            ////                        snackBar(getString(R.string.entered_amt_msg), "", false, 3);
            //                        commonMethods.showMessage(PayToAdminActivity.this, alertDialog, getString(R.string.entered_amt_msg));
            //                    } else {
            //                        addStripeToWallet(edtOweAmount.getText().toString(), "0");
            //
            //                    }
            //                }
            //            }
            //            else {
            //                snackBar(getString(R.string.enter_amount_empty), "", false, 3);
            //            }

        }

        fun hideKeyboard(activity: Activity) {
            val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view: View? = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }


        fun setMethod() {
            if ("1".equals(getString(R.string.layout_direction), ignoreCase = true)) {
                edtOweAmount.gravity = Gravity.END
                edtOweAmount.layoutDirection = View.LAYOUT_DIRECTION_LTR

            }
            edtOweAmount.setText(defaultEditTextString)
            Selection.setSelection(edtOweAmount.text, edtOweAmount.text.length)
            //edtOweAmount.setSelection(edtOweAmount.text.length)
            btnAddMoney.isEnabled = true
            btnAddMoney.background = ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_navy)
            edtOweAmount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!s.toString().startsWith(defaultEditTextString)) {
                        edtOweAmount.setText(defaultEditTextString)
                        Selection.setSelection(edtOweAmount.text, edtOweAmount.text.length)

                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
            /*if (sessionManager.getWalletCard() == 0) {
                ivPaymentImage.setImageDrawable(getResources().getDrawable(R.drawable.credit_card));
                tvPaymentMethod.setText(getResources().getString(R.string.add_card));
                edtOweAmount.setEnabled(false);
                btnAddMoney.setEnabled(false);
                btnAddMoney.setBackgroundColor(getResources().getColor(R.color.black_alpha_20));
            } else if (sessionManager.getWalletCard() == 1) {
                //  if(!cbReferral.isChecked() || )
                setCardImage(sessionManager.getCardBrand());
                edtOweAmount.setEnabled(true);
                tvPaymentMethod.setText("•••• " + sessionManager.getCardValue());
                btnAddMoney.setEnabled(true);
                btnAddMoney.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_black_full));
            }*/

            if (!TextUtils.isEmpty(sessionManager.driverReferral) && java.lang.Double.parseDouble(sessionManager.driverReferral!!) > 0) {
                rlReferralAmt.visibility = View.VISIBLE
                tvReferralAmt.text = sessionManager.currencySymbol + sessionManager.driverReferral
                //                if (!TextUtils.isEmpty(sessionManager.getOweAmount())) {
                //
                //                    Double oweAmt = Double.parseDouble(sessionManager.getOweAmount());
                //                    Double driRefer = Double.parseDouble(sessionManager.getDriverReferral());
                //
                //                    if (oweAmt > driRefer) {
                //                        tvReferralAmt.setText(sessionManager.getDriverReferral());
                //                    } else {
                //                        tvReferralAmt.setText(String.valueOf(oweAmt));
                //                    }
                //                }
            } else {
                rlReferralAmt.visibility = View.GONE
            }

            cbReferral.setOnCheckedChangeListener { _, isChecked ->
                /*if (sessionManager.getWalletCard()==1){
                        edtOweAmount.setEnabled(true);
                        btnAddMoney.setEnabled(true);
                        btnAddMoney.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_black_full));
                    }else {
                        edtOweAmount.setEnabled(false);
                        btnAddMoney.setEnabled(false);
                        btnAddMoney.setBackgroundColor(getResources().getColor(R.color.black_alpha_20));
                    }*/
                if (isChecked) {
                    btnAddMoney.isEnabled = true
                    btnAddMoney.background = (ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_navy))
                    if (!TextUtils.isEmpty(sessionManager.oweAmount!!) && !TextUtils.isEmpty(sessionManager.driverReferral!!)) {

                        val oweAmt = java.lang.Double.parseDouble(sessionManager.oweAmount!!)
                        val driRefer = java.lang.Double.parseDouble(sessionManager.driverReferral!!)

                        tvReferralAmt.text = sessionManager.currencySymbol + sessionManager.driverReferral

                        if (oweAmt > driRefer) {
                            val difference = oweAmt - driRefer
                            edtOweAmount.setText(difference.toString())
                            //                                tvReferralAmt.setText(sessionManager.getDriverReferral());

                        } else {
                            edtOweAmount.setText("0")
                            edtOweAmount.isEnabled = false
                            btnAddMoney.isEnabled = true
                            btnAddMoney.background = (ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_navy))
                            //                                tvReferralAmt.setText(String.valueOf(oweAmt));
                        }
                    }
                } else {
                    edtOweAmount.isEnabled = true
                    edtOweAmount.setText(sessionManager.oweAmount)
                    tvReferralAmt.text = sessionManager.currencySymbol + sessionManager.driverReferral
                    //                        if (!TextUtils.isEmpty(sessionManager.getOweAmount())
                    //                                && !TextUtils.isEmpty(sessionManager.getDriverReferral())) {
                    //
                    //                            Double oweAmt = Double.parseDouble(sessionManager.getOweAmount());
                    //                            Double driRefer = Double.parseDouble(sessionManager.getDriverReferral());
                    //
                    //                            if (oweAmt > driRefer) {
                    //                                tvReferralAmt.setText(sessionManager.getDriverReferral());
                    //                            } else {
                    //                                tvReferralAmt.setText(String.valueOf(oweAmt));
                    //                            }
                    //                        } else {
                    //                            if (!TextUtils.isEmpty(sessionManager.getDriverReferral())) {
                    //                                tvReferralAmt.setText(sessionManager.getDriverReferral());
                    //                            }
                    //                        }
                }
            }
        }

        /**
         * Convert Currency For BrainTree
         * @param payableWalletAmount
         */
        protected fun currencyConversion(payableWalletAmount: String) {
            commonMethods.showProgressDialog(this@PayToAdminActivity)
            //commonMethods.showProgressDialogPaypal(this@PayToAdminActivity, customDialog,resources.getString(R.string.few_seconds))
            apiService.currencyConversion(payableWalletAmount, sessionManager.accessToken!!, sessionManager.paymentMethodkey.toLowerCase()).enqueue(RequestCallback(REQ_CURRENCY_CONVERT, this@PayToAdminActivity))
        }

        /**
         * Set card images
         *
         * @param brand Type of a card
         */
        fun setCardImage(brand: String) {
            if (brand.contains("Visa")) {
                ivPaymentImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.card_visa))
            } else if (brand.contains("MasterCard")) {
                ivPaymentImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.card_master))
            } else if (brand.contains("Discover")) {
                ivPaymentImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.card_discover))
            } else if (brand.contains("Amex")) {
                ivPaymentImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.card_amex))
            } else if (brand.contains("JCP")) {
                ivPaymentImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.card_jcp))
            } else if (brand.contains("Diner")) {
                ivPaymentImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.card_diner))
            } else if (brand.contains("Union")) {
                ivPaymentImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.card_unionpay))
            } else {
                ivPaymentImage.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.card_basic))
            }
        }


    }

    companion object {

        private val TAG = "paymentExample"
        private var CONFIG_ENVIRONMENT: String? = null

        // note that these credentials will differ between live & sandbox environments.
        private var CONFIG_CLIENT_ID: String? = null
    }

    override fun onError(p0: java.lang.Exception?) {
        println("localizedMessage ${p0?.localizedMessage}")
        p0?.printStackTrace()

    }
}
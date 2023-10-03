package com.seentechs.newtaxidriver.common.views

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.*
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonKeys.PAY_TO_ADMIN
import com.seentechs.newtaxidriver.common.util.CommonMethods
import kotlinx.android.synthetic.main.activity_payment_web_view.*
import kotlinx.android.synthetic.main.app_common_header.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject

class PaymentWebViewActivity : CommonActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var customDialog: CustomDialog

    lateinit var progressDialog: ProgressDialog

    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_web_view)
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.payment), common_header)
        val payableAdminAmount = intent.getStringExtra("payableAmount")
        val isReferralApplied = intent.getStringExtra("isReferralAmount")
        back.setOnClickListener {
            onBackPressed()
        }
        setProgress()

        if (!progressDialog.isShowing) {
            //progressDialog.show()
        }
        commonMethods.showProgressDialog(this)

        payment_wv.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (!progressDialog.isShowing) {
                    //progressDialog.show()
                }
                commonMethods.showProgressDialog(this@PaymentWebViewActivity)
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                commonMethods.hideProgressDialog()
                payment_wv.loadUrl("javascript:android.showHTML(document.getElementById('data').innerHTML);")
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                commonMethods.hideProgressDialog()
            }
        }

        payment_wv.settings.javaScriptEnabled = true
        payment_wv.addJavascriptInterface(MyJavaScriptInterface(this), "android")

        /*val url = getString(R.string.apiBaseUrl) + CommonKeys.WEB_PAY_TO_ADMIN
        val postData = url + CommonKeys.WEB_PAY_TO_ADMIN + "amount=" + payableAdminAmount + "&pay_for=" +  + "&payment_type=" + sessionManager.paymentMethod?.toLowerCase() + "&applied_referral_amount=" + isReferralApplied + "&token=" + sessionManager.accessToken
        payment_wv.loadUrl(postData)*/

        val url = getString(R.string.apiBaseUrl) + CommonKeys.WEB_PAY_TO_ADMIN
        val postData = "amount=" + URLEncoder.encode(payableAdminAmount, "UTF-8") + "&pay_for=" + URLEncoder.encode(PAY_TO_ADMIN, "UTF-8") + "&applied_referral_amount=" + URLEncoder.encode(isReferralApplied, "UTF-8") +"&token=" + URLEncoder.encode(sessionManager.accessToken, "UTF-8")
        payment_wv.postUrl(url, postData.toByteArray())
    }

    private fun setProgress() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage(resources.getString(R.string.loading))
        progressDialog.setCancelable(false)
    }

    // Open previous opened link from history on webview when back button pressed
    // Detect when the back button is pressed
    override fun onBackPressed() {
        if (payment_wv.canGoBack()) {
            payment_wv.goBack()
        } else {
            super.onBackPressed()
        }
    }

    inner class MyJavaScriptInterface(private var ctx: Context) {

        @JavascriptInterface
        fun showHTML(html: String) {
            println("HTML$html")
            var response: JSONObject? = null
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
            commonMethods.hideProgressDialog()
            try {
                response = JSONObject(html)
                redirect(response.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (t: Throwable) {
                Log.e("My App", "Could not parse malformed JSON: \"$response\"")
            }
        }
    }

    private fun redirect(htmlResponse: String) {
        val intent = Intent()
        intent.putExtra("response", htmlResponse)
        setResult(RESULT_OK, intent)
        finish()
    }
}
package com.seentechs.newtaxidriver.home.payouts

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage Profile
 * @category PayoutAddressDetailsActivity
 * @author Seen Technologies
 *
 */

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.util.userchoice.UserChoice
import com.seentechs.newtaxidriver.common.util.userchoice.UserChoiceSuccessResponse
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.datamodel.PayoutData
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.payouts.adapter.PayoutCountryListAdapter
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.CountryModel
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.CountryResult
import kotlinx.android.synthetic.main.activity_payout_address_details.*
import kotlinx.android.synthetic.main.location_search.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

/* ************************************************************
                   Payout get user address detail Page
Get  address details for payout option
*************************************************************** */
var isClicked:Boolean = true

class PayoutAddressDetailsActivity : CommonActivity(), View.OnClickListener, ServiceListener, UserChoiceSuccessResponse {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var userChoice: UserChoice

    lateinit var payoutaddress_title: RelativeLayout
    lateinit var payout_next: Button

    lateinit var payoutaddress_street: EditText
    lateinit var payoutaddress_apt: EditText
    lateinit var payoutaddress_city: EditText
    lateinit var payoutaddress_state: EditText
    lateinit var payoutaddress_pin: EditText
    lateinit var payoutaddress_country: EditText
    lateinit var address_street: String
    lateinit var address_apt: String
    lateinit var address_city: String
    lateinit var address_state: String
    lateinit var address_pin: String
    lateinit var address_country: String
    var userid: String? = null

    private val handler: Handler = Handler()
    private var mLastClickTime: Long = 0

    lateinit var payoutData: PayoutData
    lateinit var recyclerView1: RecyclerView
    lateinit var countryListAdapter: PayoutCountryListAdapter
    lateinit var countryResult: CountryResult
    var countryModels = ArrayList<CountryModel>()

    @OnClick(R.id.arrow)
    fun onBackPress() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_address_details)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.payout), common_header)
        payoutData = intent.getSerializableExtra("payoutData") as PayoutData
        countryModels.clear()

        payoutaddress_street = findViewById<View>(R.id.payoutaddress_street) as EditText
        payoutaddress_apt = findViewById<View>(R.id.payoutaddress_apt) as EditText
        payoutaddress_city = findViewById<View>(R.id.payoutaddress_city) as EditText
        payoutaddress_pin = findViewById<View>(R.id.payoutaddress_pin) as EditText
        payoutaddress_state = findViewById<View>(R.id.payoutaddress_state) as EditText
        payoutaddress_country = findViewById<View>(R.id.payoutaddress_country) as EditText

        payoutaddress_street.setText(payoutData.address1)
        payoutaddress_apt.setText(payoutData.address2)
        payoutaddress_city.setText(payoutData.city)
        payoutaddress_state.setText(payoutData.state)
        payoutaddress_country.setText(payoutData.country)
        payoutaddress_pin.setText(payoutData.postal_code)



        payout_next = findViewById<View>(R.id.payout_next) as Button

        payoutaddress_country.setOnClickListener {
            if (isClicked) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                isClicked = false
                clickCountry()
            }
        }

        payout_next.setOnClickListener(this)
        getCountryListFromAPI()
    }

    private fun clickCountry(){
        if (countryModels.isNotEmpty())
            getCountryList()
        else
            getCountryListFromAPI()
    }

    private fun getCountryListFromAPI(){
        if (commonMethods.isOnline(this)) {
            countryListSearch()
        } else {
            commonMethods.snackBar(resources.getString(R.string.network_failure), "", false, 2, payoutaddress_street, address, resources, this)
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.payout_next -> {


                address_street = payoutaddress_street.text.toString()
                address_apt = payoutaddress_apt.text.toString()
                address_city = payoutaddress_city.text.toString()
                address_state = payoutaddress_state.text.toString()
                address_pin = payoutaddress_pin.text.toString()
                address_country = payoutaddress_country.text.toString()

                address_street = address_street.replace("^\\s+|\\s+$".toRegex(), "")
                address_apt = address_apt.replace("^\\s+|\\s+$".toRegex(), "")
                address_city = address_city.replace("^\\s+|\\s+$".toRegex(), "")
                address_state = address_state.replace("^\\s+|\\s+$".toRegex(), "")
                address_pin = address_pin.replace("^\\s+|\\s+$".toRegex(), "")
                address_country = address_country.replace("^\\s+|\\s+$".toRegex(), "")


                payoutaddress_street.setText(address_street)
                payoutaddress_apt.setText(address_apt)
                payoutaddress_city.setText(address_city)
                payoutaddress_state.setText(address_state)
                payoutaddress_pin.setText(address_pin)
                payoutaddress_country.setText(address_country)


                try {
                    address_street = URLEncoder.encode(address_street, "UTF-8")
                    address_apt = URLEncoder.encode(address_apt, "UTF-8")
                    address_city = URLEncoder.encode(address_city, "UTF-8")
                    address_state = URLEncoder.encode(address_state, "UTF-8")
                    address_pin = URLEncoder.encode(address_pin, "UTF-8")
                    address_country = URLEncoder.encode(address_country, "UTF-8")
                    address_street = address_street.replace("+", " ")
                    address_apt = address_apt.replace("+", " ")
                    address_city = address_city.replace("+", " ")
                    address_state = address_state.replace("+", " ")
                    address_pin = address_pin.replace("+", " ")
                    address_country = address_country.replace("+", " ")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }

                if (address_street == "") {
                    commonMethods.snackBar(resources.getString(R.string.error_address), "", false, 2, payoutaddress_street, payoutaddress_street, resources, this)
                } else if (address_city == "") {
                    commonMethods.snackBar(resources.getString(R.string.error_city), "", false, 2, payoutaddress_street, payoutaddress_street, resources, this)
                } else if (address_state == "") {
                    commonMethods.snackBar(resources.getString(R.string.error_msg_state), "", false, 2, payoutaddress_street, payoutaddress_street, resources, this)
                } else if (address_pin == "") {
                    commonMethods.snackBar(resources.getString(R.string.error_zip_code), "", false, 2, payoutaddress_street, payoutaddress_street, resources, this)
                } else if (address_country == "") {
                    commonMethods.snackBar(resources.getString(R.string.error_country), "", false, 2, payoutaddress_street, payoutaddress_street, resources, this)
                } else {
                    // Call payout email page and pass datas
                    val x = Intent(applicationContext, PayoutEmailActivity::class.java)
                    x.putExtra("address1", address_street)
                    x.putExtra("address2", address_apt)
                    x.putExtra("city", address_city)
                    x.putExtra("state", address_state)
                    x.putExtra("postal_code", address_pin)
                    x.putExtra("email", payoutData.paypal_email)
                    x.putExtra("country", sessionManager.payPalCountryCode)
                    startActivity(x)
                    finish()
                }

            }
        }
    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            onSuccessC(jsonResp)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }

    // Get country list from API
    private fun countryListSearch() {
        commonMethods.showProgressDialog(this)
        apiService.getCountryList(sessionManager.accessToken!!).enqueue(RequestCallback(this))
    }

    private fun onSuccessC(jsonResp: JsonResponse) {
        countryResult = gson.fromJson(jsonResp.strResponse, CountryResult::class.java)
        val countryModel = countryResult.countryList
        countryModels.clear()
        countryModels.addAll(countryModel)
        commonMethods.hideProgressDialog()
        //getCountryList()
    }

    private fun getCountryList() {
        userChoice.getCountryListForPayouts(this, countryModels, Enums.USER_CHOICE_COUNTRY, this)
    }

    companion object {
        lateinit var alertDialogStores: android.app.AlertDialog
    }

    override fun onSuccessUserSelected(type: String?, userChoiceData: String?, userChoiceCode: String?) {
        payoutaddress_country.setText(userChoiceData)

    }

}

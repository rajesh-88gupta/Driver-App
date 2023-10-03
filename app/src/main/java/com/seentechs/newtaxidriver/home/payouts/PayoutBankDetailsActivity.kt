package com.seentechs.newtaxidriver.home.payouts

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.seentechs.newtaxidriver.BuildConfig
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.ConnectionDetector
import com.seentechs.newtaxidriver.common.util.Enums.REQ_GET_STRIPE
import com.seentechs.newtaxidriver.common.util.Enums.REQ_UPLOAD_PAYOUT
import com.seentechs.newtaxidriver.common.util.Enums.USER_CHOICE_STRIPE_COUNTRY
import com.seentechs.newtaxidriver.common.util.Enums.USER_CHOICE_STRIPE_CURRENCY
import com.seentechs.newtaxidriver.common.util.Enums.USER_CHOICE_STRIPE_GENDER
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.util.userchoice.UserChoice
import com.seentechs.newtaxidriver.common.util.userchoice.UserChoiceSuccessResponse
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.Makent_model
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.StripeCountriesModel
import com.seentechs.newtaxidriver.home.payouts.payout_model_classed.StripeCountryDetails
import kotlinx.android.synthetic.main.activity_payout_bank_details.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class PayoutBankDetailsActivity : CommonActivity(), View.OnClickListener, ServiceListener, UserChoiceSuccessResponse {
    private var imageFile: File? = null
    private var mLastClickTime: Long = 0

    private var isLegalDocument: Boolean = false
    private var isAdditionalLegalDocument: Boolean = false

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var userChoice: UserChoice


    lateinit internal var recyclerView1: RecyclerView
    internal var countryList = ArrayList<Makent_model>()
    internal var currencyList = ArrayList<Makent_model>()
    internal var genderList = ArrayList<Makent_model>()
    internal var image: String = ""
    internal var imagePath = ""
    internal var imageaddtionalPath = ""
    private val SELECT_FILE = 1
    private val REQUEST_CAMERA = 0
    internal var ja: JSONArray? = null
    protected var isInternetAvailable: Boolean = false
    internal var thumbnail: Bitmap? = null
    var baseurl: String? = null
    var i = 0
    private var dialog: AlertDialog? = null
    lateinit var stripeCountriesModel: StripeCountriesModel

    lateinit var addresskana_linear: LinearLayout
    lateinit var addresskanji_linear: LinearLayout
    lateinit internal var addresskanji1names: String
    lateinit internal var addresskanji2names: String
    lateinit internal var kanjicitynames: String
    lateinit internal var kanjistatenames: String
    lateinit internal var kanjipostalcodenames: String
    lateinit internal var addresskana1names: String
    lateinit internal var addresskana2names: String
    lateinit internal var kanacitynames: String
    lateinit internal var kanastatenames: String
    lateinit internal var kanapostalcodenames: String
    lateinit internal var gendernames: String
    lateinit internal var accountownernames: String
    lateinit internal var banknames: String
    lateinit internal var branchnames: String
    lateinit internal var branch_code_names: String
    lateinit internal var bank_code_names: String
    lateinit internal var transitnonames: String
    lateinit internal var routing_number_names: String
    lateinit internal var ssn_names: String
    lateinit internal var institutenonames: String
    lateinit internal var bsbnames: String
    lateinit internal var sort_codenames: String
    lateinit internal var clearingcodenames: String
    lateinit internal var accountnumbernames: String
    lateinit internal var CountryNames: String
    lateinit internal var currencynames: String
    lateinit internal var Ibannames: String
    lateinit internal var accountholdernmaes: String
    lateinit internal var address1names: String
    lateinit internal var address2names: String
    lateinit internal var citynames: String
    lateinit internal var statenames: String
    lateinit internal var Phonenumbernames: String
    lateinit internal var postalcodenames: String
    lateinit internal var clabe: String
    lateinit var addresskana_msg: TextView
    lateinit var addresskanji_msg: TextView
    lateinit var payout_submit: Button
    lateinit var addresskanji1: EditText
    lateinit var addresskanji2: EditText
    lateinit var kanjicity: EditText
    lateinit var kanjistate: EditText
    lateinit var kanjipostalcode: EditText
    lateinit var addresskana1: EditText
    lateinit var addresskana2: EditText
    lateinit var kanacity: EditText
    lateinit var kanastate: EditText
    lateinit var kanapostalcode: EditText
    lateinit var gender: EditText
    lateinit var bank_name: EditText
    lateinit var branch_name: EditText
    lateinit var legal_doc: EditText
    lateinit var additionalLegalDoc: EditText
    lateinit var Ac_owner_name: EditText
    lateinit var ph_no: EditText
    lateinit var payoutaddress_country: EditText
    lateinit var payoutaddress_currency: EditText
    lateinit var bsb: EditText
    lateinit var Accountnumber: EditText
    lateinit var clabeNo: EditText
    lateinit var transitno: EditText
    lateinit var instituteno: EditText
    lateinit var ssn: EditText
    lateinit var routing_number: EditText
    lateinit var clearing_code: EditText
    lateinit var bank_code: EditText
    lateinit var branch_code: EditText
    lateinit var sort_code: EditText
    lateinit var Iban_no: EditText
    lateinit var Ac_holder_name: EditText
    lateinit var address1: EditText
    lateinit var address2: EditText
    lateinit var city: EditText
    lateinit var state: EditText
    lateinit var postalcode: EditText
    lateinit var countryname /*={"Austria", "Australia", "Belgium", "Canada", "Denmark", "Finland", "France", "Germany", "Hong Kong", "Ireland", "Italy", "Japan", "Luxembourg", "Netherlands", "New Zealand", "Norway", "Portugal", "Singapore", "Spain", "Sweden", "Switzerland", "United Kingdom", "United States"}*/: Array<String?>
    lateinit var currencyname /*= {"EUR", "DKK", "GBP", "NOK", "SEK", "USD", "CHF"}*/: Array<String?>
    var currencynamecanada/* ={"CAD", "USD"}*/: Array<String>? = null
    var genders = arrayOf("Male", "Female")
    private lateinit var encodedImage: String
    lateinit var countryID: Array<String?>
    lateinit var countryCode: Array<String?>
    lateinit var CountryCodeNames: String
    var currencyPosition: Int = 0

    // Check network connection
    val networkState: ConnectionDetector
        get() = ConnectionDetector(this)

    @OnClick(R.id.arrow)
    fun onBackPress() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payout_bank_details)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.payout), common_header)

        dialog = commonMethods.getAlertDialog(this)
        payoutaddress_country = findViewById<View>(R.id.payoutaddress_country) as EditText
        payoutaddress_currency = findViewById<View>(R.id.payoutaddress_currency) as EditText
        bsb = findViewById<View>(R.id.bsb) as EditText
        Accountnumber = findViewById<View>(R.id.Ac_no) as EditText
        clabeNo = findViewById<View>(R.id.clabe_no) as EditText
        bank_name = findViewById<View>(R.id.bank_name) as EditText
        payout_submit = findViewById<View>(R.id.payout_submit) as Button
        gender = findViewById<View>(R.id.gender) as EditText
        branch_name = findViewById<View>(R.id.branch_name) as EditText
        Ac_owner_name = findViewById<View>(R.id.Ac_owner_name) as EditText
        addresskana1 = findViewById<View>(R.id.addresskana1) as EditText
        addresskana2 = findViewById<View>(R.id.addresskana2) as EditText
        kanacity = findViewById<View>(R.id.kanacity) as EditText
        kanastate = findViewById<View>(R.id.kanastate) as EditText
        kanapostalcode = findViewById<View>(R.id.kanapostalcode) as EditText
        ph_no = findViewById<View>(R.id.ph_no) as EditText
        legal_doc = findViewById<View>(R.id.legal_doc) as EditText
        additionalLegalDoc = findViewById<View>(R.id.additional_legal_doc) as EditText
        transitno = findViewById<View>(R.id.transit_no) as EditText
        instituteno = findViewById<View>(R.id.institute_no) as EditText
        routing_number = findViewById<View>(R.id.routing_number) as EditText
        ssn = findViewById<View>(R.id.ssn_number) as EditText
        clearing_code = findViewById<View>(R.id.clearing_code) as EditText
        bank_code = findViewById<View>(R.id.bank_code) as EditText
        addresskana_msg = findViewById<View>(R.id.addresskana_msg) as TextView
        addresskanji_msg = findViewById<View>(R.id.addresskanji_msg) as TextView
        branch_code = findViewById<View>(R.id.branch_code) as EditText
        sort_code = findViewById<View>(R.id.sort_code) as EditText
        Iban_no = findViewById<View>(R.id.Iban_no) as EditText
        Ac_holder_name = findViewById<View>(R.id.Ac_holder_name) as EditText
        address1 = findViewById<View>(R.id.address1) as EditText
        address2 = findViewById<View>(R.id.address2) as EditText
        city = findViewById<View>(R.id.city) as EditText
        state = findViewById<View>(R.id.state) as EditText
        postalcode = findViewById<View>(R.id.postalcode) as EditText
        addresskana_linear = findViewById<View>(R.id.addresskana_linear) as LinearLayout
        addresskanji_linear = findViewById<View>(R.id.addresskanji_linear) as LinearLayout
        addresskanji1 = findViewById<View>(R.id.addresskanji1) as EditText
        addresskanji2 = findViewById<View>(R.id.addresskanji2) as EditText
        kanjicity = findViewById<View>(R.id.kanjicity) as EditText
        kanjistate = findViewById<View>(R.id.kanjistate) as EditText
        kanjipostalcode = findViewById<View>(R.id.kanjipostalcode) as EditText

        isInternetAvailable = networkState.isConnectingToInternet


        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        if (isInternetAvailable) {
            getStripeCountryList()
        } else {
            snackBar(resources.getString(R.string.Interneterror))
        }

        payoutaddress_country.setOnClickListener(this)
        payoutaddress_currency.setOnClickListener(this)
        payout_submit.setOnClickListener(this)
        legal_doc.setOnClickListener(this)
        additionalLegalDoc.setOnClickListener(this)
        gender.setOnClickListener(this)

    }

    private fun getStripeCountryList() {
        commonMethods.showProgressDialog(this)
        apiService.stripeSupportedCountry(sessionManager.accessToken!!).enqueue(RequestCallback(REQ_GET_STRIPE, this))
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.payoutaddress_country -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                userChoice.getStripeCountryCurrency(this, countryList, USER_CHOICE_STRIPE_COUNTRY, this)
                //countryList("country")
            }

            R.id.payoutaddress_currency -> {
                if (sessionManager.countryName2 != null) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    userChoice.getStripeCountryCurrency(this, currencyList, USER_CHOICE_STRIPE_CURRENCY, this)
                }
            }
            R.id.legal_doc -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                isLegalDocument = true
                pickLegalDoc()
            }
            R.id.additional_legal_doc -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                isAdditionalLegalDocument = true
                pickLegalDoc()
            }
            R.id.gender -> {
                setGender()
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                userChoice.getStripeCountryCurrency(this, genderList, USER_CHOICE_STRIPE_GENDER, this)
                //countryList("gender")
            }
            R.id.payout_submit -> {

                CountryNames = payoutaddress_country.text.toString()
                Phonenumbernames = ph_no.text.toString()
                if (CountryNames == "Austria" || CountryNames == "Belgium" || CountryNames == "Denmark" || CountryNames == "Finland" || CountryNames == "France" || CountryNames == "Germany" || CountryNames == "Ireland" || CountryNames == "Italy" || CountryNames == "Luxembourg" || CountryNames == "Norway" || CountryNames == "Portugal" || CountryNames == "Spain" || CountryNames == "Sweden" || CountryNames == "Switzerland" || CountryNames == "Belgium" || CountryNames == "Netherlands") {
                    Ibannames = Iban_no.text.toString()
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()


                    if (isInternetAvailable) {
                        val imageObject = HashMap<String, String>()

                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        imageObject["account_holder_name"] = accountholdernmaes
                        imageObject["iban"] = Ibannames
                        //imageObject["document"] = image

                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.choose_currency))
                        } else if (Ibannames == "") {
                            snackBar(resources.getString(R.string.iban_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.address_1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }


                    } else {
                        snackBar(resources.getString(R.string.no_connection))
                    }
                } else if (CountryNames == "Australia") {

                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()
                    bsbnames = bsb.text.toString()
                    accountnumbernames = Accountnumber.text.toString()

                    if (isInternetAvailable) {

                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        imageObject["payout_method"] = "stripe"
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["currency"] = currencynames
                        imageObject["account_holder_name"] = accountholdernmaes
                        imageObject["bsb"] = bsbnames
                        //imageObject["document"] = image
                        imageObject["account_number"] = accountnumbernames

                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (bsbnames == "") {
                            snackBar(resources.getString(R.string.please_enter_BSB))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }


                    } else {
                    }

                } else if (CountryNames == "Canada") {
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()
                    transitnonames = transitno.text.toString()
                    institutenonames = instituteno.text.toString()
                    accountnumbernames = Accountnumber.text.toString()


                    if (isInternetAvailable) {

                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["account_holder_name"] = accountholdernmaes
                        //imageObject["document"] = image
                        imageObject["account_number"] = accountnumbernames
                        imageObject["transit_number"] = transitnonames
                        imageObject["institution_number"] = institutenonames

                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.choose_currency))
                        } else if (transitnonames == "") {
                            snackBar(resources.getString(R.string.please_enter_transit_number))
                        } else if (institutenonames == "") {
                            snackBar(resources.getString(R.string.please_enter_institution_number))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {
                            updateProfile(imageObject)
                        }

                    } else {
                    }


                } else if (CountryNames == "New Zealand") {
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()
                    routing_number_names = routing_number.text.toString()
                    accountnumbernames = Accountnumber.text.toString()

                    if (isInternetAvailable) {

                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        imageObject["account_holder_name"] = accountholdernmaes
                        imageObject["routing_number"] = routing_number_names
                        //imageObject["document"] = image
                        imageObject["account_number"] = accountnumbernames

                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (routing_number_names == "") {
                            snackBar(resources.getString(R.string.please_enter_routing_number))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }
                    } else {
                    }


                } else if (CountryNames == "Singapore" || CountryNames == "Brazil") {
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()


                    bank_code_names = bank_code.text.toString()
                    branch_code_names = branch_code.text.toString()
                    accountnumbernames = Accountnumber.text.toString()

                    if (isInternetAvailable) {

                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["bank_code"] = bank_code_names
                        imageObject["branch_code"] = branch_code_names
                        //imageObject["document"] = image
                        imageObject["account_number"] = accountnumbernames
                        imageObject["account_holder_name"] = accountholdernmaes


                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (bank_code_names == "") {
                            snackBar(resources.getString(R.string.please_enter_bank_code))
                        } else if (branch_code_names == "") {
                            snackBar(resources.getString(R.string.please_enter_branch_code))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }


                    } else {
                    }


                } else if (CountryNames == "United Kingdom") {
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()


                    sort_codenames = sort_code.text.toString()
                    accountnumbernames = Accountnumber.text.toString()


                    if (isInternetAvailable) {
                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["sort_code"] = sort_codenames
                        //imageObject["document"] = image
                        imageObject["account_number"] = accountnumbernames
                        imageObject["account_holder_name"] = accountholdernmaes


                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (sort_codenames == "") {
                            snackBar(resources.getString(R.string.please_enter_sort_code))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }

                    } else {
                    }

                } else if (CountryNames == "Malaysia") {
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()

                    accountnumbernames = Accountnumber.text.toString()


                    if (isInternetAvailable) {
                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        imageObject["phone_number"] = Phonenumbernames
                        //imageObject["document"] = image
                        imageObject["account_number"] = accountnumbernames
                        imageObject["account_holder_name"] = accountholdernmaes


                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }

                    } else {
                    }

                } else if (CountryNames == "Mexico") {
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()
                    clabe = clabeNo.text.toString()

                    if (isInternetAvailable) {
                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        imageObject["phone_number"] = Phonenumbernames
                        imageObject["iban"] = clabe
                        //imageObject["document"] = image
                        imageObject["account_holder_name"] = accountholdernmaes


                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (clabe == "") {
                            snackBar(resources.getString(R.string.please_enter_clabe_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }

                    } else {
                    }

                } else if (CountryNames == "United States") {
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()
                    routing_number_names = routing_number.text.toString()
                    ssn_names = ssn.text.toString()
                    accountnumbernames = Accountnumber.text.toString()

                    if (isInternetAvailable) {

                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["routing_number"] = routing_number_names
                        imageObject["ssn_last_4"] = ssn_names
                        imageObject["account_holder_name"] = accountholdernmaes
                        //imageObject["document"] = image
                        imageObject["account_number"] = accountnumbernames


                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (ssn_names == "") {
                            snackBar(resources.getString(R.string.please_enter_SSN))
                        } else if (routing_number_names == "") {
                            snackBar(resources.getString(R.string.please_enter_routing_number))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }
                    } else {

                    }


                } else if (CountryNames == "Hong Kong") {
                    currencynames = payoutaddress_currency.text.toString()
                    accountholdernmaes = Ac_holder_name.text.toString()
                    address1names = address1.text.toString()
                    address2names = address2.text.toString()
                    citynames = city.text.toString()
                    statenames = state.text.toString()
                    postalcodenames = postalcode.text.toString()

                    clearingcodenames = clearing_code.text.toString()
                    branch_code_names = branch_code.text.toString()
                    accountnumbernames = Accountnumber.text.toString()

                    if (isInternetAvailable) {

                        val imageObject = HashMap<String, String>()

                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }

                        imageObject["address1"] = address1names
                        imageObject["address2"] = address2names
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = postalcodenames
                        imageObject["payout_method"] = "stripe"
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["currency"] = currencynames
                        imageObject["clearing_code"] = clearingcodenames
                        imageObject["account_holder_name"] = accountholdernmaes
                        imageObject["branch_code"] = branch_code_names
                        //imageObject["document"] = image//branch_code
                        imageObject["account_number"] = accountnumbernames

                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (clearingcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_clearing_code))
                        } else if (branch_code_names == "") {
                            snackBar(resources.getString(R.string.please_enter_branch_code))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }

                    } else {
                    }


                } else if (CountryNames == "Japan") {


                    currencynames = payoutaddress_currency.text.toString()
                    banknames = bank_name.text.toString()
                    branchnames = branch_name.text.toString()
                    bank_code_names = bank_code.text.toString()
                    branch_code_names = branch_code.text.toString()
                    accountnumbernames = Accountnumber.text.toString()
                    accountownernames = Ac_owner_name.text.toString()
                    gendernames = gender.text.toString()


                    if (gendernames == "Male") {
                        gendernames = "male"
                    } else {
                        gendernames = "female"
                    }
                    addresskana1names = addresskana1.text.toString()

                    accountholdernmaes = Ac_holder_name.text.toString()
                    addresskana2names = addresskana2.text.toString()
                    kanacitynames = kanacity.text.toString()

                    kanastatenames = kanastate.text.toString()
                    kanapostalcodenames = kanapostalcode.text.toString()
                    addresskanji1names = addresskanji1.text.toString()
                    addresskanji2names = addresskanji2.text.toString()
                    kanjicitynames = kanjicity.text.toString()
                    kanjistatenames = kanjistate.text.toString()
                    kanjipostalcodenames = kanjipostalcode.text.toString()


                    if (isInternetAvailable) {

                        val imageObject = HashMap<String, String>()
                        if (thumbnail != null) {
                            image = getStringImage(thumbnail)
                        }


                        imageObject["payout_method"] = "stripe"
                        imageObject["currency"] = currencynames
                        //imageObject["document"] = image//branch_code
                        imageObject["account_number"] = accountnumbernames
                        imageObject["address1"] = addresskana1names
                        imageObject["address2"] = addresskana2names
                        imageObject["city"] = kanacitynames
                        imageObject["state"] = kanastatenames
                        imageObject["token"] = sessionManager.accessToken!!
                        imageObject["country"] = CountryCodeNames
                        imageObject["postal_code"] = kanapostalcodenames
                        imageObject["bank_code"] = bank_code_names
                        imageObject["bank_name"] = banknames
                        imageObject["branch_code"] = branch_code_names
                        imageObject["branch_name"] = branchnames
                        imageObject["account_holder_name"] = accountholdernmaes
                        imageObject["account_owner_name"] = accountownernames
                        imageObject["phone_number"] = Phonenumbernames
                        imageObject["kanji_address1"] = addresskanji1names
                        imageObject["kanji_address2"] = addresskanji2names
                        imageObject["kanji_city"] = kanjicitynames
                        imageObject["kanji_state"] = kanjistatenames
                        imageObject["kanji_postal_code"] = kanjipostalcodenames
                        imageObject["gender"] = gendernames


                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (banknames == "") {
                            snackBar(resources.getString(R.string.please_enter_bank_name))
                        } else if (branchnames == "") {
                            snackBar(resources.getString(R.string.please_enter_branch_name))
                        } else if (bank_code_names == "") {
                            snackBar(resources.getString(R.string.please_enter_bank_code))
                        } else if (branch_code_names == "") {
                            snackBar(resources.getString(R.string.please_enter_branch_code))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountownernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_owner_name))
                        } else if (Phonenumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_phone_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (gendernames == "") {
                            snackBar(resources.getString(R.string.please_choose_gender))
                        } else if (addresskana1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address_1_of_kana))
                        } else if (addresskana2names == "") {
                            snackBar(resources.getString(R.string.please_enter_address_2_of_kana))
                        } else if (kanacitynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city_of_kana))
                        } else if (kanastatenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state_of_kana))
                        } else if (kanapostalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code_of_kana))
                        } else if (addresskanji1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address_1_of_kanji))
                        } else if (addresskanji2names == "") {
                            snackBar(resources.getString(R.string.please_enter_address_2_of_kanji))
                        } else if (kanjicitynames == "") {

                            snackBar(resources.getString(R.string.please_enter_city_of_kanji))
                        } else if (kanjistatenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state_of_kanji))
                        } else if (kanjipostalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code_of_kanji))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {

                            updateProfile(imageObject)
                        }

                    } else {
                        currencynames = payoutaddress_currency.text.toString()
                        Ibannames = Iban_no.text.toString()
                        accountholdernmaes = Ac_holder_name.text.toString()
                        address1names = address1.text.toString()
                        address2names = address2.text.toString()
                        citynames = city.text.toString()
                        statenames = state.text.toString()
                        postalcodenames = postalcode.text.toString()
                        val imageObject = HashMap<String, String>()

                        imageObject["payout_method"] = "stripe"
                        imageObject["country"] = CountryCodeNames
                        imageObject["currency"] = currencynames
                        imageObject["iban"] = Ibannames
                        imageObject["account_holder_name"] = accountholdernmaes
                        imageObject["address1"] = address1names
                        //imageObject.put("phone_number", Phonenumbernames)
                        imageObject["address2"] = address2names
                        imageObject["city"] = citynames
                        imageObject["state"] = statenames
                        imageObject["postal_code"] = postalcodenames
                        //imageObject["document"] = image
                        imageObject["token"] = sessionManager.accessToken!!


                        if (currencynames == "") {
                            snackBar(resources.getString(R.string.please_choose_a_currency))
                        } else if (accountnumbernames == "") {
                            snackBar(resources.getString(R.string.please_enter_account_number))
                        } else if (accountholdernmaes == "") {
                            snackBar(resources.getString(R.string.please_enter_account_holder_name))
                        } else if (address1names == "") {
                            snackBar(resources.getString(R.string.please_enter_address1))
                        } else if (citynames == "") {
                            snackBar(resources.getString(R.string.please_enter_city))
                        } else if (statenames == "") {
                            snackBar(resources.getString(R.string.please_enter_state))
                        } else if (postalcodenames == "") {
                            snackBar(resources.getString(R.string.please_enter_postal_code))
                        } else if (legal_doc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_legal_document))
                        } else if (additionalLegalDoc.text.toString() == "") {
                            snackBar(resources.getString(R.string.please_upload_additional_document))
                        } else {
                            updateProfile(imageObject)
                        }
                    }

                } else {
                    snackBar(resources.getString(R.string.please_choose_country))

                }


            }
        }
    }

    private fun selectImage() {
        //final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        val items = arrayOf<CharSequence>(resources.getString(R.string.takephoto_title), resources.getString(R.string.choosefromlib), resources.getString(R.string.cancel))

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.addphoto))
        builder.setItems(items) { dialog, item ->
            //boolean result = Utility.checkPermission(PayoutBankDetailsActivity.this);

            if (items[item] == resources.getString(R.string.takephoto_title)) {
                cameraIntent()

            } else if (items[item] == resources.getString(R.string.choosefromlib)) {
                galleryIntent()

            } else if (items[item] == resources.getString(R.string.cancel)) {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun galleryIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), SELECT_FILE)
        } else {
            startGallaryIntent()
        }
    }


    private fun cameraIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA)
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CAMERA)
        } else {
            startCameraIntent()
        }

    }

    private fun startCameraIntent() {
        imageFile = commonMethods.cameraFilePath(this)
        commonMethods.cameraIntent(imageFile!!, this)
    }

    private fun startGallaryIntent() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, SELECT_FILE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startCameraIntent()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            showOpenSettingsDialog()
                        }
                    }
                }
            }
            SELECT_FILE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGallaryIntent()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showOpenSettingsDialog()
                        }
                    }
                }
            }
            else -> {
            }
        }
    }

    fun showOpenSettingsDialog() {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle(resources.getString(R.string.enable_permission))
        alertBuilder.setMessage(resources.getString(R.string.external_storage_permission_necessary))
        alertBuilder.setPositiveButton(android.R.string.yes) { dialog, which -> startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID))) }
        val alert = alertBuilder.create()
        alert.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data)
            else if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) onCaptureImageResult()
        }
    }

    /**
     * Getting image uri from bitmap
     *
     * @param inContext Activity
     * @param inImage   Image In BitMap
     * @return path
     */
    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        // val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "IMG_" + Calendar.getInstance().getTime(), null)
        return Uri.parse(path)
    }

    private fun onCaptureImageResult() {
        if (imageFile == null) return
        imagePath = imageFile?.path!!
        if (isLegalDocument) {
            legal_doc.setText(imageFile?.name)
            isLegalDocument = false
        } else {
            additionalLegalDoc.setText(imageFile?.name)
            isAdditionalLegalDocument = false
            imageaddtionalPath = imagePath
        }


    }

    private fun onSelectFromGalleryResult(data: Intent?) {
        if (data != null) {
            try {
                val imageFile = commonMethods.getDefaultFileName(this)
                val inputStream = contentResolver?.openInputStream(data!!.data!!)
                val fileOutputStream = FileOutputStream(imageFile)
                commonMethods.copyStream(inputStream, fileOutputStream)
                fileOutputStream.close()
                inputStream?.close()
                if (imageFile == null) return
                imagePath = imageFile.path
                if (isLegalDocument) {
                    legal_doc.setText(imageFile.name)
                    isLegalDocument = false
                } else {
                    additionalLegalDoc.setText(imageFile.name)
                    isAdditionalLegalDocument = false
                    imageaddtionalPath = imagePath
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }


    fun getStringImage(bmp: Bitmap?): String {
        try {
            val baos = ByteArrayOutputStream()
            bmp!!.compress(Bitmap.CompressFormat.JPEG, 20, baos)
            val imageBytes = baos.toByteArray()
            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            return encodedImage
        } catch (e: Exception) {

        }

        return encodedImage
    }


    private fun setGender() {
        genderList.clear()
        for (i in genders.indices) {
            val listdata = Makent_model()
            listdata.countryId = Integer.toString(i)
            listdata.countryName = genders[i]
            genderList.add(listdata)
        }
    }

    private fun enableDefault() {


        Iban_no.visibility = View.VISIBLE
        Ac_holder_name.visibility = View.VISIBLE
        address1.visibility = View.VISIBLE
        address2.visibility = View.VISIBLE
        city.visibility = View.VISIBLE
        state.visibility = View.VISIBLE
        postalcode.visibility = View.VISIBLE


        bsb.visibility = View.GONE
        Accountnumber.visibility = View.GONE
        transitno.visibility = View.GONE
        instituteno.visibility = View.GONE
        routing_number.visibility = View.GONE
        ssn.visibility = View.GONE
        clearing_code.visibility = View.GONE
        bank_code.visibility = View.GONE
        branch_code.visibility = View.GONE
        sort_code.visibility = View.GONE
        bank_name.visibility = View.GONE
        branch_name.visibility = ViewPager.GONE
        Ac_owner_name.visibility = View.GONE
        addresskana_msg.visibility = View.GONE
        addresskanji_msg.visibility = View.GONE
        addresskana_linear.visibility = View.GONE
        addresskanji_linear.visibility = View.GONE
        gender.visibility = View.GONE

        clabeNo.visibility = View.GONE


    }

    override fun onBackPressed() {
        super.onBackPressed()
        sessionManager.countryName2 = ""
    }

    private fun setCurrency(CountryName: String) {
        var position: Int = -1
        for (i in 0 until sCountryDetails.size) {
            if (CountryName.equals(sCountryDetails[i].countryName, true)) {
                position = i
                break
            }
        }
        if (position == -1) {
            return
        } else {
            currencyPosition = position
            currencyname = arrayOfNulls(sCountryDetails[currencyPosition].currencyCode.size)
            CountryCodeNames = sCountryDetails[position].countryCode
            println("Country COde names $CountryCodeNames")
            currencyList.clear()
            currencyname = sCountryDetails[position].currencyCode
            for (i in currencyname.indices) {
                //currencyname = sCountryDetails.get(i).getCurrencyCode();
                val makent_model = Makent_model()
                makent_model.countryName = currencyname[i]
                currencyList.add(makent_model)
            }

        }
    }

    /**
     * Api calling method based on country type
     *
     * @param imageObject hash Map Datas Based on Country Type
     */
    private fun updateProfile(imageObject: HashMap<String, String>) {

        commonMethods.showProgressDialog(this)

        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        var file: File?
        var additionalLegalfile: File?
        try {
            file = File(imagePath)
            additionalLegalfile = File(imageaddtionalPath)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            if (imagePath == "" && imageaddtionalPath == "" && CountryNames == "Other") {

            } else {
                multipartBody.addFormDataPart("document", "IMG_$timeStamp.jpg", RequestBody.create("image/png".toMediaTypeOrNull(), file))
                multipartBody.addFormDataPart("additional_document", "IMG_$timeStamp.jpg", RequestBody.create("image/png".toMediaTypeOrNull(), additionalLegalfile))
            }

            for (key in imageObject.keys) {
                CommonMethods.DebuggableLogI(key, imageObject[key])
                multipartBody.addFormDataPart(key, imageObject[key]!!.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val formBody = multipartBody.build()
        apiService.uploadStripe(formBody, sessionManager.accessToken!!).enqueue(RequestCallback(REQ_UPLOAD_PAYOUT, this))

    }

    /**
     * Success Response For API
     *
     * @param jsonResp JsonResp FroM API
     * @param data     Request Data
     */
    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }
        when (jsonResp.requestCode) {
            REQ_GET_STRIPE -> if (jsonResp.isSuccess) {
                onSuccessgetStripeList(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            REQ_UPLOAD_PAYOUT -> if (jsonResp.isSuccess) {
                snackBar(jsonResp.statusMsg)

                finish()
            } else {
                snackBar(jsonResp.statusMsg)
            }
            else -> {
            }
        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        println("Response checking error $jsonResp $data")
        commonMethods.hideProgressDialog()
        snackBar(data)
    }

    //Show network error and exception
    fun snackBar(statusmessage: String) {
        // Create the Snackbar
        val snackbar = Snackbar.make(gender, "", Snackbar.LENGTH_LONG)
        // Get the Snackbar's layout view
        val layout = snackbar.view as Snackbar.SnackbarLayout
        // Hide the text
        val textView = layout.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE

        // Inflate our custom view
        val snackView = layoutInflater.inflate(R.layout.snackbar, null)
        // Configure the view

        val snackbar_background = snackView.findViewById<View>(R.id.snackbar) as RelativeLayout
        snackbar_background.setBackgroundColor(resources.getColor(R.color.app_background))

        val actionButton = snackView.findViewById<TextView>(R.id.snack_button)
        actionButton.visibility = View.GONE
        actionButton.text = resources.getString(R.string.showpassword)
        actionButton.setTextColor(resources.getColor(R.color.app_background))
        actionButton.setOnClickListener { }

        val textViewTop = snackView.findViewById<View>(R.id.snackbar_text) as TextView
        if (isInternetAvailable) {
            textViewTop.text = statusmessage
        } else {
            textViewTop.text = resources.getString(R.string.Interneterror)
        }

        // textViewTop.setTextSize(getResources().getDimension(R.dimen.midb));
        textViewTop.setTextColor(resources.getColor(R.color.white))

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(resources.getColor(R.color.app_background))
        snackbar.show()

    }

    private fun onSuccessgetStripeList(jsonResponse: JsonResponse) {
        stripeCountriesModel = gson.fromJson(jsonResponse.strResponse, StripeCountriesModel::class.java)

        sCountryDetails.clear()
        sCountryDetails.addAll(stripeCountriesModel.countryList)

        countryname = arrayOfNulls(sCountryDetails.size)
        countryID = arrayOfNulls(sCountryDetails.size)
        countryCode = arrayOfNulls(sCountryDetails.size)


        for (i in sCountryDetails.indices) {

            countryname[i] = sCountryDetails[i].countryName
            countryID[i] = sCountryDetails[i].countryId.toString()
            countryCode[i] = sCountryDetails[i].countryCode

            val listdata = Makent_model()
            listdata.countryId = countryID[i]
            listdata.countryName = countryname[i]
            listdata.countryCode = countryCode[i]
            countryList.add(listdata)

        }

    }

    companion object {
        var flag = false
        var sCountryDetails = ArrayList<StripeCountryDetails>()
        lateinit var alertDialog: android.app.AlertDialog
    }

    override fun onSuccessUserSelected(type: String?, userChoiceData: String?, userChoiceCode: String?) {
        if (type.equals(USER_CHOICE_STRIPE_COUNTRY)) {
            val CountryName = sessionManager.countryName2
            //String CountryName = localSharedPreferences.getSharedPreferences(Constants.StripeCountryCode);
            if (CountryName != null && CountryName != "") {
                setCurrency(CountryName)
                payoutaddress_currency.visibility = View.VISIBLE
                payoutaddress_country.setText(CountryName)
            } else {
                payoutaddress_currency.visibility = View.GONE
                payoutaddress_country.setText("")
            }

            val CountryCurrencyType = sessionManager.countryCurrencyType

            if (CountryCurrencyType != null) {
                if (CountryCurrencyType == "country") {
                    payoutaddress_currency.setText("")
                }
            }


            if (CountryName !== "" && CountryName != null) {


                if (CountryName == "Austria" || CountryName == "Belgium" || CountryName == "Denmark" || CountryName == "Finland" || CountryName == "France" || CountryName == "Germany" || CountryName == "Ireland" || CountryName == "Italy" || CountryName == "Luxembourg" || CountryName == "Norway" || CountryName == "Portugal" || CountryName == "Spain" || CountryName == "Sweden" || CountryName == "Switzerland" || CountryName == "Belgium" || CountryName == "Netherlands") {

                    enableDefault()
                } else if (CountryName == "Australia") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    bsb.visibility = View.VISIBLE
                    Accountnumber.visibility = View.VISIBLE

                } else if (CountryName == "Canada") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    transitno.visibility = View.VISIBLE
                    instituteno.visibility = View.VISIBLE
                    Accountnumber.visibility = View.VISIBLE


                } else if (CountryName == "New Zealand") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    routing_number.visibility = View.VISIBLE
                    Accountnumber.visibility = View.VISIBLE


                } else if (CountryName == "Singapore" || CountryName == "Brazil") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    bank_code.visibility = View.VISIBLE
                    branch_code.visibility = View.VISIBLE
                    Accountnumber.visibility = View.VISIBLE

                } else if (CountryName == "United Kingdom") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    sort_code.visibility = View.VISIBLE
                    Accountnumber.visibility = View.VISIBLE

                } else if (CountryName == "Malaysia") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    sort_code.visibility = View.GONE
                    Accountnumber.visibility = View.VISIBLE

                } else if (CountryName == "Mexico") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    sort_code.visibility = View.GONE
                    Accountnumber.visibility = View.GONE
                    clabeNo.visibility = View.VISIBLE

                } else if (CountryName == "United States") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    routing_number.visibility = View.VISIBLE
                    ssn.visibility = View.VISIBLE
                    Accountnumber.visibility = View.VISIBLE

                } else if (CountryName == "Hong Kong") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    clearing_code.visibility = View.VISIBLE
                    branch_code.visibility = View.VISIBLE
                    Accountnumber.visibility = View.VISIBLE

                } else if (CountryName == "Japan") {
                    enableDefault()
                    Iban_no.visibility = View.GONE
                    address1.visibility = View.GONE
                    address2.visibility = View.GONE
                    city.visibility = View.GONE
                    state.visibility = View.GONE
                    postalcode.visibility = View.GONE

                    Ac_holder_name.visibility = View.VISIBLE
                    bank_name.visibility = View.VISIBLE
                    bank_code.visibility = View.VISIBLE
                    branch_name.visibility = View.VISIBLE
                    branch_code.visibility = View.VISIBLE
                    Accountnumber.visibility = View.VISIBLE
                    Ac_owner_name.visibility = View.VISIBLE
                    gender.visibility = View.VISIBLE

                    addresskana_msg.visibility = View.VISIBLE
                    addresskana_linear.visibility = View.VISIBLE
                    addresskanji_msg.visibility = View.VISIBLE
                    addresskanji_linear.visibility = View.VISIBLE

                } else {
                    payoutaddress_currency.visibility = View.VISIBLE
                    Iban_no.visibility = View.VISIBLE
                    Ac_holder_name.visibility = View.VISIBLE
                    address1.visibility = View.VISIBLE
                    address2.visibility = View.VISIBLE
                    city.visibility = View.VISIBLE
                    state.visibility = View.VISIBLE
                    postalcode.visibility = View.VISIBLE
                    legal_doc.visibility = View.VISIBLE

                }

            }
        } else if (type.equals(USER_CHOICE_STRIPE_CURRENCY, true)) {
            val Currencyname = sessionManager.currencyName2
            if (Currencyname != null) {
                payoutaddress_currency.setText(Currencyname)
            } else {
                payoutaddress_currency.setText("")
            }
        } else {
            val Genders = sessionManager.gender
            if (Genders != null) {
                gender.setText(Genders)
            } else {
                gender.setText("")
            }
        }
    }

    /**
     * Bottom Sheet to choose camera or gallery
     */

    fun pickLegalDoc() {
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
            cameraIntent()
            bottomSheetDialog.dismiss()
        }

        lltLibrary.setOnClickListener {
            galleryIntent()
            bottomSheetDialog.dismiss()
        }
        lltcancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

}

package com.seentechs.newtaxidriver.home.managevehicles

import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.database.Sqlite
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.home.datamodel.BankDetailsModel
import com.seentechs.newtaxidriver.home.datamodel.CurrencyDetailsModel
import com.seentechs.newtaxidriver.home.datamodel.CurreneyListModel
import com.seentechs.newtaxidriver.home.datamodel.DriverProfileModel
import com.seentechs.newtaxidriver.home.fragments.currency.CurrencyListAdapter
import com.seentechs.newtaxidriver.home.fragments.currency.CurrencyModel
import com.seentechs.newtaxidriver.home.fragments.language.LanguageAdapter
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.home.payouts.PayoutDetailsListActivity
import com.seentechs.newtaxidriver.home.signinsignup.SigninSignupHomeActivity
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums
import com.seentechs.newtaxidriver.common.util.Enums.REQ_CURRENCY
import com.seentechs.newtaxidriver.common.util.Enums.REQ_DRIVER_PROFILE
import com.seentechs.newtaxidriver.common.util.Enums.REQ_LANGUAGE
import com.seentechs.newtaxidriver.common.util.Enums.REQ_LOGOUT
import com.seentechs.newtaxidriver.common.util.Enums.REQ_UPDATE_CURRENCY
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.util.userchoice.UserChoice
import com.seentechs.newtaxidriver.common.util.userchoice.UserChoiceSuccessResponse
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.google.locationmanager.TrackingServiceListener
import com.seentechs.newtaxidriver.home.splash.SplashActivity
import kotlinx.android.synthetic.main.app_activity_trip_details.*
import org.json.JSONException
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class SettingActivity : CommonActivity(), ServiceListener, UserChoiceSuccessResponse {


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

    @Inject
    lateinit var userChoice: UserChoice

    internal var bankDetailsModel: BankDetailsModel? = null
    lateinit var trackingServiceListener: TrackingServiceListener

    lateinit @BindView(R.id.tv_currency)
    var currency_code: TextView

    /**
     * Edit Document
     */
    @OnClick(R.id.rlt_manage_doc)
    fun document() {
        sessionManager.isRegister = false

        val intent = Intent(this, DocumentDetails::class.java)
        intent.putExtra(CommonKeys.Intents.DocumentDetailsIntent, driverProfileModel.driverDocuments);
        startActivity(intent)
        overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
    }


    /**
     * Bank Details
     */
    @OnClick(R.id.rlt_payout)
    fun bankDetails() {
        val intent = Intent(this, PayoutDetailsListActivity::class.java)
        intent.putExtra("bankDetailsModel", bankDetailsModel)
        startActivity(intent)
    }



    @OnClick(R.id.back)
    fun onBack() {
        onBackPressed()
    }

    /**
     * Language List
     */
    private var mLastClickTime: Long = 0
    @OnClick(R.id.rltLanguage)
    fun language() {
       // languagelist()
        languagelist = ArrayList()
        loadlang()
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        userChoice.getUsersLanguages(this,languagelist, Enums.USER_CHOICE_LANGUAGE,this)
     //   languagelayout.isClickable = false
    }

    lateinit @BindView(R.id.rlt_currency)
    var currencylayout: RelativeLayout
    lateinit @BindView(R.id.rltLanguage)
    var languagelayout: RelativeLayout


    lateinit var recyclerView1: RecyclerView
    lateinit var languageView: RecyclerView
    lateinit var currencyList: ArrayList<CurrencyModel>
    var languagelist: MutableList<CurrencyModel> = ArrayList<CurrencyModel>()
    lateinit var currencyListAdapter: CurrencyListAdapter
    lateinit var LanguageAdapter: LanguageAdapter
    lateinit var driverProfileModel: DriverProfileModel


    private var dialog: AlertDialog? = null


    lateinit var symbol: Array<String?>
    lateinit var currencycode: Array<String?>
    var currency: String=""
    var Language: String? = null
    lateinit var langocde: String

    @OnClick(R.id.rlt_currency)
    fun currency() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime();
      //  currencylayout.isClickable = false
        getCurrency()
     //   currency_list()
    }

   @OnClick(R.id.rltSignOut)
    fun logoutpopup() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        // set the custom dialog components - text, image and button
        val cancel = dialog.findViewById<View>(R.id.signout_cancel) as TextView
        val signout = dialog.findViewById<View>(R.id.signout_signout) as TextView
        // if button is clicked, close the custom dialog
        cancel.setOnClickListener { dialog.dismiss() }

        signout.setOnClickListener {
            logout()
            dialog.dismiss()
        }
        dialog.show()
    }



    lateinit @BindView(R.id.tv_language)
    var language: TextView

    @Inject
    lateinit var dbHelper: Sqlite
    private var isViewUpdatedWithLocalDB: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity_setting)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        /**Commmon Header Text View */
        commonMethods.setheaderText(resources.getString(R.string.setting),common_header)
        initViews()
        getDriverProfile()

    }


    private fun initViews() {

        dialog = commonMethods.getAlertDialog(this)

    }



    /**
     * get Currency List
     */
    fun getCurrency() {
        apiService.getCurrency(sessionManager.accessToken!!).enqueue(RequestCallback(REQ_CURRENCY, this))
    }



    /**
     * Driver Logout
     */
    private fun logout() {
        commonMethods.showProgressDialog(this as AppCompatActivity)
        apiService.logout(sessionManager.type!!, sessionManager.accessToken!!).enqueue(RequestCallback(REQ_LOGOUT, this))
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(this, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            REQ_LOGOUT -> if (jsonResp.isSuccess) {
                onSuccessLogOut()
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            
            REQ_CURRENCY -> if (jsonResp.isSuccess) {
                onSuccessgetCurrency(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }

            REQ_DRIVER_PROFILE -> if (jsonResp.isSuccess) {
                dbHelper.insertWithUpdate(Constants.DB_KEY_DRIVER_PROFILE.toString(), jsonResp.strResponse)
                onSuccessDriverProfile(jsonResp.strResponse)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            REQ_UPDATE_CURRENCY -> if (jsonResp.isSuccess) {
                //getDriverProfile()
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            REQ_LANGUAGE -> if (jsonResp.isSuccess) {
                //setLocale(sessionManager.languageCode)
                //this.recreate()
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            }
            else -> {
            }
        }
    }


    /*
    *  Get Driver profile while open the page
    **/
    override fun onResume() {
        super.onResume()
        currency = sessionManager.currencyCode!!
        currency_code.text = sessionManager.currencySymbol +" "+ currency

        Language = sessionManager.language
        if (Language != null) {
            language.text = Language
        } else {
            language.text = "English"
        }
    }

    /**
     * Driver Datas
     */
    private fun getDriverProfile() {
        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_DRIVER_PROFILE.toString())
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            //tvOfflineAnnouncement.setVisibility(View.VISIBLE)
            try {
                onSuccessDriverProfile(allHomeDataCursor.getString(0))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    private fun driverProfile(){
        if (commonMethods.isOnline(this)) {
            apiService.getDriverProfile(sessionManager.accessToken!!).enqueue(RequestCallback(REQ_DRIVER_PROFILE, this))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(this)
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            commonMethods.showProgressDialog(this)
            driverProfile()
        } else {
            CommonMethods.showNoInternetAlert(this, object : CommonMethods.INoInternetCustomAlertCallback {
                override fun onOkayClicked() {
                    finish()
                }

                override fun onRetryClicked() {
                    followProcedureForNoDataPresentInDB()
                }

            })
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

        CommonMethods.DebuggableLogI("datacheck", "datacheck")
    }


    /*
     *  Load Driver profile details
     **/
    fun loadData(driverProfileModel: DriverProfileModel) {


        sessionManager.oweAmount = driverProfileModel.oweAmount
        sessionManager.driverReferral = driverProfileModel.driverReferralEarning

        //bankDetailsModel = driverProfileModel.bank_detail


    }



    private fun onSuccessDriverProfile(jsonResponse: String) {
        driverProfileModel = gson.fromJson(jsonResponse, DriverProfileModel::class.java)
        sessionManager.profileDetail = jsonResponse
        loadData(driverProfileModel)
        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            driverProfile()
        }
        val carid = ""
        sessionManager.vehicle_id = carid
        CommonMethods.DebuggableLogV("localshared", "Carid==" + sessionManager.vehicle_id)
    }


    fun setLocale(lang: String) {
        val myLocale = Locale(lang)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.setLocale(myLocale)
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
    }

    private fun onSuccessgetCurrency(jsonResp: JsonResponse) {
        val currencyDetailsModel = gson.fromJson(jsonResp.strResponse, CurrencyDetailsModel::class.java)
        val currencyListModel = currencyDetailsModel.curreneyListModels
        userChoice.getUserCurrency(this,currencyListModel,Enums.USER_CHOICE_CURRENCY,this)
       // currencylayout.isClickable = true
    }



    /**
     * SuccessFully Log out
     */
    private fun onSuccessLogOut() {

        val lang = sessionManager.language
        val langCode = sessionManager.languageCode
        trackingServiceListener = TrackingServiceListener(this)
        trackingServiceListener.stopTrackingService()
        //   CommonMethods.stopSinchService(context)
        CommonMethods.stopSinchService(this)
        sessionManager.clearAll()
        AddFirebaseDatabase().removeDriverFromGeofire(this)
        Firebase.auth.signOut()

        clearApplicationData() // Clear cache data

        sessionManager.language = lang
        sessionManager.languageCode = langCode

        val intent = Intent(this, SigninSignupHomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }



    /*
     *  Clear app cache data
     **/
    fun clearApplicationData() {
        val cache = this.cacheDir
        val appDir = File(cache.parent)
        if (appDir.exists()) {
            val children = appDir.list()
            for (s in children) {
                if ("lib" != s) {
                    deleteDir(File(appDir, s))
                    CommonMethods.DebuggableLogI("TAG", "**************** File /data/data/APP_PACKAGE/$s DELETED *******************")

                    // clearApplicationData();
                }
            }
        }

    }

    /**
     * Update Currency
     */
    private fun updateCurrency() {
        commonMethods.showProgressDialog(this)
        apiService.updateCurrency(currency, sessionManager.accessToken!!)
                .enqueue(RequestCallback(REQ_UPDATE_CURRENCY, this))
    }



    /**
     * Language List
     */
    fun languagelist() {

        languageView = RecyclerView(this)
        loadlang()

        LanguageAdapter = LanguageAdapter(this, languagelist)
        languageView.layoutManager = LinearLayoutManager(this.applicationContext, LinearLayoutManager.VERTICAL, false)
        languageView.adapter = LanguageAdapter

        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.header, null)
        val T = view.findViewById<View>(R.id.header) as TextView
        T.text = getString(R.string.selectlanguage)
        alertDialogStores2 = android.app.AlertDialog.Builder(this).setCustomTitle(view).setView(languageView).setCancelable(true).show()
        languagelayout.isClickable = true

        alertDialogStores2!!.setOnDismissListener {
            // TODO Auto-generated method stub
            if (langclick) {
                langclick = false


                langocde = sessionManager.languageCode!!
                val lang = sessionManager.language
                language.text = lang
                updateLanguage()
            }
            languagelayout.isClickable = true
        }
    }


    fun loadlang() {

        val languages: Array<String>
        val langCode: Array<String>
        languages = resources.getStringArray(R.array.language)
        langCode = resources.getStringArray(R.array.languageCode)
        languagelist.clear()
        for (i in languages.indices) {
            val listdata = CurrencyModel()
            listdata.currencyName = languages[i]
            listdata.currencySymbol = langCode[i]
            languagelist.add(listdata)

        }
    }


    /**
     * Update Language
     */
    fun updateLanguage() {
        commonMethods.showProgressDialog(this as AppCompatActivity)
        apiService.language(sessionManager.languageCode!!, sessionManager.accessToken!!).enqueue(RequestCallback(REQ_LANGUAGE, this))
    }


    companion object {

        var langclick: Boolean = false
        var currencyclick: Boolean? = false
        var alertDialogStores1: android.app.AlertDialog ?= null
        var alertDialogStores2: android.app.AlertDialog ?= null
        var curreneyListModels = ArrayList<CurreneyListModel>()



        /*
     *  Delete app local cache data
     **/
        fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                if (children != null) {
                    for (i in children.indices) {
                        val success = deleteDir(File(dir, children[i]))
                        if (!success) {
                            return false
                        }
                    }
                }
            }

            return dir!!.delete()
        }
    }
    override fun onSuccessUserSelected(type: String?, userChoiceData: String?, userChoiceCode: String?) {
        if (type.equals(Enums.USER_CHOICE_LANGUAGE)){
            val langocde = sessionManager.languageCode
            val lang = sessionManager.language
            language.text = lang
            updateLanguage()
            setLocale(langocde!!)
            /*CommonKeys.isFirstSetpaymentMethod=false
            val intent = Intent(this@SettingActivity, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)*/
        }
        else if (type.equals(Enums.USER_CHOICE_CURRENCY)){
            currency = sessionManager.currencyCode!!
            currency_code.text =sessionManager.currencySymbol +" "+ currency
            updateCurrency()
        }
    }
}

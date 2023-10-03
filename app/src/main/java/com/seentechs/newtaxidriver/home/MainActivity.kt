package com.seentechs.newtaxidriver.home


/**
 * @package com.seentechs.newtaxidriver
 * @subpackage -
 * @category MainActivity
 * @author Seen Technologies
 *
 */


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.database.Cursor
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import com.github.angads25.toggle.widget.LabeledSwitch
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.custompalette.CustomTypefaceSpan
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.database.Sqlite
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.helper.Constants.RequestEndTime
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.startFirebaseChatListenerService
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.stopFirebaseChatListenerService
import com.seentechs.newtaxidriver.common.util.Enums
import com.seentechs.newtaxidriver.common.util.Enums.REQ_DEVICE_STATUS
import com.seentechs.newtaxidriver.common.util.Enums.REQ_DRIVER_PROFILE
import com.seentechs.newtaxidriver.common.util.Enums.REQ_DRIVER_STATUS
import com.seentechs.newtaxidriver.common.util.Enums.REQ_UPDATE_ONLINE
import com.seentechs.newtaxidriver.common.util.Enums.REQ_LOGOUT
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.common.views.SupportActivityCommon
import com.seentechs.newtaxidriver.google.locationmanager.TrackingService
import com.seentechs.newtaxidriver.google.locationmanager.TrackingServiceListener
import com.seentechs.newtaxidriver.google.locationmanager.UpdateLocations
import com.seentechs.newtaxidriver.home.datamodel.*
import com.seentechs.newtaxidriver.home.firebaseChat.ActivityChat
import com.seentechs.newtaxidriver.home.fragments.EarningActivity
import com.seentechs.newtaxidriver.home.fragments.HomeFragment
import com.seentechs.newtaxidriver.home.fragments.RatingActivity
import com.seentechs.newtaxidriver.home.fragments.Referral.ShowReferralOptionsActivity
import com.seentechs.newtaxidriver.home.fragments.payment.PayToAdminActivity
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.managevehicles.DocumentDetails
import com.seentechs.newtaxidriver.home.managevehicles.ManageVehicles
import com.seentechs.newtaxidriver.home.managevehicles.SettingActivity
import com.seentechs.newtaxidriver.home.map.AppUtils
import com.seentechs.newtaxidriver.home.profile.DriverProfile
import com.seentechs.newtaxidriver.home.pushnotification.Config
import com.seentechs.newtaxidriver.home.pushnotification.NotificationUtils
import com.seentechs.newtaxidriver.home.signinsignup.SigninSignupHomeActivity
import com.seentechs.newtaxidriver.home.splash.SplashActivity.Companion.checkVersionModel
import com.seentechs.newtaxidriver.trips.RequestReceiveActivity
import com.seentechs.newtaxidriver.trips.tripsdetails.YourTrips
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/* ************************************************************
                MainActivity page
Its main page to connected to all the screen pages
*************************************************************** */


class MainActivity : CommonActivity(), ServiceListener {


    lateinit var selectedFragment: HomeFragment
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
    lateinit var dbHelper: Sqlite

    @Inject
    lateinit var customDialog: CustomDialog

    @BindView(R.id.txt_checkdriverstatus)
    lateinit var txt_checkdriverstatus: TextView

    @BindView(R.id.view)
    lateinit var view: View

    @BindView(R.id.homelist)
    lateinit var homelist: ImageView

    @BindView(R.id.activity_main)
    lateinit var coordinatorLayout: CoordinatorLayout

    @BindView(R.id.iv_line)
    lateinit var ivLine: TextView

    @BindView(R.id.drawer_layout)
    lateinit var mDrawerLayout: DrawerLayout

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.labledSwitch)
    lateinit var labled_switch: LabeledSwitch


    /* @BindView(R.id.switch_driverstatus)
     lateinit var switch_driverstatus: SwitchCompat*/

    @BindView(R.id.tv_status)
    lateinit var tvStatus: TextView

    @BindView(R.id.tvAppVersion)
    lateinit var tvAppVersion: TextView

    @BindView(R.id.nav_view)
    lateinit var navigationView: NavigationView
    lateinit var headerview: View
    lateinit var profilelayout: LinearLayout
    lateinit var ivDriverName: TextView
    lateinit var ivDriverImage: ImageView
    internal var mOverlay2Hr: TileOverlay? = null

    lateinit var driverProfileModel: DriverProfileModel

    internal var bankDetailsModel: BankDetailsModel? = null


    private var companyName: String? = null
    private var company_id: Int = 0

    var menuItem: Menu? = null

    var statusmessage: String = ""
    var width: Int = 0
    lateinit var animation: TranslateAnimation
    var count = 1

    private var mDrawerToggle: ActionBarDrawerToggle? = null
    lateinit var fragment: HomeFragment

    protected var isInternetAvailable: Boolean = false
    private var backPressed = 0
    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null
    private lateinit var mContext: Context


    protected lateinit var locationRequest: LocationRequest
    internal var REQUEST_CHECK_SETTINGS = 100
    private lateinit var GPSCallbackStatus: Status
    private var isTriggeredFromDriverAPIErrorMessage = false

    internal var extraFeeReasons = ArrayList<ExtraFeeReason>()

    private var builder1: AlertDialog.Builder? = null

    lateinit internal var addFirebaseDatabase: AddFirebaseDatabase
    lateinit var trackingServiceListener: TrackingServiceListener

    lateinit var updateLocations:UpdateLocations

    /**
     * Hash map for update driver status
     */

    val status: HashMap<String, String>
        get() {
            val driverStatusHashMap = HashMap<String, String>()
            driverStatusHashMap["user_type"] = sessionManager.type!!
            driverStatusHashMap["token"] = sessionManager.accessToken!!
            return driverStatusHashMap
        }

    /* Hash Map for driver location Upadte
    * */

    val location: HashMap<String, String>
        get() {
            val locationHashMap = HashMap<String, String>()
            locationHashMap["latitude"] = sessionManager.latitude!!
            locationHashMap["longitude"] = sessionManager.longitude!!
            locationHashMap["user_type"] = sessionManager.type!!
            locationHashMap["car_id"] = sessionManager.vehicle_id!!
            locationHashMap["status"] = sessionManager.driverStatus!!
            locationHashMap["token"] = sessionManager.accessToken!!

            return locationHashMap
        }

    /**
     * Hash map for update driver status
     */

    val deviceId: HashMap<String, String>
        get() {
            val driverStatusHashMap = HashMap<String, String>()
            driverStatusHashMap["user_type"] = sessionManager.type!!
            driverStatusHashMap["device_type"] = sessionManager.deviceType!!
            driverStatusHashMap["device_id"] = sessionManager.deviceId!!
            driverStatusHashMap["token"] = sessionManager.accessToken!!
            return driverStatusHashMap
        }

    @OnClick(R.id.txt_checkdriverstatus)
    fun txtCheckDriverStatus() {
        isInternetAvailable = commonMethods.isOnline(this)
        if (isInternetAvailable) {
            updateDriverStatus()
        } else {
            commonMethods.showMessage(applicationContext, dialog, resources.getString(R.string.no_connection))
        }
    }

    @OnClick(R.id.gotorider)
    fun GotoRider() {
        val managerclock = packageManager
        var i = managerclock.getLaunchIntentForPackage(resources.getString(R.string.package_rider))

        if (i == null) {
            i = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + resources.getString(R.string.package_rider)))
        } else {
            i.addCategory(Intent.CATEGORY_LAUNCHER)

        }
        startActivity(i)
    }
    /*-------------------------FULLL LOGOUT CODE ON Main ------------------*/
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

    /**
     * Driver Logout
     */
    private fun logout() {
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

    /**
     * SuccessFully Log out
     */

    fun clearApplicationData() {
        val cache = this.cacheDir
        val appDir = File(cache.parent)
        if (appDir.exists()) {
            val children = appDir.list()
            for (s in children) {
                if ("lib" != s) {
                    SettingActivity.deleteDir(File(appDir, s))
                    CommonMethods.DebuggableLogI("TAG", "**************** File /data/data/APP_PACKAGE/$s DELETED *******************")

                    // clearApplicationData();
                }
            }
        }

    }
    /*-------------------------/FULLL LOGOUT CODE ON Main ------------------*/
    private var isViewUpdatedWithLocalDB: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        updateLocations = UpdateLocations(this)
        mContext = this
        builder1 = AlertDialog.Builder(mContext)
        addFirebaseDatabase = AddFirebaseDatabase()
        trackingServiceListener = TrackingServiceListener(this)
        addFirebaseDatabase.firebasePushLisener(this)
        dialog = commonMethods.getAlertDialog(this)
        /*
         *  Common loader initialize and internet connection check
         */
        isInternetAvailable = commonMethods.isOnline(this)

        labled_switch.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {
                isInternetAvailable = commonMethods.isOnline(mContext)
                if (isOn) {
                    if (isInternetAvailable) {
                        sessionManager.driverStatus = "Online"
                        tvStatus.text = getString(R.string.online)
                        tvStatus.setTextColor(resources.getColor(R.color.newtaxi_app_navy))
                        updateOnlineStatus()
                    } else {
                        commonMethods.showMessage(mContext, dialog, resources.getString(R.string.no_connection))
                    }
                } else {
                    if (isInternetAvailable) {
                        sessionManager.driverStatus = "Offline"
                        tvStatus.text = getString(R.string.offline)
                        tvStatus.setTextColor(resources.getColor(R.color.newtaxi_app_white))
                        updateOnlineStatus()
                    } else {
                        commonMethods.showMessage(mContext, dialog, resources.getString(R.string.no_connection))
                    }
                }

            }
        })

        if (sessionManager.isLocationUpdatedForOneTime!!){
            sessionManager.isLocationUpdatedForOneTime = false
            updateOnlineStatus()
        }

        if (!isInternetAvailable) {

            dialogfunction() // Show dialog for internet connection not available
        }

        /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
              askForPermission()
          }*/

        width = 1

        translateAnimation()



        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (10 * 1000).toLong()
        locationRequest.fastestInterval = (5 * 1000).toLong()


        //setUpDrawer()
        initNavitgaionview()
        initView()


    }


    private fun initView() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            coordinatorLayout.visibility = View.VISIBLE
            coordinatorLayout.post { this.doCircularReveal() }
        }
        view
        tvAppVersion.text = "V" + CommonMethods.getAppVersionNameFromGradle(this)

        /*
         *  FCM push notification receive function
         */
        receivepushnotification()

        updateDeviceId() // Update FCM device id
        //switch_driverstatus.switchPadding = 40
        //switch_driverstatus.setOnCheckedChangeListener(this)

        if (sessionManager.driverSignupStatus == "pending") {
            //labled_switch.visibility = View.GONE
            //switch_driverstatus.visibility = View.GONE

        }
        /*
         * Set driver status
         */
        if (sessionManager.driverStatus == "Online") {
            labled_switch.isOn = true
            //switch_driverstatus.isChecked = true
            sessionManager.driverStatus = "Online"
        } else {
            labled_switch.isOn = false
            //switch_driverstatus.isChecked = false
            sessionManager.driverStatus = "Offline"
        }
    }


    fun setOnlineVisbility(visibile: Int) {
        labled_switch.visibility = visibile
    }


    fun initNavitgaionview() {

        setUpHomeFragment()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 0F


        // mDrawerToggle?.syncState()


        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        mDrawerLayout.addDrawerListener(mDrawerToggle!!)
        // mDrawerToggle?.getDrawerArrowDrawable()?.setColor(Color.BLACK)
        mDrawerToggle?.syncState()
        drawerListener()

        setupDrawerContent()

        //navigationView.menu.getItem(0).isChecked = true

        navigationView.menu.getItem(0).setChecked(false)

        fragment = HomeFragment.newInstance()


        val m = navigationView.menu
        for (i in 0 until m.size()) {
            val mi = m.getItem(i)
            //for aapplying a font to subMenu ...
            val subMenu = mi.subMenu
            if (subMenu != null && subMenu.size() > 0) {
                for (j in 0 until subMenu.size()) {
                    val subMenuItem = subMenu.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi)
        }

    }


    /**
     * Apply font
     */
    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, resources.getString(R.string.fonts_UBERMedium))
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(CustomTypefaceSpan("", font), 0, mNewTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mi.title = mNewTitle
    }


    fun setUpHomeFragment() {
        selectedFragment = HomeFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, selectedFragment)
        transaction.commit()
    }


    private fun setupDrawerContent() {
        navigationView.setNavigationItemSelectedListener {
            mDrawerLayout.closeDrawer(GravityCompat.START)
            when (it.itemId) {

                /*R.id.choose_vehicle -> {
                    // recreate()
                    // setUpHomeFragment()
                    true
                }*/
                R.id.trips -> {

                    val signin = Intent(this, YourTrips::class.java)
                    startActivity(signin)
                    false
                }
                R.id.earnings -> {

                    val earnings = Intent(this, EarningActivity::class.java)
                    startActivity(earnings)
                    false
                }
                R.id.ratings -> {

                    val rating = Intent(this, RatingActivity::class.java)
                    startActivity(rating)
                    false
                }
                R.id.pay_to_admin -> {
                    val payto = Intent(this, PayToAdminActivity::class.java)
                    startActivity(payto)
                    false
                }
                R.id.nav_referral -> {

                    val intent = Intent(this, ShowReferralOptionsActivity::class.java)
                    startActivity(intent)
                    false
                }
                R.id.manage_vehicles -> {

                    val intent = Intent(this, ManageVehicles::class.java)
                    intent.putExtra(CommonKeys.Intents.DocumentDetailsIntent, driverProfileModel.driverDocuments)
                    intent.putExtra(CommonKeys.Intents.VehicleDetailsIntent, driverProfileModel.vehicle)
                    intent.putExtra("New", false)
                    startActivity(intent)
                    false
                }
                R.id.manage_documents -> {

                    val intent = Intent(this, DocumentDetails::class.java)
                    intent.putExtra(CommonKeys.Intents.DocumentDetailsIntent, driverProfileModel.driverDocuments)
                    startActivity(intent)
                    false
                }
                R.id.nav_setting -> {

                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)
                    false
                }
                R.id.nav_support -> {
                    val intent = Intent(applicationContext, SupportActivityCommon::class.java)
                    startActivity(intent)
                    false
                }
                else -> false
            }

        }
    }


    fun askForPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName))
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION)
    }


    private fun drawerListener() {
        mDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                toolbar.visibility = View.VISIBLE
            }

            override fun onDrawerOpened(drawerView: View) {
                toolbar.visibility = View.VISIBLE

            }

            override fun onDrawerClosed(drawerView: View) {
                toolbar.visibility = View.VISIBLE
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
    }


    /**
     * To do translate animation
     */


    fun translateAnimation() {
        animation = TranslateAnimation(5.0f, (width - 50).toFloat(),
                0.0f, 0.0f)          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        animation.duration = 1000  // animation duration
        animation.repeatCount = 1000  // animation repeat count
        animation.repeatMode = 2   // repeat animation (left to right, right to left )
        animation.fillAfter = true
        animation.isFillEnabled = true
        animation.repeatMode = ValueAnimator.REVERSE
        animation.interpolator = AccelerateInterpolator(2f)

        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(arg0: Animation) {

                animation = TranslateAnimation(5.0f, (width - 150).toFloat(),
                        0.0f, 0.0f)
                animation.duration = 1000  // animation duration
                animation.repeatCount = 1000  // animation repeat count
                animation.repeatMode = 2   // repeat animation (left to right, right to left )
                animation.fillAfter = true
                animation.isFillEnabled = true

                val lparams = RelativeLayout.LayoutParams(50, 10)
                ivLine.layoutParams = lparams
            }

            override fun onAnimationRepeat(arg0: Animation) {

            }

            override fun onAnimationEnd(arg0: Animation) {
                animation.duration = 1000  // animation duration
                animation.repeatCount = 1000  // animation repeat count
                animation.repeatMode = 2   // repeat animation (left to right, right to left )
                animation.fillAfter = true
                animation.isFillEnabled = true
                val lparams = RelativeLayout.LayoutParams(10, 10)
                ivLine.layoutParams = lparams

            }
        })
        //animation.setFillAfter(true);

        ivLine.startAnimation(animation)
    }

    /**
     * Driver Datas
     */
    fun getDriverProfile() {
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

    private fun driverProfile() {
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


    fun updateDriverStatus() {
        commonMethods.showProgressDialog(this as AppCompatActivity)
        apiService.updateCheckStatus(status).enqueue(RequestCallback(REQ_DRIVER_STATUS, this))

    }

    /*
     *  FCM push nofication received funcation called
     */
    fun receivepushnotification() {

        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // checking for type intent filter
                if (intent.action == Config.REGISTRATION_COMPLETE) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)


                } else if (intent.action == Config.PUSH_NOTIFICATION) {
                    // new push notification is received

                    val JSON_DATA = sessionManager.pushJson
                    val json = JSONObject(sessionManager.pushJson)

                    if (json.getJSONObject("custom").has("chat_notification")) {


                        println("Chat json : ")
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
                        val notificationIntent = Intent(applicationContext, ActivityChat::class.java)
                        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())

                        sessionManager.chatJson = json.toString()
                        val notificationUtils = NotificationUtils(applicationContext)
                        val message = json.getJSONObject("custom").getJSONObject("chat_notification").getString("message_data")
                        val title = json.getJSONObject("custom").getJSONObject("chat_notification").getString("user_name")
                        println("ChatNotification : Driver" + message)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, notificationIntent, null, 0L)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            notificationUtils.playNotificationSound()
                        }


                    } else if (count == 1) {
                        val jsonObject: JSONObject
                        try {
                            jsonObject = JSONObject(JSON_DATA)
                            if (jsonObject.getJSONObject("custom").has("cancel_trip")) {
                                statusDialog(resources.getString(R.string.yourtripcancelledbydriver), 1)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }


                } else if (intent.action == Config.UPDATE_UI) {
                    if (intent.getBooleanExtra("isInActive", false)) {
                        HomeFragment.newInstance().updateActInActStatus(true)
                    } else {
                        HomeFragment.newInstance().updateActInActStatus(false)
                    }

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                //GPS Enabled

            } else {
                //GPS not enabled
                //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //            startActivity(intent);
                showGPSNotEnabledWarning()
            }

        }
    }

    private fun showGPSNotEnabledWarning() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.location_not_enabled_please_enable_location))
        builder.setCancelable(true)
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialogInterface, _ ->
            try {
                GPSCallbackStatus.startResolutionForResult(mContext as Activity?, REQUEST_CHECK_SETTINGS)
            } catch (e: IntentSender.SendIntentException) {
                AppUtils.openLocationEnableScreen(mContext)
                e.printStackTrace()
            }
        }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
        dialog.show()

    }


    /*
     *  Animate home page
     */
    private fun doCircularReveal() {

        // get the center for the clipping circle
        val centerX = (coordinatorLayout.left + coordinatorLayout.right) / 2
        val centerY = (coordinatorLayout.top + coordinatorLayout.bottom) / 2

        val startRadius = 0
        // get the final radius for the clipping circle
        val endRadius = Math.max(coordinatorLayout.width, coordinatorLayout.height)

        // create the animator for this view (the start radius is zero)
        var anim: Animator? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(coordinatorLayout,
                    centerX, centerY, startRadius.toFloat(), endRadius.toFloat())
        }
        anim!!.duration = 1500
        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                // splash_logo.setBackgroundColor(getResources().getColor(R.color.colorblack));
            }

        })


        anim.start()
    }


    override fun onStop() {
        super.onStop()
        mRegistrationBroadcastReceiver?.debugUnregister
    }

    override fun onDestroy() {
        CommonKeys.IS_ALREADY_IN_TRIP = false
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (backPressed >= 1) {
            // startActivity(new Intent(this, MainActivity.class));
            CommonKeys.IS_ALREADY_IN_TRIP = false
            finishAffinity()
            super.onBackPressed()


        } else {
            // clean up
            backPressed = backPressed + 1
            Toast.makeText(this, resources.getString(R.string.pressbackagain),
                    Toast.LENGTH_SHORT).show()
        }
    }


    public override fun onResume() {
        super.onResume()

        if (CommonKeys.isRideRequest) {
            CommonKeys.isRideRequest = false
            if (RequestEndTime.isNotEmpty() && commonMethods.checkTimings(commonMethods.getCurrentTime(), commonMethods.getTimeFromLong(RequestEndTime.toLong()))) {
                RequestEndTime = ""
                val requstreceivepage = Intent(applicationContext, RequestReceiveActivity::class.java)
                requstreceivepage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                requstreceivepage.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(requstreceivepage)
            }
        }

        count = 1
        // register FCM registration complete receiver
        mRegistrationBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(it,
                    IntentFilter(Config.REGISTRATION_COMPLETE))
        }
        commonMethods.hideProgressDialog()

        getDriverProfile()



        mRegistrationBroadcastReceiver?.let {

            println("RequestReceiveActivity Triggered : four ")
            LocalBroadcastManager.getInstance(this).registerReceiver(it,
                    IntentFilter(Config.PUSH_NOTIFICATION))
        }

        //update ui
        mRegistrationBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(it,
                    IntentFilter(Config.UPDATE_UI))
        }


        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(applicationContext)

        // code to initiate firebase chat service

        if (!TextUtils.isEmpty(sessionManager.tripId) && sessionManager.isDriverAndRiderAbleToChat) {
            startFirebaseChatListenerService(this)
        } else {
            stopFirebaseChatListenerService(this)
        }


    }

    public override fun onPause() {
        super.onPause()
    }


    fun onSuccessUpdateDriverStatus(jsonResp: JsonResponse) {
        val signInUpResultModel = gson.fromJson(jsonResp.strResponse, DriverStatus::class.java)
        if (signInUpResultModel != null) {
            val driver_status = signInUpResultModel.driverStatus
            sessionManager.driverSignupStatus = driver_status
            if ("Active" == driver_status) {
                commonMethods.showMessage(this, dialog, resources.getString(R.string.active))
                //txt_checkdriverstatus.visibility = View.GONE
                /*txt_driverstatus.text = driver_status
                txt_driverstatus.visibility = View.VISIBLE*/
                // view.visibility = View.VISIBLE
            } else {
                commonMethods.showMessage(this, dialog, resources.getString(R.string.waiting))
            }
        }
    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }


        when (jsonResp.requestCode) {


            REQ_DRIVER_PROFILE -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    dbHelper.insertWithUpdate(Constants.DB_KEY_DRIVER_PROFILE.toString(), jsonResp.strResponse)
                    onSuccessDriverProfile(jsonResp.strResponse)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                }
            }

            REQ_DRIVER_STATUS -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    onSuccessUpdateDriverStatus(jsonResp)
                } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                }
            }
            REQ_DEVICE_STATUS -> {
                if (!jsonResp.isSuccess) {
                    commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
                }
            }
            REQ_UPDATE_ONLINE -> {
                val statuscode = commonMethods.getJsonValue(jsonResp.strResponse, "status_code", String::class.java) as String
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    onSuccessUpdateOnline()
                } else if (statuscode.equals("2", true)) {
                    dialogfunction2(jsonResp.statusMsg)
                    isTriggeredFromDriverAPIErrorMessage = true
                    tvStatus.text = resources.getString(R.string.online)
                    sessionManager.driverStatus = "Online"
                    labled_switch.isOn = true
                    if (!commonMethods.isMyServiceRunning(TrackingService::class.java, this)) {
                        trackingServiceListener.startTrackingService(true, true)
                    }
                }
            }
        }
    }

    private fun onSuccessUpdateOnline() {
        if (sessionManager.driverStatus == "Offline") {
            sessionManager.isGeoFireUpdatedWhenOnline = false
            addFirebaseDatabase.removeDriverFromGeofire(this)
            trackingServiceListener.stopTrackingService()
        } else {
            if (!sessionManager.isGeoFireUpdatedWhenOnline!!) {
                val targetLocation = Location("") //provider name is unnecessary
                if (!(sessionManager.latitude.isNullOrEmpty() || sessionManager.longitude.isNullOrEmpty())) {
                    targetLocation.latitude = sessionManager.latitude!!.toDouble()
                    targetLocation.longitude = sessionManager.longitude!!.toDouble()
                    sessionManager.isGeoFireUpdatedWhenOnline = true
                    println("GEO FIRE UPDATED ")
                    updateLocations.updateDriverLocationInGeoFire(targetLocation, 16.0, this)
                }
            }
            if (!commonMethods.isMyServiceRunning(TrackingService::class.java, this)) {
                trackingServiceListener.startTrackingService(true, true)
            }
        }

    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
    }


    private fun onSuccessDriverProfile(jsonResponse: String) {
        driverProfileModel = gson.fromJson(jsonResponse, DriverProfileModel::class.java)
        sessionManager.profileDetail = jsonResponse
        loadData(driverProfileModel)
        if (isViewUpdatedWithLocalDB) {
            isViewUpdatedWithLocalDB = false
            driverProfile()
        }

    }

    /*
    *  Load Driver profile details
    **/
    fun loadData(driverProfileModel: DriverProfileModel) {

        val first_name = driverProfileModel.firstName
        val last_name = driverProfileModel.lastName
        val user_thumb_image = driverProfileModel.profileImage
        sessionManager.firstName = first_name
        sessionManager.phoneNumber = driverProfileModel.mobileNumber
        company_id = driverProfileModel.companyId
        companyName = driverProfileModel.companyName
        bankDetailsModel = driverProfileModel.bank_detail

        sessionManager.oweAmount = driverProfileModel.oweAmount
        sessionManager.driverReferral = driverProfileModel.driverReferralEarning

        sessionManager.countryCode = driverProfileModel.countryCode


        driverProfileModel.status?.let { selectedFragment.updateDocumentstatus(it) }
        selectedFragment.vehicleTypeModelList.clear()
        selectedFragment.vehicleTypeModelList.addAll(driverProfileModel.vehicle)
        val defaultVehiclePosition = getDefaultPosition()
        if (defaultVehiclePosition != null) {
            selectedFragment.updateUI(defaultVehiclePosition)
        }

        getDocumentsDetails()

        getVehicleDetails()

        headerview = navigationView.getHeaderView(0)
        ivDriverName = headerview.findViewById<View>(R.id.ivDriverName) as TextView
        ivDriverImage = headerview.findViewById<View>(R.id.ivDriverImage) as ImageView

        ivDriverName.text = "$first_name $last_name"
        Picasso.get().load(user_thumb_image).into(ivDriverImage)
        Picasso.get().load(user_thumb_image).into(ivCar)

        menuItem = navigationView.menu


        try {
            navigationView.menu.findItem(R.id.nav_support).isVisible = checkVersionModel.support.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }

        if (company_id > 1) {
            menuItem?.findItem(R.id.pay_to_admin)?.isVisible = false
            menuItem?.findItem(R.id.nav_referral)?.isVisible = false
        } else {

            menuItem?.findItem(R.id.pay_to_admin)?.isVisible = true
            menuItem?.findItem(R.id.nav_referral)?.isVisible = true
        }


        headerViewset()

    }

    private fun getDocumentsDetails() {
        if (driverProfileModel.driverDocuments.size > 0) {
            val driverDocumentSize = driverProfileModel.driverDocuments.size
            var driverdocumentlist = ArrayList<DocumentsModel>()
            driverdocumentlist.addAll(driverProfileModel.driverDocuments)
            for (i in driverdocumentlist.indices) {
                println("Document URL ${driverdocumentlist.get(i).documentUrl}")
                if (driverdocumentlist.get(i).documentUrl != null && !driverdocumentlist.get(i).documentUrl.equals("")) {
                    selectedFragment.tv_addDriverProof.text = resources.getString(R.string.manage_driver_document)

                } else {
                    selectedFragment.tv_addDriverProof.text = resources.getString(R.string.add_driver_proof)
                    return
                }
            }
        } else {
            selectedFragment.tv_addDriverProof.text = resources.getString(R.string.add_driver_proof)
        }

    }

    private fun getVehicleDetails() {

        if (driverProfileModel.vehicle.size > 0) {
            val vehicleDocumentSize = driverProfileModel.vehicle.size
            for (i in selectedFragment.vehicleTypeModelList.indices) {
                var vehicledocumentlist = ArrayList<DocumentsModel>()
                var isDocumentuploaded: Boolean = false
                vehicledocumentlist.addAll(selectedFragment.vehicleTypeModelList.get(i).document)
                for (j in vehicledocumentlist.indices) {
                    isDocumentuploaded = false
                    if (vehicledocumentlist.get(j).documentUrl != null && !vehicledocumentlist.get(j).documentUrl.equals("")) {
                        selectedFragment.tv_addVehicle.text = resources.getQuantityText(R.plurals.manage_vehicles, vehicleDocumentSize)
                        selectedFragment.newVehicle = false
                        isDocumentuploaded = true
                        return
                    }

                }

                if (!isDocumentuploaded) {
                    selectedFragment.tv_addVehicle.text = resources.getString(R.string.add_vehicle)
                    selectedFragment.newVehicle = false
                }
            }

        } else {
            selectedFragment.tv_addVehicle.text = resources.getString(R.string.add_vehicle)
            selectedFragment.newVehicle = true
        }

    }

    private fun getDefaultPosition(): Int? {


        var defaultPosition: Int? = null
        for (j in selectedFragment.vehicleTypeModelList.indices) {

            if (selectedFragment.vehicleTypeModelList.get(j).isDefault.equals("1"))
                defaultPosition = j

        }

        return defaultPosition

    }


    fun headerViewset() {

        /**
         * After trip driver profile details page
         */
        profilelayout = headerview.findViewById<View>(R.id.profilelayout) as LinearLayout
        profilelayout.setOnClickListener {
            val intent = Intent(this, DriverProfile::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)
            mDrawerLayout.closeDrawer(GravityCompat.START)
        }

    }


    /*
         *   Internet not available dialog
         */
    fun dialogfunction() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.turnoninternet))
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ -> builder.setCancelable(true) }

        val alert = builder.create()
        alert.show()
    }


    /*
     *  Dialog for arrive now , begin trip, end trip, payment completed
     */
    fun statusDialog(message: String, show: Int) {


        if (!this.isFinishing) {
            val alert11 = builder1!!.create()
            builder1!!.setMessage(message)
            builder1!!.setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }

            if (!alert11.isShowing) {
                try {
                    alert11.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }


    }


    /*
     *  Dialog for driver status (Active or pending)
     */
    fun dialogfunction2(statusmessage: String) {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(statusmessage)
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    //switch_driverstatus.setChecked(false);
                }

        val alert = builder.create()
        if (!this.isFinishing) {
            alert.show()
        }

    }

    fun updateOnlineStatus() {
        println("UPDATE FROM HOME")
        apiService.updateLocation(location).enqueue(RequestCallback(Enums.REQ_UPDATE_ONLINE, this))

    }

    /*
     *  Update driver device id API call
     */

    fun updateDeviceId() {
        apiService.updateDevice(deviceId).enqueue(RequestCallback(REQ_DEVICE_STATUS, this))
    }


    companion object {
        private val SYSTEM_ALERT_WINDOW_PERMISSION = 2084


        private val TAG = "MAP LOCATION"
        var selectedFrag = 0
    }

    /*
*  Check driver status is online or offline
*/
    /*override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.switch_driverstatus -> {
                CommonMethods.DebuggableLogI("switch_compat", isChecked.toString() + "")

            }
        }
    }*/


}


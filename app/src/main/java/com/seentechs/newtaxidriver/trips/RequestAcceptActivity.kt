package com.seentechs.newtaxidriver.trips

/**
 * @package com.seentechs.newtaxidriver.home
 * @subpackage home
 * @category RequestAcceptActivity
 * @author Seen Technologies
 *
 */


import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.animation.CycleInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.BuildConfig
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.custompalette.FontButton
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.database.Sqlite
import com.seentechs.newtaxidriver.common.dependencies.module.ImageCompressAsyncTask
import com.seentechs.newtaxidriver.common.helper.ComplexPreferences
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.helper.LatLngInterpolator
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.network.PermissionCamer
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonKeys.STATIC_MAP_STYLE
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.DebuggableLogD
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.DebuggableLogE
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.DebuggableLogI
import com.seentechs.newtaxidriver.common.util.Enums.REQ_ARRIVE_NOW
import com.seentechs.newtaxidriver.common.util.Enums.REQ_BEGIN_TRIP
import com.seentechs.newtaxidriver.common.util.Enums.REQ_END_TRIP
import com.seentechs.newtaxidriver.common.util.Enums.REQ_TOLL_REASON
import com.seentechs.newtaxidriver.common.util.RuntimePermissionDialogFragment
import com.seentechs.newtaxidriver.common.util.RuntimePermissionDialogFragment.Companion.STORAGEPERMISSIONARRAY
import com.seentechs.newtaxidriver.common.util.RuntimePermissionDialogFragment.Companion.checkPermissionStatus
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.databinding.ActivityRequestAcceptBinding
import com.seentechs.newtaxidriver.google.direction.DirectionDataModel
import com.seentechs.newtaxidriver.google.direction.GetDirectionData
import com.seentechs.newtaxidriver.google.locationmanager.*
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.datamodel.*
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys
import com.seentechs.newtaxidriver.home.firebaseChat.ActivityChat
import com.seentechs.newtaxidriver.home.firebaseChat.FirebaseChatHandler
import com.seentechs.newtaxidriver.home.interfaces.ImageListener
import com.seentechs.newtaxidriver.home.managevehicles.adapter.DriverDetailsAdapter
import com.seentechs.newtaxidriver.home.map.AppUtils
import com.seentechs.newtaxidriver.home.map.DriverLocation
import com.seentechs.newtaxidriver.home.map.FetchAddressIntentService
import com.seentechs.newtaxidriver.home.map.GpsService
import com.seentechs.newtaxidriver.home.map.drawpolyline.DirectionsJSONParser
import com.seentechs.newtaxidriver.home.pushnotification.Config
import com.seentechs.newtaxidriver.home.pushnotification.NotificationUtils
import com.seentechs.newtaxidriver.trips.proswipebutton.ProSwipeButton
import com.seentechs.newtaxidriver.trips.rating.PaymentAmountPage
import com.seentechs.newtaxidriver.trips.rating.Riderrating
import com.seentechs.newtaxidriver.trips.viewmodel.ReqAccpVM
import com.seentechs.newtaxidriver.trips.viewmodel.RequestAcceptActivityInterface
import com.seentechs.newtaxidriver.trips.voip.NewTaxiSinchService
import com.seentechs.newtaxidriver.trips.voip.NewTaxiSinchService.Companion.sinchClient
import kotlinx.android.synthetic.main.activity_request_accept.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList


/* ************************************************************
                      RequestAcceptActivity
Its used to get RequestAcceptActivity for rider
*************************************************************** */

class RequestAcceptActivity : CommonActivity(),ImageListener, DriverDetailsAdapter.OnRiderClickListener, RequestAcceptActivityInterface, OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RuntimePermissionDialogFragment.RuntimePermissionRequestedCallback, ProSwipeButton.OnAutoSwipeListener, PositionProvider.PositionListener {


    private var currentRiderPosition: Int = 0
    private val riderList = ArrayList<RiderDetailsModelList>()
    var distance = 0f

    /**
     * Coroutine variables
     */


    val uiScope = CoroutineScope(Dispatchers.Main)

    lateinit var pickuplatlng: LatLng
    lateinit var droplatlng: LatLng

    var positionOnMap: String = ""
    var positionOnMap1: String = ""


    val movepoints = ArrayList<LatLng>()
    val polylinepoints = ArrayList<LatLng>()

    var stepPointsList = ArrayList<StepsClass>()
    var overallDuration = 0
    var totalMins: String = ""

    private var encodedImage: String? = null

    var viewModel: ReqAccpVM? = null
    internal var imagePath = ""
    lateinit var SqliteDB: Sqlite

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog
    lateinit var dialog: AlertDialog

    @BindView(R.id.iv_swipe_up)
    lateinit var ivSwipeUp: ImageView

    @BindView(R.id.pickup_address)
    lateinit var pickup_address: TextView

    @BindView(R.id.ridername)
    lateinit var ridername: TextView

    @BindView(R.id.profileimg)
    lateinit var profileimg: ImageView

    @BindView(R.id.user_details_lay)
    lateinit var user_details_lay: RelativeLayout

    @BindView(R.id.latlng)
    lateinit var textView: TextView

    @BindView(R.id.latlng1)
    lateinit var textView1: TextView

    @BindView(R.id.tv_eta)
    lateinit var tvEta: TextView

    @BindView(R.id.tripastatusbutton)
    lateinit var tripastatusbutton: ProSwipeButton

    @BindView(R.id.navigation)
    internal lateinit var navigation: FrameLayout

    @BindView(R.id.cashtrip_lay)
    lateinit var cashtrip_lay: RelativeLayout

    @BindView(R.id.rv_driver_details)
    lateinit var rvDriverDetails: RecyclerView
    private var totalDistanceVal: Float = 0.toFloat()

    @BindView(R.id.fab_start_chat)
    lateinit var fabChat: TextView
    private var isDeletable = true
    internal lateinit var edtxOne: EditText
    internal lateinit var edtxTwo: EditText
    internal lateinit var edtxThree: EditText
    internal lateinit var edtxFour: EditText
    internal lateinit var validateOTPToBeginTrip: Button
    internal lateinit var tvOTPErrorMessage: TextView
    internal lateinit var rlEdittexts: RelativeLayout
    private var beginTripOTP = ""
    private var backPressCounter: CountDownTimer? = null

    lateinit var dropOptions: MarkerOptions
    var overviewpolylines: String = ""
    var newLatLng: LatLng? = null
    var count0 = 0
    var count1 = 0
    var polyline: Polyline? = null
    lateinit var mContext: Context
    lateinit var latLong: LatLng
    lateinit var tripDetailsModel: TripDetailsModel
    lateinit var carmarker: Marker
    var startbear = 0f
    var endbear = 0f
    var marker: Marker? = null
    var samelocation = false
    var firstloop = true
    var speed = 13f
    lateinit var handler_movemap: Handler
    var valueAnimator: ValueAnimator? = null
    lateinit var twoDForm: DecimalFormat
    var imagepath: String? = null
    var imageInSD: String = ""
    var compressPath: String = ""
    var pathString: String = ""
    var getDricetionalurlCount = 0
    var time: String = "1"
    var EtaTime: String = "1"
    var ETACalculatingwithDistance: Double = 0.0

    var floatWidget: View? = null
    var mWindowManager: WindowManager? = null


    var isEtaFromPolyline = false
    private val compressImgWeakRef = WeakReference<AppCompatActivity>(this)

    /**
     * The formatted location address.
     */

    lateinit var dropMarker: Marker

    private var mLocationRequest: LocationRequest? = null


    protected var mAddressOutput: String? = null
    protected var mAreaOutput: String? = null
    protected var mCityOutput: String? = null
    protected var mStateOutput: String? = null
    protected var isInternetAvailable: Boolean = false
    private var mMap: GoogleMap? = null
    private lateinit var droplatlngTemp: LatLng
    private var broadcastReceiver: BroadcastReceiver? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null
    private var isTrip = false
    internal lateinit var complexPreferences: ComplexPreferences
    private var extraFeeAmount = ""
    private var extra_fee_reason = ""
    private var extraFeeDescriptionID = ""
    private var edtxExtraFeeDescription: EditText? = null
    private var edtx_extra_fee_other_description: EditText? = null

    private var tipl_extra_fee_other_description: TextInputLayout? = null
    internal var extraFeeReason: ExtraFeeReason? = null
    internal var alertDialogStores: android.app.AlertDialog? = null
    private var toll_reasons: Toll_reasons? = null

    private var title: TextView? = null
    internal var extraFeeReasons = ArrayList<ExtraFeeReason>()

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private val MAX_DISTANCE = 750

    private var mResultReceiver: AddressResultReceiver? = null
    private var lastLocation: Location? = null
    lateinit var statmap: Bitmap
    protected var locationRequest: LocationRequest? = null

    internal var calculatedDistance = 0f
    internal var distanceCalculatedEvery1Sec = 0f
    internal var googleDistance = 0f

    internal var delay = 0 // delay for 0 sec.
    internal var periodDirection = 15000 // repeat every 15 sec.
    private lateinit var timerDirection: Timer
    private lateinit var timerDirectionTask: TimerTask
    private val handler = Handler()
    var isCheckDirection = true
    var riderChanged = false

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>


    /*
    Service and activity connection variables
     */

    private var mBoundService: GpsService? = null
    private var mIsBound: Boolean = false

    private var isOnPauseCalled: Boolean = false


    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has
            // been established, giving us the service object we can use
            // to interact with the service.  Because we have bound to a
            // explicit service that we know is running in our own
            // process, we can cast its IBinder to a concrete class and
            // directly access it.
            mBoundService = (service as GpsService.LocalBinder).getService()


        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has
            // been unexpectedly disconnected -- that is, its process
            // crashed. Because it is running in our same process, we
            // should never see this happen.
            mBoundService = null

        }
    }

    /*
    Service and activity connection variables dec ends
     */
    @OnClick(R.id.navigation)
    fun onclickNavigation() {
        load()
    }

    @OnClick(R.id.back)
    fun back() {
        CommonKeys.IS_ALREADY_IN_TRIP = true
        val redirectMain = Intent(applicationContext, MainActivity::class.java)
        startActivity(redirectMain)
        finish()
    }

    fun getBitmapFromURL(src: String): Bitmap {
        try {
            val url = URL(src)
            val connection = url.openConnection() as HttpsURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            return statmap
        } catch (e: NetworkOnMainThreadException) {
            e.printStackTrace()
            return statmap
        } catch (e: Exception) {
            e.printStackTrace()
            return statmap
        }

    }

    @OnClick(R.id.profileimg_card)
    fun onclickProfile() {
        /*
         *  Redirect to profile page
         */
        val intent = Intent(applicationContext, RiderProfilePage::class.java)
        intent.putExtra("currentRiderPosition", currentRiderPosition)
        intent.putExtra("riderDetails", tripDetailsModel)
        startActivity(intent)

    }

    @OnClick(R.id.user_details_lay)
    fun onclickUser() {
        /*
         *  Redirect to profile page
         */
        val intent = Intent(applicationContext, RiderProfilePage::class.java)
        intent.putExtra("currentRiderPosition", currentRiderPosition)
        intent.putExtra("riderDetails", tripDetailsModel)
        startActivity(intent)
    }

    @OnClick(R.id.fab_start_chat)
    fun startChating() {
        try {
            sessionManager.riderName = tripDetailsModel.riderDetails.get(currentRiderPosition).name
            sessionManager.riderId = tripDetailsModel.riderDetails.get(currentRiderPosition).riderId!!
            sessionManager.riderRating = tripDetailsModel.riderDetails.get(currentRiderPosition).rating
            sessionManager.riderProfilePic = tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage
            sessionManager.tripId = tripDetailsModel.riderDetails.get(currentRiderPosition).tripId!!.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sessionManager.chatJson = ""

        startActivity(Intent(this, ActivityChat::class.java))
    }

    lateinit var positionProvider: PositionProvider
    private var directionModel: DirectionDataModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val binding: ActivityRequestAcceptBinding = DataBindingUtil.setContentView(this, R.layout.activity_request_accept)
        viewModel = ViewModelProviders.of(this).get(ReqAccpVM::class.java)
        binding.viewmodel = viewModel!!
        viewModel?.requestAcceptActivityInterface = this

        //setContentView(R.layout.activity_request_accept)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        NotificationUtils.clearNotifications(applicationContext)
        commonMethods.setheaderText(resources.getString(R.string.enroute), common_header)
        positionProvider = PositionProviderFactory.create(this, this)
        permissionCheck()

        viewModel?.initAppController()
        getIntentValues()
        tripStatusChanges()
        initView()
        showBottomSheet()

    }


    private fun permissionCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        positionProvider.requestSingleLocation()
    }

    fun getIntentValues() {
        /*
         *  Get Trip rider details
         */
        val extras = intent.extras
        if (extras != null) {
            tripDetailsModel = intent.getSerializableExtra("riderDetails") as TripDetailsModel //Obtaining data
            if (intent.getIntExtra(CommonKeys.KEY_IS_NEED_TO_PLAY_SOUND, CommonKeys.NO) == CommonKeys.YES) {
                playNotificatinSoundAndViberate()
            }
        }
    }

    private fun showBottomSheet() {


        mBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                fabChat.visibility = View.GONE
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                if (tripDetailsModel.riderDetails.size > 1) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        fabChat.visibility = View.GONE
                    } else {
                        fabChat.visibility = View.VISIBLE
                    }
                    //mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                } else {
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                }

            }
        })


    }

    private fun initView() {
        CommonMethods.startSinchService(this)
        //start firebase chat listernet service
        startFirebaseChatListenerService()

        /*
        *  Receive push notification
        */
        receivepushnotification()

        var poolIDs = ""
        var iscashtrip = ""
        sessionManager.poolIds = ""
        for (i in tripDetailsModel.riderDetails.indices) {
            if (tripDetailsModel.isPool) {
                sessionManager.isPool = true
                poolIDs = if (poolIDs.isNotEmpty()) {
                    poolIDs + "," + tripDetailsModel.riderDetails[i].tripId
                } else {
                    tripDetailsModel.riderDetails[i].tripId!!
                }
            }
        }

        sessionManager.poolIds = poolIDs

        mBottomSheetBehavior = BottomSheetBehavior.from(user_details_lay1)


        isEtaFromPolyline = true
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        SqliteDB = Sqlite(this@RequestAcceptActivity)
        hideChatAccordingToBookingType()
        complexPreferences = ComplexPreferences.getComplexPreferences(this, "mypref", Context.MODE_PRIVATE)

        statmap = BitmapFactory.decodeResource(resources, R.drawable.mapimg)
        dialog = commonMethods.getAlertDialog(this)
        textView.movementMethod = ScrollingMovementMethod()
        textView1.movementMethod = ScrollingMovementMethod()
        twoDForm = DecimalFormat("#.##########")
        val dfs = DecimalFormatSymbols()
        dfs.decimalSeparator = '.'
        twoDForm.decimalFormatSymbols = dfs
        isInternetAvailable = commonMethods.isOnline(this)

        cashtrip_lay.visibility = View.GONE
        if (tripDetailsModel.riderDetails.size > 1) {
            ivSwipeUp.visibility = View.VISIBLE
            rvDriverDetails.visibility = View.VISIBLE
            //mBottomSheetBehavior.setAllowUserDragging(true)
        } else {
            //mBottomSheetBehavior.setAllowUserDragging(false)
            ivSwipeUp.visibility = View.INVISIBLE
            rvDriverDetails.visibility = View.GONE
        }

        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        mContext = this
        handler_movemap = Handler()
        mResultReceiver = AddressResultReceiver(Handler())
        tv_count.text = CommonKeys.getUrlCount.toString()
        /*
         *  Check GPS enable or not
         */
        checkGPSEnable()


        pickup_address.movementMethod = ScrollingMovementMethod()


        initDriverDetails()
        tripastatusbutton.swipeDistance = 0.5f

        tripastatusbutton.text = intent.getStringExtra("tripstatus").toString()
        try {
            if (sessionManager.tripId != null || sessionManager.tripId != "null" || sessionManager.tripId != "") {
                tripastatusbutton.text = sessionManager.subTripStatus!!
            }
        } catch (n: NullPointerException) {
            //null
            tripastatusbutton.text = sessionManager.subTripStatus!!
        } catch (e: Exception) {
            //null
            tripastatusbutton.text = sessionManager.subTripStatus!!
        }

        if (sessionManager.subTripStatus.equals(resources.getString(R.string.end_trip))) {
            pickup_address.text = tripDetailsModel.riderDetails.get(currentRiderPosition).destAddress
        } else {
            pickup_address.text = tripDetailsModel.riderDetails.get(currentRiderPosition).pickupAddress
        }



        if (tripDetailsModel.riderDetails.get(currentRiderPosition).paymentMode.equals("Cash")) {
            cashtrip_lay.visibility = View.VISIBLE
        } else {
            cashtrip_lay.visibility = View.GONE

        }
        ridername.text = tripDetailsModel.riderDetails.get(currentRiderPosition).name
        val imageUr = tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage

        Picasso.get().load(imageUr)
                .into(profileimg)


        tripastatusbutton.setOnSwipeListener(object : ProSwipeButton.OnSwipeListener {
            override fun onButtonTouched() {
                mBottomSheetBehavior.isDraggable = false
            }

            override fun onButtonReleased() {
                /*if (tripDetailsModel.riderDetails.size>1)

                else
                    mBottomSheetBehavior.setAllowUserDragging(false)*/
                mBottomSheetBehavior.isDraggable = true

            }

            override fun onSwipeConfirm() {

                isInternetAvailable = commonMethods.isOnline(applicationContext)
                if (isInternetAvailable) {
                    tripFunction()
                } else {
                    tripastatusbutton.showResultIcon(false, true)
                    commonMethods.showMessage(mContext, dialog, resources.getString(R.string.no_connection))

                }

            }
        })


    }


    override fun setRiderClick(make: RiderDetailsModelList, position: Int) {

        currentRiderPosition = position
        riderChanged = true
        polylinepoints.clear()
        tripStatusChanges()
        initView()
        initResume()
        acceptedRequest()

    }

    private fun tripStatusChanges() {
        val tripStatus = tripDetailsModel.riderDetails.get(currentRiderPosition).status
        sessionManager.tripId = tripDetailsModel.riderDetails.get(currentRiderPosition).tripId!!.toString()
        sessionManager.bookingType = tripDetailsModel.riderDetails.get(currentRiderPosition).bookingType.toString()
        sessionManager.beginLatitude = tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lat
        sessionManager.beginLongitude = tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lng
        val invoiceModels = tripDetailsModel.riderDetails.get(currentRiderPosition).invoice
        // sessionManager.paymentMethod = tripDetailsModel.paymentMode

        // Pass different data based on trip status

        sessionManager.isTrip = true
        if (CommonKeys.TripStatus.Scheduled == tripStatus || CommonKeys.TripStatus.Begin_Trip == tripStatus || CommonKeys.TripStatus.End_Trip == tripStatus) {

            commonMethods.hideProgressDialog()
            if (CommonKeys.TripStatus.Scheduled == tripStatus) {

                //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
                sessionManager.subTripStatus = resources.getString(R.string.confirm_arrived)
                CommonKeys.isTripBegin = false
                tvEta.visibility = View.VISIBLE

            } else if (CommonKeys.TripStatus.Begin_Trip == tripStatus) {
                //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
                sessionManager.subTripStatus = resources.getString(R.string.begin_trip)
                tvEta.visibility = View.VISIBLE


            } else if (CommonKeys.TripStatus.End_Trip == tripStatus) {
                tvEta.visibility = View.GONE
                //sessionManager.setTripStatus("Begin Trip");
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.BeginTrip
                sessionManager.subTripStatus = resources.getString(R.string.end_trip)
            }
        }
    }

    private fun tripFunction() {

        isInternetAvailable = commonMethods.isOnline(applicationContext)


        if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived)) {
            if (isInternetAvailable) {
                tvEta.visibility = View.VISIBLE
                arriveRequest()
            } else {
                //tripastatusbutton.showResultIcon(false, true);
                commonMethods.showMessage(mContext, dialog, resources.getString(R.string.no_connection))
            }

        } else if (tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
            /*
             *  Begin trip API call
             */
            if (isInternetAvailable) {
                // if (tripDetailsModel.riderDetails[currentRiderPosition].isOtpEnabled!!)
                tvEta.visibility = View.VISIBLE
                showBottomOTPsheet()

                /*else {
                    tripastatusbutton.AutoSwipe(this, CommonKeys.TripDriverStatus.BeginTrip)
                }*/
            } else {
                commonMethods.showMessage(mContext, dialog, resources.getString(R.string.no_connection))
            }
        } else if (tripastatusbutton.text.toString() == resources.getString(R.string.end_trip)) {

            if (sessionManager.isExtraFeeCollectable) {
                //   System.out.println("extrafeeReason"+extraFeeReason.getName());
                tvEta.visibility = View.GONE
                showTollFeeBottomSheet()
            } else {
                tripastatusbutton.AutoSwipe(this, CommonKeys.TripDriverStatus.EndTrip)
            }

        }
    }

    fun showTollFeeBottomSheet() {
        val tiplFeeDescription: TextInputLayout
        val tiplFeeAmount: TextInputLayout
        val view = layoutInflater.inflate(R.layout.extra_fee, null)
        val btnNo: FontButton
        val btnYes: FontButton
        val btnCancel: FontButton
        val btnApply: FontButton
        val applyTollFare = view.findViewById<ConstraintLayout>(R.id.ctlv_apply_toll_fare)
        val enterTollFare = view.findViewById<ConstraintLayout>(R.id.ctlv_enter_toll_fare)
        tiplFeeDescription = view.findViewById(R.id.tipl_extra_fee_description)
        tiplFeeAmount = view.findViewById(R.id.tipl_extra_fee_amount)
        val tollFare = view.findViewById<EditText>(R.id.edtx_toll_amount)
        edtxExtraFeeDescription = view.findViewById(R.id.edtx_extra_fee_description)
        edtx_extra_fee_other_description = view.findViewById(R.id.edtx_extra_fee_other_description)
        tipl_extra_fee_other_description = view.findViewById(R.id.tipl_extra_fee_other_description)
        //FontTextView currencySymbol = view.findViewById(R.id.tv_currency_symbol);

        btnNo = view.findViewById(R.id.btn_no)
        btnYes = view.findViewById(R.id.btn_yes)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnApply = view.findViewById(R.id.btn_apply)

        tripastatusbutton.showResultIcon(false, true)

        tollFare.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (!TextUtils.isEmpty(s.toString())) {
                    tiplFeeAmount.isErrorEnabled = false
                }
            }
        })

        edtx_extra_fee_other_description!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (!TextUtils.isEmpty(s.toString())) {
                    //tipl_extra_fee_other_description!!.isErrorEnabled = false
                }
            }
        })

        tiplFeeAmount.hint = resources.getString(R.string.amount) + " (" + sessionManager.currencySymbol + ")"


        val bottomSheetDialog = Dialog(this, R.style.BottomSheetDialogThemeTransparent)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(true)
        if (bottomSheetDialog.window == null) return
        bottomSheetDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.setCancelable(false)

        bottomSheetDialog.show()

        btnNo.setOnClickListener { _ ->
            isTollFee = false
            tripastatusbutton.AutoSwipe(this, CommonKeys.TripDriverStatus.EndTrip)
            bottomSheetDialog.dismiss()

        }
        btnYes.setOnClickListener { _ ->
            viewModel?.getTollReasons(this)
            applyTollFare.visibility = View.GONE
            enterTollFare.visibility = View.VISIBLE
            isTollFee = true
        }
        btnCancel.setOnClickListener { _ ->
            applyTollFare.visibility = View.VISIBLE

            enterTollFare.visibility = View.GONE

            tiplFeeAmount.isErrorEnabled = false
            tiplFeeDescription.isErrorEnabled = false
        }

        edtxExtraFeeDescription!!.setOnClickListener { _ -> showExtraFeeReasons(tiplFeeDescription) }

        btnApply.setOnClickListener { _ ->

            tiplFeeAmount.isErrorEnabled = false
            tiplFeeDescription.isErrorEnabled = false

            if (tollFare.text.toString().trim { it <= ' ' } == "0" || tollFare.text.toString().isEmpty()) {
                //tiplFeeAmount.isErrorEnabled = true
                Toast.makeText(this,resources.getString(R.string.enter_amount_empty),Toast.LENGTH_SHORT).show()
                //tiplFeeAmount.error = resources.getString(R.string.enter_amount_empty)
            } else if (extraFeeDescriptionID.equals("", ignoreCase = true)) {
                //tiplFeeDescription.isErrorEnabled = true
                Toast.makeText(this,resources.getString(R.string.error_select_extra_fee_description),Toast.LENGTH_SHORT).show()
                //tiplFeeDescription.error = resources.getString(R.string.error_select_extra_fee_description)
            } else {
                if (edtx_extra_fee_other_description!!.visibility == View.VISIBLE && TextUtils.isEmpty(edtx_extra_fee_other_description!!.text.toString())) {
                    //tipl_extra_fee_other_description!!.isErrorEnabled = true
                    Toast.makeText(this,resources.getString(R.string.enter_extra_fee_description),Toast.LENGTH_SHORT).show()
                    //tipl_extra_fee_other_description!!.error = resources.getString(R.string.enter_extra_fee_description)
                } else {
                    bottomSheetDialog.dismiss()
                    extraFeeAmount = tollFare.text.toString().trim { it <= ' ' }

                    if (extraFeeDescriptionID.equals("1", ignoreCase = true)) {

                        extra_fee_reason = edtx_extra_fee_other_description!!.text.toString()
                        // System.out.println("extra fee_reason"+extra_fee_reason);
                    } else {
                        extra_fee_reason = edtxExtraFeeDescription!!.text.toString()
                        // System.out.println("extra fee_reason"+extra_fee_reason);
                    }
                    //extraFeeDescription= edtxExtraFeeDescription.getText().toString().trim();
                    println("extra fee_reason$extra_fee_reason")
                    tripastatusbutton.AutoSwipe(this, CommonKeys.TripDriverStatus.EndTrip)
                }
            }

        }

    }


    /**
     *
     */

    private suspend fun downloadTask(polyLineType: String, origin: LatLng, dest: LatLng) {

        withContext(Dispatchers.IO) {


            var result: String? = null


            val polylineurl = viewModel?.getDirectionsUrl(origin, dest)

            result = polylineurl?.let { downloadUrl(it) }


            val jObject: JSONObject

            var routes: List<List<HashMap<String, String>>>? = null
            var distances = ""
            var overviewPolyline = ""
            var stepPoints = java.util.ArrayList<StepsClass>()
            var totalDuration: Int = 0


            //  if(result!=null){

            try {
                jObject = JSONObject(result)
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
                distances = parser.parseDistance(jObject)
                overviewPolyline = parser.parseOverviewPolyline(jObject)
                stepPoints = parser.parseStepPoints(jObject)
                totalDuration = parser.parseDuration(jObject)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            println("Distances : " + distances)


            val points: java.util.ArrayList<LatLng> = java.util.ArrayList<LatLng>()
            val lineOptions: PolylineOptions = PolylineOptions()
            if (routes != null) {
                for (i in routes.indices) {

                    val path = routes[i]

                    for (j in path.indices) {
                        val point = path[j]

                        val lat = java.lang.Double.parseDouble(point["lat"]!!)
                        val lng = java.lang.Double.parseDouble(point["lng"]!!)
                        val position = LatLng(lat, lng)
                        points.add(position)
                    }

                    lineOptions.addAll(points)
                    lineOptions.width(6f)
                    lineOptions.color(ContextCompat.getColor(this@RequestAcceptActivity, R.color.newtaxi_app_navy))
                    lineOptions.geodesic(true)

                }

                // Drawing polyline in the Google Map for the i-th route
                //  mMap.addPolyline(lineOptions);
            }


            withContext(Dispatchers.Main) {
                if (polyLineType.equals(CommonKeys.DownloadTask.DistCalcResume)) {
                    updateDistanceAndPoints(distances, points)
                } else if (polyLineType.equals(CommonKeys.DownloadTask.EndTrip)) {
                    //endTripDirectionUrl(distances, overviewPolyline, stepPoints, totalDuration)
                } else if (polyLineType.equals(CommonKeys.DownloadTask.UpdateRoute)) {
                    updateRouteDirectionUrl(totalDuration, distances, stepPoints, overviewPolyline, lineOptions, points)
                } else if (polyLineType.equals(CommonKeys.DownloadTask.AcceptRequest)) {
                    acceptedRequestDirectionUrl(totalDuration, distances, stepPoints, overviewPolyline, lineOptions, points)
                }
            }


        }


    }

    private fun acceptedRequestDirectionUrl(totalDuration: Int, distances: String, stepPoints: ArrayList<StepsClass>, overviewPolyline: String, lineOptions: PolylineOptions, points: ArrayList<LatLng>) {
        overallDuration = totalDuration

        calculateEtaFromPolyline(distances)

        stepPointsList.clear()
        stepPointsList.addAll(stepPoints)

        val getLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = latLong.latitude
            longitude = latLong.longitude
        }
        println("ETA LINE 998")
        calculateEta(getLocation)
        //TimeUnit.SECONDS.toMinutes(overallDuration.toLong())

        tv_count.text = CommonKeys.getUrlCount.toString()
        polylinepoints.clear()
        polylinepoints.addAll(points)
        AddFirebaseDatabase().UpdatePolyLinePoints(overviewPolyline, this)
        if (polyline != null) {
            polyline!!.remove()
        }

        if (mMap != null) {
            polyline = mMap!!.addPolyline(lineOptions)
            //MapAnimator.getInstance().animateRoute(mMap,output.getPoints());
        }
    }

    private fun updateRouteDirectionUrl(totalDuration: Int, distances: String, stepPoints: ArrayList<StepsClass>, overviewPolyline: String, lineOptions: PolylineOptions, points: ArrayList<LatLng>) {


        if (mMap != null) {
            overallDuration = totalDuration

            calculateEtaFromPolyline(distances)


            stepPointsList.clear()
            stepPointsList.addAll(stepPoints)
            val currentLocation = Location("")
            currentLocation.latitude = sessionManager.currentLatitude!!.toDouble()
            currentLocation.longitude = sessionManager.currentLongitude!!.toDouble()

            println("ETA LINE 1030")
            calculateEta(currentLocation)

            val count = (CommonKeys.getUrlCount++).toString()
            tv_count.text = CommonKeys.getUrlCount.toString()
            polylinepoints.clear()
            polylinepoints.addAll(points)
            if (!isOnPauseCalled) {
                AddFirebaseDatabase().UpdatePolyLinePoints(overviewPolyline, this)
            }
            if (polyline != null) {
            }
            polyline?.remove()
            // Toast.makeText(getApplicationContext(), "Map route Updated", Toast.LENGTH_SHORT).show();
            polyline = mMap!!.addPolyline(lineOptions)

        }

    }

    private fun endTripDirectionUrl(distances: String, overviewPolyline: String, stepPoints: ArrayList<StepsClass>, totalDuration: Int) {

        var distancevar = distances
        overallDuration = totalDuration

        calculateEtaFromPolyline(distances)

        if (TextUtils.isEmpty(distancevar))
            distancevar = "0"
        println("value " + java.lang.Float.valueOf(distancevar) / 1000)

        stepPointsList.clear()
        stepPointsList.addAll(stepPoints)

        //Toast.makeText(getApplicationContext(),"Distanace "+distances,Toast.LENGTH_SHORT).show();
        val distanceInMeters = java.lang.Float.valueOf(distancevar)
        googleDistance = (distanceInMeters / 1000.0).toFloat()

        try {
            googleDistance = java.lang.Float.valueOf(twoDForm.format(googleDistance.toDouble()).replace(",".toRegex(), "."))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        println("Get calculatedDistance $calculatedDistance")
        println("Get distanceCalculatedEvery10Sec $distanceCalculatedEvery1Sec")
        println("Get googleDistance $googleDistance")

        if (calculatedDistance >= distanceCalculatedEvery1Sec && calculatedDistance >= googleDistance)
            distance = calculatedDistance
        else if (distanceCalculatedEvery1Sec >= calculatedDistance && distanceCalculatedEvery1Sec >= googleDistance)
            distance = distanceCalculatedEvery1Sec
        else {
            distance = googleDistance
            pathString = "&path=color:0x000000ff|weight:4|enc%3A$overviewPolyline"
        }

        if (BuildConfig.DEBUG) {
            if (!commonMethods.getFileWriter()) {
                commonMethods.createFileAndUpdateDistance(this)
            }
            commonMethods.finalDistanceUpdateInFile(googleDistance, distanceCalculatedEvery1Sec, calculatedDistance)
        }

        /*if (calculatedDistance > googleDistance) {
            distance = calculatedDistance
        } else {
            distance = googleDistance
            pathString = "&path=color:0x000000ff|weight:4|enc%3A$overviewPolyline"
        }*/

        val staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x250&" +
                pickuplatlng.latitude + "," + pickuplatlng.longitude +
                /*pathString +*/ positionOnMap + "" + positionOnMap1 + //"&zoom=14" +
                "&style=" + STATIC_MAP_STYLE +
                "&key=" + sessionManager.googleMapKey +
                "&language=" + Locale.getDefault()
        DebuggableLogE("static map", staticMapURL)
        println("Static Map Url : " + staticMapURL)

        val bm = getBitmapFromURL(staticMapURL)

        var file = savebitmap("StaticMap", bm)
        ImageCompressAsyncTask(500,this,file.absolutePath,this,"").execute()

    }


    /**
     *
     */
    private fun updateDistanceAndPoints(distances: String, points: ArrayList<LatLng>) {

        var distanceInKm = 0F

        try {
            distanceInKm = (distances.toFloat() / 1000.0).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sessionManager.onlineDistance = sessionManager.onlineDistance + distanceInKm


        var i = 0
        while (i < points.size) {
            SqliteDB.AddUserLocation(UserLocationModel(points.get(i).latitude, points.get(i).longitude, ""))

            i++ // Same as x += 1
        }

        val userdatalists = SqliteDB.ViewUserLocation()
        SqliteDB.deleteUsingLatLng(userdatalists.get(userdatalists.size - 1).lat, userdatalists.get(userdatalists.size - 1).lng)


    }


    suspend fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpsURLConnection? = null
        try {
            val url = URL(strUrl)

            urlConnection = url.openConnection() as HttpsURLConnection

            urlConnection.connect()

            iStream = urlConnection.inputStream


            val sb = iStream.bufferedReader().use(BufferedReader::readText)


            data = sb


        } catch (e: Exception) {
            CommonMethods.DebuggableLogD("Exception", e.toString())
        } finally {
            iStream?.close()
            urlConnection?.disconnect()
        }
        return data
    }


    fun showExtraFeeReasons(tiplFeeDescription: TextInputLayout) {
        val recyclerView1 = RecyclerView(this)

        val extraFeeReasonAdapter = ExtraFeeReasonAdapter(this, toll_reasons!!.extraFeeReason, object : ExtraFeeReasonAdapter.IExtraFeeReasonSelectListener {
            override fun selectedExtraFeeReason(extraFeeReason: ExtraFeeReason) {
                //   if(extraFeeDescriptionID.equalsIgnoreCase())
                //tiplFeeDescription.isErrorEnabled = false
                edtxExtraFeeDescription!!.setText(extraFeeReason.name)
                extraFeeDescriptionID = extraFeeReason.id.toString()
                if (extraFeeDescriptionID.equals("1", ignoreCase = true)) {
                    edtx_extra_fee_other_description!!.visibility = View.VISIBLE
                    tipl_extra_fee_other_description!!.visibility = View.VISIBLE
                    title!!.text = resources.getString(R.string.enter_other_reason)
                } else {
                    edtx_extra_fee_other_description!!.visibility = View.GONE
                    tipl_extra_fee_other_description!!.visibility = View.GONE
                }
                if (alertDialogStores != null) {
                    alertDialogStores!!.cancel()
                }
            }
        })



        recyclerView1.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView1.adapter = extraFeeReasonAdapter
        // loadcurrencylist(0);


        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.header, null)
        title = view.findViewById<View>(R.id.header) as TextView
        title!!.text = resources.getString(R.string.select_reason)
        alertDialogStores = android.app.AlertDialog.Builder(this)
                .setCustomTitle(view)
                .setView(recyclerView1)
                .setCancelable(true)
                .show()

    }

    private fun initProcessForEndTrip() {
        sessionManager.isDriverAndRiderAbleToChat = false
        stopFirebaseChatListenerService()
        deleteFirbaseChatNode()
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(Intent("stop-foreground-service").putExtra("foreground", "stop"))

        /*
         *  End trip API call
         */
        complexPreferences.clearSharedPreferences()
        if (isInternetAvailable) {

            user_details_lay.isEnabled = false

            if (Build.VERSION.SDK_INT > 15) {
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

                val permissionsToRequest = ArrayList<String>()
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(this@RequestAcceptActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(permission)
                    }
                }

            }
            try {

                SqliteDB.AddUserLocation(UserLocationModel(java.lang.Double.valueOf(sessionManager.currentLatitude!!), java.lang.Double.valueOf(sessionManager.currentLongitude!!), ""))


            } catch (e: Exception) {
                e.printStackTrace()
            }

            checkAllPermission()


        } else {
            //tripastatusbutton.showResultIcon(false, true);
            commonMethods.showMessage(mContext, dialog, resources.getString(R.string.no_connection))
        }
    }

    fun showBottomOTPsheet() {
        val view = layoutInflater.inflate(R.layout.otp_verification_screen_before_begin_trip, null)


        edtxOne = view.findViewById(R.id.one)
        edtxTwo = view.findViewById(R.id.two)
        edtxThree = view.findViewById(R.id.three)
        edtxFour = view.findViewById(R.id.four)
        val closePoupu = view.findViewById<TextView>(R.id.tv_close_popup)

        validateOTPToBeginTrip = view.findViewById(R.id.btn_verify_OTP)
        tvOTPErrorMessage = view.findViewById(R.id.tv_otp_error_field)
        rlEdittexts = view.findViewById(R.id.rl_edittexts)


        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(false)
        if (bottomSheetDialog.window == null) return
        /*bottomSheetDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)*/
        //bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        closePoupu.setOnClickListener { _ ->
            tripastatusbutton.showResultIcon(false, true)
            bottomSheetDialog.dismiss()
        }
        initOTPTextviewListener()
        bottomSheetDialog.show()



        validateOTPToBeginTrip.setOnClickListener {
            if (!beginTripOTP.equals("", ignoreCase = true)) {
                if (beginTripOTP.equals(tripDetailsModel.riderDetails.get(currentRiderPosition).otp!!, ignoreCase = true)) {
                    //CommonMethods.showUserMessage("Success");
                    bottomSheetDialog.dismiss()
                    //commonMethods.showProgressDialog(RequestAcceptActivity.this,customDialog);
                    //startBeginTripProgress();
                    tripastatusbutton.showResultIcon(false, true)
                    tripastatusbutton.AutoSwipe(this@RequestAcceptActivity, CommonKeys.TripDriverStatus.BeginTrip)
                } else {
                    showOTPMismatchIssue()
                }
            }
        }

        bottomSheetDialog.setOnDismissListener {
            //tripastatusbutton.showResultIcon(false, true);
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

    private fun shakeEdittexts() {
        val shake = TranslateAnimation(0f, 20f, 0f, 0f)
        shake.duration = 500
        shake.interpolator = CycleInterpolator(3f)
        rlEdittexts.startAnimation(shake)

    }


    private inner class OtpTextWatcher : TextWatcher {


        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

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
                    //edtxTwo.setSelection(1);
                }
            } else if (edtxFour.isFocused) {
                if (edtxFour.text.toString().length == 0) {
                    edtxThree.requestFocus()
                }
            }

            if (edtxOne.text.toString().trim { it <= ' ' }.length > 0 && edtxTwo.text.toString().trim { it <= ' ' }.length > 0 && edtxThree.text.toString().trim { it <= ' ' }.length > 0 && edtxFour.text.toString().trim { it <= ' ' }.length > 0) {
                beginTripOTP = edtxOne.text.toString().trim { it <= ' ' } + edtxTwo.text.toString().trim { it <= ' ' } + edtxThree.text.toString().trim { it <= ' ' } + edtxFour.text.toString().trim { it <= ' ' }
                validateOTPToBeginTrip.setBackground(ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_navy))
                validateOTPToBeginTrip.setTextColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_black))
            } else {
                beginTripOTP = ""
                validateOTPToBeginTrip.setBackground(ContextCompat.getDrawable(applicationContext, R.drawable.app_curve_button_navy_disable))
                validateOTPToBeginTrip.setTextColor(ContextCompat.getColor(applicationContext, R.color.newtaxi_app_black))
            }
            tvOTPErrorMessage.visibility = View.GONE
        }

        override fun afterTextChanged(editable: Editable) {
            DebuggableLogI("Newtaxi", "Textchange")

        }
    }

    fun showOTPMismatchIssue() {
        shakeEdittexts()
        tvOTPErrorMessage.visibility = View.VISIBLE
    }

    private inner class OtpTextBackWatcher : View.OnKeyListener {

        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            DebuggableLogD("keyview", v.id.toString() + "")
            DebuggableLogD("keycode", keyCode.toString() + "")
            DebuggableLogD("keyEvent", event.toString())
            if (keyCode == KeyEvent.KEYCODE_DEL && isDeletable) {
                when (v.id) {
                    /* case R.id.one: {
                        break;
                    }*/
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
            }
            return false
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

    private fun startBeginTripProgress() {
        beginTrip()
        try {
            /*GpsService gps_service = new GpsService();
                            gps_service.latLngList = new ArrayList<LatLng>();

                            LatLng newLatLng = new LatLng(Double.valueOf(sessionManager.getCurrentLatitude()), Double.valueOf(sessionManager.getCurrentLongitude()));
                            gps_service.latLngList.add(newLatLng);*/

            SqliteDB.AddUserLocation(UserLocationModel(java.lang.Double.valueOf(sessionManager.currentLatitude!!), java.lang.Double.valueOf(sessionManager.currentLongitude!!), ""))


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun checkAllPermission() {

        checkPermissionStatus(this, supportFragmentManager, this, STORAGEPERMISSIONARRAY, 0, 0)
    }


    private fun showGPSNotEnabledWarning() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.location_not_enabled_please_enable_location))
        builder.setCancelable(true)
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setPositiveButton(resources.getString(R.string.ok)) { _, _ -> AppUtils.openLocationEnableScreen(mContext) }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
        dialog.show()

    }

    private fun hideChatAccordingToBookingType() {
        if (sessionManager.bookingType == CommonKeys.RideBookedType.manualBooking) {
            fabChat.visibility = View.GONE
        }
    }

    private fun playNotificatinSoundAndViberate() {
        try {
            val mPlayer = MediaPlayer.create(this, R.raw.newtaxi)
            mPlayer.start()
            mPlayer.isLooping = true

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    mPlayer.stop()
                    mPlayer.release()
                }
            }, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26
                v.vibrate(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun deleteFirbaseChatNode() {
        try {
            sessionManager.clearRiderNameRatingAndProfilePicture()
            FirebaseChatHandler.deleteChatNode(sessionManager.tripId!!, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun load() {
        val view = layoutInflater.inflate(R.layout.select_navigation_app_bottomsheet, null)
        val googleMap = view.findViewById<LinearLayout>(R.id.llt_google_map)
        val wazeMap = view.findViewById<LinearLayout>(R.id.llt_waze)


        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(true)
        if (bottomSheetDialog.window == null) return
        bottomSheetDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        bottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)
        if (!bottomSheetDialog.isShowing) {
            bottomSheetDialog.show()
        }

        googleMap.setOnClickListener {
            bottomSheetDialog.dismiss()
            //cameraIntent();
            navigateViaGoogleMap()
        }

        wazeMap.setOnClickListener {
            bottomSheetDialog.dismiss()
            navigateViaWazeMap()
        }

    }


    private fun startFirebaseChatListenerService() {
        //  startService(Intent(this, FirebaseChatNotificationService::class.java))
    }

    private fun stopFirebaseChatListenerService() {
        // stopService(Intent(this, FirebaseChatNotificationService::class.java))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))


    }


    override fun onConnected(bundle: Bundle?) {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        if (!gps_enabled || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Do something for lollipop and above versions
                showPermissionDialog()
            } else {
                // do something for phones running an SDK before lollipop
                checkGPSEnable()
            }
            //  return
        }

        try {

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this)

        } catch (e: Exception) {
            e.printStackTrace()
        }


        val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient)
        if (mLastLocation != null) {
            val driverLocation = DriverLocation(mLastLocation.latitude.toString(), mLastLocation.longitude.toString())
            val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.real_time_db))
            mFirebaseDatabaseReference.child(FirebaseDbKeys.LIVE_TRACKING_NODE).child(sessionManager.tripId!!).setValue(driverLocation)
            changeMap(mLastLocation)
            acceptedRequest()

            sessionManager.currentLatitude = java.lang.Double.toString(mLastLocation.latitude)
            sessionManager.currentLongitude = java.lang.Double.toString(mLastLocation.longitude)
            sessionManager.latitude = java.lang.Double.toString(mLastLocation.latitude)
            sessionManager.longitude = java.lang.Double.toString(mLastLocation.longitude)
            CommonMethods.DebuggableLogD(TAG, "ON connected$mLastLocation")

        } else
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this)

            } catch (e: Exception) {
                e.printStackTrace()
            }


        /*
         *  Accept rider reqeust
         */

        //acceptedRequest();


    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode.equals(4) && grantResults.size > 0) {
            val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            if (mLastLocation != null) {
                changeMap(mLastLocation)
                acceptedRequest()
                sessionManager.currentLatitude = java.lang.Double.toString(mLastLocation.latitude)
                sessionManager.currentLongitude = java.lang.Double.toString(mLastLocation.longitude)
                sessionManager.latitude = java.lang.Double.toString(mLastLocation.latitude)
                sessionManager.longitude = java.lang.Double.toString(mLastLocation.longitude)
            }
        } else {
            buildGoogleApiClient()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        CommonMethods.DebuggableLogI(TAG, "Connection suspended")
        mGoogleApiClient!!.connect()
    }

    /*
     *  update driver location changed
     */
    override fun onLocationChanged(location: Location) {
        try {
            println("onLocation Changed : ")
            if (location != null) {
                sessionManager.currentLatitude = java.lang.Double.toString(location.latitude)
                sessionManager.currentLongitude = java.lang.Double.toString(location.longitude)
                sessionManager.latitude = java.lang.Double.toString(location.latitude)
                sessionManager.longitude = java.lang.Double.toString(location.longitude)
                //Toast.makeText(getActivity(), "Current speed:" + location.getSpeed(),Toast.LENGTH_SHORT).show();
                speed = location.speed


                var calculatedSpeed = 0f
                if (lastLocation != null) {
                    var elapsedTime = ((location.time - lastLocation!!.time) / 1000).toDouble() // Convert milliseconds to seconds
                    if (elapsedTime <= 0)
                        elapsedTime = 1.0

                    calculatedSpeed = (lastLocation!!.distanceTo(location) / elapsedTime).toFloat()

                    if (lastLocation!!.distanceTo(location) * 1000 < MAX_DISTANCE) {
                        return
                    }
                }
                this.lastLocation = location

                val speedcheck = if (location.hasSpeed()) location.speed else calculatedSpeed
                println("location.hasSpeed() : " + location.hasSpeed())
                println("location.speed : " + location.hasSpeed())
                println("calculatedSpeed : " + calculatedSpeed)
                println("speedcheck : " + speedcheck)
                println("speed : " + speed)
                if (!java.lang.Float.isNaN(speedcheck) && !java.lang.Float.isInfinite(speedcheck))
                    speed = (speed + speedcheck) / 2

                if (speed <= 5) speed = 5f

                // Toast.makeText(getApplicationContext(), "Current speed:" + speed,Toast.LENGTH_SHORT).show();
                CommonMethods.DebuggableLogE("Live tracking ", "On Location change")
                changeMap(location)

                if (overallDuration > 0) {
                    println("ETA LINE 1720")
                    calculateEta(location)
                }


                // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * To calculate estimated arrival time from google
     */

    private fun calculateEta(location: Location) {

        val pos = ArrayList<Int>()

        for (j in stepPointsList.indices) {

            println("step points  : distance $j " + location.distanceTo(stepPointsList.get(j).location))
            println("step points  : time $j " + stepPointsList.get(j).time)

            if (location.distanceTo(stepPointsList.get(j).location) < 30) {
                pos.add(j)
                println("position ${pos.size}")
                overallDuration = overallDuration - stepPointsList.get(j).time.toInt()
                println("OverAllTime $overallDuration steppoints ${stepPointsList.get(j).time}")
            }

        }

        for (j in pos.indices) {

            try {
                stepPointsList.removeAt(pos.get(j))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            println("position ${pos.size}")

        }

        // Add new marker to the Google Map Android API V2


        totalMins = TimeUnit.SECONDS.toMinutes(overallDuration.toLong()).toString()

        println("Overall duration : " + overallDuration)

        if (TimeUnit.SECONDS.toMinutes(overallDuration.toLong()) >= 1L) {
            val min = totalMins.toLong() % 60
            println("totalMins " + totalMins)
            println("time " + time)
            println("totalMins.toLong()+time.toInt() " + totalMins.toLong() + time.toInt())
            val tM = totalMins.toLong()
            val times = time.toInt()
            println("tM " + tM)
            println("times " + times)

            val avgtime = (tM + times) / 2

            val correctavgtime = avgtime
            println("avgtime " + avgtime)
            println("correctavgtime " + correctavgtime)
            println("ETACalculatingwithDistance " + ETACalculatingwithDistance)
            if (ETACalculatingwithDistance > 0.5) {
                if ((correctavgtime).toInt() > 1) {
                    EtaTime = correctavgtime.toString()
                    println("EtaTime " + EtaTime)
                    if (correctavgtime > 60) {

                        val hours = TimeUnit.MINUTES.toHours(correctavgtime)
                        val remainMinutes = correctavgtime - TimeUnit.HOURS.toMinutes(hours)

                        println("hours " + hours)
                        println("remainMinutes " + remainMinutes)

                        tvEta.text = """${resources.getQuantityString(R.plurals.hours, hours.toInt(), hours.toInt())}${"\n"}${resources.getQuantityString(R.plurals.minutes, remainMinutes.toInt(), remainMinutes.toInt())}"""
                        AddFirebaseDatabase().updateEtaToFirebase(EtaTime, this)

                    } else {
                        tvEta.text = resources.getQuantityString(R.plurals.minutes, correctavgtime.toInt(), correctavgtime.toInt())
                        println("tvEta ==> : " + correctavgtime.toInt())
                        AddFirebaseDatabase().updateEtaToFirebase(EtaTime, this)

                    }
                } else {
                    defaultETA()
                }
            } else {
                defaultETA()
            }
            println("Minutes to hours : " + TimeUnit.MINUTES.toHours(correctavgtime).toString())

        }
    }

    private fun defaultETA() {
        EtaTime = "1"
        tvEta.text = resources.getQuantityString(R.plurals.minutes, EtaTime.toInt(), EtaTime.toInt())
        AddFirebaseDatabase().updateEtaToFirebase(EtaTime, this)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }


    /*
     *  Google API client called
     */
    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onStart() {
        super.onStart()
        try {
            mGoogleApiClient!!.connect()
            isOnPauseCalled = false

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onStop() {
        super.onStop()
        stopTimer()
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }
    }


    /*
     *  Check play service
     */
    private fun checkPlayServices(): Boolean {
        //        code updated due to deprication, code updated by the reference of @link: https://stackoverflow.com/a/31016761/6899791
        //        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this); this is commented due to depricated
        val googleAPI = GoogleApiAvailability.getInstance()
        val resultCode = googleAPI.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(resultCode)) {
                googleAPI.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            }
            return false
        }
        return true
    }

    /*
     *  Show current location in map
     */
    private fun changeMap(location: Location) {

        CommonMethods.DebuggableLogD(TAG, "Reaching map" + mMap!!)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return
        }

        // check if map is created successfully or not
        if (mMap != null) {
            mMap!!.uiSettings.isZoomControlsEnabled = false
            mMap!!.isMyLocationEnabled = false
            mMap!!.uiSettings.isMyLocationButtonEnabled = false
            mMap!!.uiSettings.isCompassEnabled = false
            CommonMethods.DebuggableLogE("Live tracking", "langua=" + location.latitude)
            latLong = LatLng(location.latitude, location.longitude)


            println("PolylinePoints2 ${polylinepoints.size}")


            liveTracking(latLong)

            if (newLatLng == null) {
                val cameraPosition = CameraPosition.Builder()
                        .target(latLong).zoom(15f).tilt(0f).build()
                mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }

            startIntentService(location)

        } else {
            Toast.makeText(applicationContext,
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show()
        }

        //acceptedRequest();

    }

    fun navigateViaGoogleMap() {
        try {
            val pickuplatlng = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lng!!))
            val droplatlng = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lng!!))

            var latlngs: String
            if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived) || tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
                latlngs = pickuplatlng.latitude.toString() + "," + pickuplatlng.longitude.toString()
            } else {
                latlngs = droplatlng.latitude.toString() + "," + droplatlng.longitude.toString()
            }


            val currentlatlngs = sessionManager.currentLatitude + "," + sessionManager.currentLongitude


            println("Drop lat lng: " + pickuplatlng.latitude.toString() + " : " + pickuplatlng.longitude.toString() + " : " + droplatlng.latitude.toString() + " : " + droplatlng.longitude.toString())

            val gmmIntentUri = Uri.parse("http://maps.google.com/maps?saddr=$currentlatlngs&daddr=$latlngs")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
            println("googlemap URI  $gmmIntentUri")
            startActivity(mapIntent)
            // Attempt to start an activity that can handle the Intent

        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"))
                startActivity(intent)
            } catch (excep: Exception) {
                CommonMethods.showUserMessage(resources.getString(R.string.google_map_not_found_in_device))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun navigateViaWazeMap() {
        try {

            val pickuplatlng = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lng!!))
            val droplatlng = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lng!!))

            var latlngs: String
            if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived) || tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
                latlngs = pickuplatlng.latitude.toString() + "," + pickuplatlng.longitude.toString()
            } else {
                latlngs = droplatlng.latitude.toString() + "," + droplatlng.longitude.toString()
            }

            val currentlatlngs = sessionManager.currentLatitude + "," + sessionManager.currentLongitude

            val mapRequest = "https://waze.com/ul?ll=$latlngs&from$currentlatlngs&at=now&navigate=yes&zoom=17"

            val gmmIntentUri = Uri.parse(mapRequest)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.waze")
            startActivity(mapIntent)

        } catch (activityNotfoundException: ActivityNotFoundException) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"))
                startActivity(intent)
            } catch (e: Exception) {
                CommonMethods.showUserMessage(resources.getString(R.string.waze_google_map_not_found_in_device))

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected fun startIntentService(mLocation: Location) {
        val intent = Intent(this, FetchAddressIntentService::class.java)

        intent.putExtra(AppUtils.LocationConstants.RECEIVER, mResultReceiver)
        intent.putExtra(AppUtils.LocationConstants.LOCATION_DATA_EXTRA, mLocation)

        startService(intent)
    }


    /*
     *  Check GPS enable or not
     */
    fun checkGPSEnable() {
        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            if (!AppUtils.isLocationEnabled(mContext)) {
                // notify user
                val dialog = AlertDialog.Builder(mContext)
                dialog.setMessage("LOCATION AND PERMISSION not enabled!")
                dialog.setPositiveButton("Open location settings") { _, _ ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                dialog.setNegativeButton("Cancel") { _, _ ->
                    // TODO Auto-generated method stub
                }
                dialog.show()
            }
            buildGoogleApiClient()
        } else {
            buildGoogleApiClient()
            Toast.makeText(mContext, "LOCATION_AND_WRITEPERMISSION_ARRAY not supported in this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermissionDialog() {

        if (!PermissionCamer.checkPermission(mContext)) {
            // android.app.Fragment fragment=(android.app.Fragment)
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    4)
            displayLocationSettingsRequest(mContext)
        } else {
            buildGoogleApiClient()
            // displayLocationSettingsRequest(mContext);
        }
    }


    private fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build()
        googleApiClient.connect()

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = (5000 / 2).toLong()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { results ->
            val status = results.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> CommonMethods.DebuggableLogI(TAG, "All location settings are satisfied.")
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    //  CommonMethods.DebuggableLogI(TAG ,"Location settings are not satisfied. Show the user a dialog to upgrade location settings ")

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        status.startResolutionForResult(mContext as Activity, 100)
                    } catch (e: IntentSender.SendIntentException) {
                        //  CommonMethods.DebuggableLogI(TAG ,"PendingIntent unable to execute request.")
                    }

                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> CommonMethods.DebuggableLogI(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.")
                else -> {
                }
            }
        }
    }

    /*
     *  Accept rider reqeust
     */
    fun acceptedRequest() {

        mMap!!.clear()
        val pickuplatlng = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lng!!))
        val droplatlng = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lng!!))

        droplatlngTemp = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lng!!))
        if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived) || tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
            //After begin trip hide eta time
            // Creating MarkerOptions
            val pickupOptions = MarkerOptions()

            // Setting the position of the marker

            pickupOptions.position(latLong)
            pickupOptions.anchor(0.5f, 0.5f)
            pickupOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carmap))
            // Add new marker to the Google Map Android API V2
            carmarker = mMap!!.addMarker(pickupOptions)


            // Creating MarkerOptions
            dropOptions = MarkerOptions()

            // Setting the position of the marker
            dropOptions.position(pickuplatlng)
            dropOptions.icon(commonMethods.vectorToBitmap(R.drawable.app_ic_pickup_small, this))

            // Add new marker to the Google Map Android API V2
            dropMarker = mMap!!.addMarker(dropOptions)

            val builder = LatLngBounds.Builder()

            //the include method will calculate the min and max bound.
            builder.include(latLong)
            builder.include(pickuplatlng)


            newLatLng = latLong
            //Toast.makeText(getApplicationContext(),"Latlng"+latLong+" \nBearing"+targetBearing,Toast.LENGTH_SHORT).show();


            val cameraPosition = CameraPosition.Builder()
                    .target(latLong)            // Sets the center of the map to current location
                    .zoom(16.5f)                   // Sets the zoom
                    //.bearing(targetBearing)     // Sets the orientation of the camera to east
                    .tilt(0f)                   // Sets the tilt of the camera to 0 degrees
                    .build()                   // Creates a CameraPosition from the builder
            mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))


        } else {
            // Creating MarkerOptions
            val pickupOptions = MarkerOptions()

            // Setting the position of the marker
            pickupOptions.position(latLong)
            pickupOptions.anchor(0.5f, 0.5f)
            pickupOptions.draggable(true)
            pickupOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carmap))
            // Add new marker to the Google Map Android API V2
            carmarker = mMap!!.addMarker(pickupOptions)

            // Creating MarkerOptions
            dropOptions = MarkerOptions()

            // Setting the position of the marker
            dropOptions.position(droplatlng)
            dropOptions.icon(commonMethods.vectorToBitmap(R.drawable.app_ic_drop_small, this))
            //dropOptions.icon((BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ub__ic_pin_dropoff, 2, R.layout.estimation_time_layout, duration))))

            // Add new marker to the Google Map Android API V2
            dropMarker = mMap!!.addMarker(dropOptions)


            val builder = LatLngBounds.Builder()

            //the include method will calculate the min and max bound.
            builder.include(latLong)
            builder.include(droplatlng)


            newLatLng = latLong


            val cameraPosition = CameraPosition.Builder()
                    .target(latLong)            // Sets the center of the map to current location
                    .zoom(16.5f)                   // Sets the zoom
                    //.bearing(targetBearing)     // Sets the orientation of the camera to east
                    .tilt(0f)                   // Sets the tilt of the camera to 0 degrees
                    .build()                   // Creates a CameraPosition from the builder
            mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))


        }
        if (isCheckDirection || riderChanged) {
            isCheckDirection = false


            uiScope.launch {

                if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived) || tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
                    if (polylinepoints.size == 0)
                        directionModel = GetDirectionData(this@RequestAcceptActivity).directionParse(CommonKeys.DownloadTask.AcceptRequest, latLong, pickuplatlng)
                } else {
                    directionModel = GetDirectionData(this@RequestAcceptActivity).directionParse(CommonKeys.DownloadTask.AcceptRequest, latLong, droplatlng)
                }
                println("NAthiya 2224")
                withContext(Dispatchers.Main) {
                    acceptedRequestDirectionUrl(directionModel!!.totalDuration, directionModel!!.distances, directionModel!!.stepPoints, directionModel!!.overviewPolyline, directionModel!!.lineOptions, directionModel!!.points)
                }
            }

        }


    }


    /**
     * startTimer
     */
    private fun startDistanceTimer() {
        timerDirection = Timer()
        timerDirectionTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    isCheckDirection = true
                }
            }
        }
        timerDirection.schedule(timerDirectionTask, delay.toLong(), periodDirection.toLong())
    }


    /*
     *  Send Arrive now , Begin trip, end trip to rider
     */
    fun arriveRequest() {
        user_details_lay.isEnabled = false
        viewModel?.arriveNow(this)
    }

    fun beginTrip() {

        user_details_lay.isEnabled = false
        viewModel?.beginTrip(this)
    }


    /**
     * Parameters for an api call for end trip
     */

    fun endTrip(distance: Float?) {


        //removeWiget()
        sessionManager.offlineDistance = 0f
        sessionManager.onlineDistance = 0f

        sessionManager.totalDistance = 0f
        sessionManager.totalDistanceEverySec = 0f


        val imageObject = HashMap<String, String>()
        if (isTollFee) {
            CommonMethods.DebuggableLogE("toll_reason_id", extra_fee_reason)
            println("Toll_reason" + extra_fee_reason + "amount" + extraFeeAmount + "id" + extraFeeDescriptionID)
            imageObject["toll_fee"] = extraFeeAmount
            imageObject["toll_reason"] = extra_fee_reason
            imageObject["toll_reason_id"] = extraFeeDescriptionID
        }

        imageObject["trip_id"] = sessionManager.tripId!!
        imageObject["end_latitude"] = sessionManager.latitude!!
        imageObject["end_longitude"] = sessionManager.longitude!!
        imageObject["total_km"] = distance.toString()
        imageObject["token"] = sessionManager.accessToken!!
        imageObject["image"] = compressPath
        DebuggableLogE("end trip Latitude", sessionManager.latitude)
        DebuggableLogE("end trip Longitude", sessionManager.longitude)
        updateStaticMap(imageObject)

    }

    /**
     * Api calling method based on country type
     *
     * @param imageObject hash Map Datas Based on Country Type
     */
    private fun updateStaticMap(imageObject: HashMap<String, String>) {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        var file: File?
        try {
            file = File(compressPath)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

            if (compressPath != "") {
                multipartBody.addFormDataPart("image", "IMG_$timeStamp.jpg", RequestBody.create("image/png".toMediaTypeOrNull(), file))
            }


            for (key in imageObject.keys) {
                multipartBody.addFormDataPart(key, imageObject[key]!!.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        //commonMethods.showProgressDialog(this);
        val formBody = multipartBody.build()
        viewModel?.endTrip(formBody, this)

    }


    fun onSuccessArrive(jsonResp: JsonResponse) {


        currentRiderPosition = 0
        val tripDetailsModels = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)
        tripDetailsModel = tripDetailsModels
        tripStatusChanges()
        initView()
        initResume()


    }

    fun onSuccessBegin(jsonResp: JsonResponse) {
        sessionManager.totalDistance = 0f
        sessionManager.totalDistanceEverySec = 0f

        if (BuildConfig.DEBUG) {
            commonMethods.createFileAndUpdateDistance(this)
        }

        currentRiderPosition = 0
        polylinepoints.clear()
        val tripDetailsModels = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)
        tripDetailsModel = tripDetailsModels
        tripStatusChanges()
        initView()
        initResume()


        acceptedRequest()
    }


    fun onSuccessEnd(jsonResp: JsonResponse) {
        //drawStaticMap();
        CommonKeys.isTripBegin = false
        currentRiderPosition = 0
        /*if (commonMethods.isMyServiceRunning(TrackingService::class.java, this)) {
             val gpsService = Intent(applicationContext, TrackingService::class.java)
             stopService(gpsService)
         }*/
        val json = JSONObject(jsonResp.strResponse)
        val jArray = json.getJSONArray("riders")
        println("Riderslist" + jArray.length())
        if (jArray.length() > 0) {
            sessionManager.isTrip = true
        } else {
            sessionManager.isTrip = false
        }
        //sessionManager.setTripStatus("End Trip");
        try {
            sinchClient?.stopListeningOnActiveConnection()
            sinchClient?.terminateGracefully()
            stopService(Intent(this, NewTaxiSinchService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sessionManager.tripStatus = CommonKeys.TripDriverStatus.EndTrip

        if (!tripDetailsModel.isPool) {
            sessionManager.totalDistance = 0f
            sessionManager.totalDistanceEverySec = 0f
            PositionProvider.lastDistanceCalculationLocation = null
        }


        // sessionManager.isTrip=false
        val checkuser = SqliteDB.checkUser()
        if (checkuser > 0) {
            SqliteDB.DeleteUser()
            println("Delete User $checkuser")
        }

        val rating = Intent(applicationContext, Riderrating::class.java)
        rating.putExtra("imgprofile", tripDetailsModel.riderDetails.get(currentRiderPosition).profileImage)
        startActivity(rating)
        /*startActivity(new Intent(getApplicationContext(),PaymentAmountPage.class));*/

    }


    /*
     *  Show dialog for payment completed trip cancelled
     */
    fun statusDialog(message: String) {
        println("Print Message $message")
        AlertDialog.Builder(this@RequestAcceptActivity)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    val requestaccept = Intent(applicationContext, MainActivity::class.java)
                    startActivity(requestaccept)
                    finish()
                }
                .show()
    }

    /*
     *  Receive push notification
     */
    fun receivepushnotification() {

        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // checking for type intent filter
                if (intent.action == Config.REGISTRATION_COMPLETE) {
                    // FCM successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)


                } else if (intent.action == Config.PUSH_NOTIFICATION) {
                    // new push notification is received


                    val JSON_DATA = sessionManager.pushJson


                    var jsonObject: JSONObject?
                    try {
                        jsonObject = JSONObject(JSON_DATA)
                        if (jsonObject.getJSONObject("custom").has("cancel_trip")) {

                            statusDialog(resources.getString(R.string.yourtripcanceledrider))

                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }


    private fun getDateTime(): String {
        val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date()



        return dateFormat.format(date)
    }


    private fun initDriverDetails() {

        riderList.clear()
        riderList.addAll(tripDetailsModel.riderDetails)
        val adapter = DriverDetailsAdapter(this)
        adapter.setOnRiderClickListner(this)
        adapter.initRiderModel(riderList)
        rvDriverDetails.adapter = adapter
    }


    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        /*if(this::positionProvider.isInitialized){
            positionProvider.stopUpdates()
        }*/
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
        if (mRegistrationBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver!!)

    }


    private class GetFloatingIconClick : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val selfIntent = Intent(context, RequestAcceptActivity::class.java)
            selfIntent.flags = (Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(selfIntent)
        }
    }


    fun distanceCalcCorout() {
        try {
            if (SqliteDB.ViewUserLocation().isNotEmpty()) {
                positionProvider.lastStoredLocation.latitude = SqliteDB.ViewUserLocation().last().lat
                positionProvider.lastStoredLocation.longitude = SqliteDB.ViewUserLocation().last().lng
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        uiScope.launch {


            if (commonMethods.isOnline(mContext)) {


                val tripStatus = sessionManager.tripStatus

                if (tripStatus != null && tripStatus == CommonKeys.TripDriverStatus.BeginTrip) {


                    val userdatalist = SqliteDB.ViewUserLocation()

                    if (userdatalist.size > 0) {

                        val time1 = getDateTime()
                        val time2 = userdatalist.get(userdatalist.size - 1).lastTime

                        println("calcMinDiff(time1, time2) : " + calcMinDiff(time1, time2))
                        if (calcMinDiff(time1, time2) > 60) {

                            //downloadTask(CommonKeys.DownloadTask.DistCalcResume, LatLng(userdatalist.get(userdatalist.size - 1).lat, userdatalist.get(userdatalist.size - 1).lng), LatLng(java.lang.Double.valueOf(sessionManager.latitude!!), java.lang.Double.valueOf(sessionManager.longitude!!)))
                            directionModel = GetDirectionData(this@RequestAcceptActivity).directionParse(CommonKeys.DownloadTask.DistCalcResume, LatLng(userdatalist.get(userdatalist.size - 1).lat, userdatalist.get(userdatalist.size - 1).lng), LatLng(java.lang.Double.valueOf(sessionManager.latitude!!), java.lang.Double.valueOf(sessionManager.longitude!!)))
                            println("NAthiya 2558")
                            withContext(Dispatchers.Main) {
                                updateDistanceAndPoints(directionModel!!.distances, directionModel!!.points)
                            }

                        }
                    }
                }


            }


        }

    }


    override fun onResume() {
        super.onResume()
        /*if(this::positionProvider.isInitialized){
            positionProvider.startUpdates()
        }*/
        initResume()


        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val location = Location(LocationManager.GPS_PROVIDER)
                    if (intent.getStringExtra("type") == "change") {
                        count0++
                        location.latitude = intent.getDoubleExtra("Lat", 0.0)
                        location.longitude = intent.getDoubleExtra("Lng", 0.0)
                    } else if (intent.getStringExtra("type") == "Updates") {
                        location.latitude = intent.getDoubleExtra("Lat", 0.0)
                        location.longitude = intent.getDoubleExtra("Lng", 0.0)
                    } else if (intent.getStringExtra("type") == "DataBase") {
                        count1++
                        location.latitude = intent.getDoubleExtra("Lat", 0.0)
                        location.longitude = intent.getDoubleExtra("Lng", 0.0)
                        val text = intent.getDoubleExtra("Lat", 0.0).toString() + " -- " + intent.getDoubleExtra("Lng", 0.0)
                        textView1.append("\n$count1 * $text")
                    } else {
                        //count1++;
                        location.latitude = intent.getDoubleExtra("Lat", 0.0)
                        location.longitude = intent.getDoubleExtra("Lng", 0.0)

                    }
                    CommonMethods.DebuggableLogI(TAG, "MyService running...")
                    CommonMethods.DebuggableLogI(TAG, "\n" + location)
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("location_update"))


        isRequestAcceptActivity = true
        // register FCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver!!,
                IntentFilter(Config.REGISTRATION_COMPLETE))


        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver!!,
                IntentFilter(Config.PUSH_NOTIFICATION))

        // register new push message receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver!!,
                IntentFilter(Config.DISTANCE_CALCULATION))

        // clear the notification area when the app is opened

        startDistanceTimer()
    }

    private fun initResume() {

        distanceCalcCorout()
        positionProvider.requestSingleLocation()


        if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived) || tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
            AddFirebaseDatabase().UpdatePolyLinePoints("0", this)
        }

    }


    private fun calculateEtaFromPolyline(distance: String) {

        try {
            if (isEtaFromPolyline) {
                isEtaFromPolyline = false
                ETACalculatingwithDistance = distance.toDouble()


            }
            println("Distance calclulation : " + ETACalculatingwithDistance)
        } catch (e: java.lang.Exception) {

        }


    }

    private fun calcMinDiff(lastTime: String, lastTime1: String): Long {

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val date1: Date = simpleDateFormat.parse(lastTime1)
        val date2: Date = simpleDateFormat.parse(lastTime)



        println("last time : " + lastTime)
        println("last time two : " + lastTime1)


        val difference = date2.time - date1.time
        val diffInSec = TimeUnit.MILLISECONDS.toSeconds(difference)

        return diffInSec

    }


    /**
     * To stop timer
     */
    private fun stopTimer() {
        try {
            timerDirection.cancel()
            timerDirection.purge()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onPause() {
        super.onPause()
        stopTimer()
        isRequestAcceptActivity = false
        isOnPauseCalled = true
        AddFirebaseDatabase().UpdatePolyLinePoints("0", this)
    }


/* ***************************************************************** */
/*                  Animate Marker for Live Tracking                 */
/* ***************************************************************** */

/*
 *  After driver track location or route
 */

    /*
     *   After driver accept the trip update the pickup and drop route in map
     */
    fun UpdateRoute(driverlatlng: LatLng) {
        println("PolylinePoints3 ${polylinepoints.size}")
        //mMap.clear();
        val pickuplatlng = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lng!!))
        val droplatlng = LatLng(java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lat!!), java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).drop_lng!!))

        // Creating MarkerOptions
        val pickupOptions = MarkerOptions()

        // Setting the position of the marker

        if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived) || tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
            pickupOptions.position(pickuplatlng)
            pickupOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.app_ic_pickup_small))
        } else {
            pickupOptions.position(droplatlng)
            pickupOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.app_ic_drop_small))
        }
        // Add new marker to the Google Map Android API V2
        //mMap.addMarker(pickupOptions);

        // Creating MarkerOptions
        val dropOptions = MarkerOptions()

        // Setting the position of the marker
        dropOptions.position(driverlatlng)
        dropOptions.anchor(0.5f, 0.5f)
        dropOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carmap))
        // Add new marker to the Google Map Android API V2
        //carmarker = mMap.addMarker(dropOptions);

        val builder = LatLngBounds.Builder()

        //the include method will calculate the min and max bound.
        builder.include(driverlatlng)
        if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived) || tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
            builder.include(pickuplatlng)
        } else {
            builder.include(droplatlng)
        }

        val bounds = builder.build()

        val width = resources.displayMetrics.widthPixels / 2
        val height = resources.displayMetrics.heightPixels / 2
        val padding = (width * 0.08).toInt() // offset from edges of the map 10% of screen

        if (firstloop) {
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
            mMap!!.moveCamera(cu)
            firstloop = false
        }


        if (isCheckDirection) {
            println("isCheckDirection")
            isCheckDirection = false



            uiScope.launch {
                if (tripastatusbutton.text.toString() == resources.getString(R.string.confirm_arrived) || tripastatusbutton.text.toString() == resources.getString(R.string.begin_trip)) {
                    // Getting URL to the Google Directions API
                    directionModel = GetDirectionData(this@RequestAcceptActivity).directionParse(CommonKeys.DownloadTask.UpdateRoute, driverlatlng, pickuplatlng)
                    //downloadTask(CommonKeys.DownloadTask.UpdateRoute, driverlatlng, pickuplatlng)

                } else {
                    directionModel = GetDirectionData(this@RequestAcceptActivity).directionParse(CommonKeys.DownloadTask.UpdateRoute, driverlatlng, droplatlng)
                    //downloadTask(CommonKeys.DownloadTask.UpdateRoute, driverlatlng, droplatlng)
                }

                println("NAthiya 2798")
                println("NAthiya ${driverlatlng.latitude}")
                println("NAthiya ${driverlatlng.longitude}")

                println("NAthiya ${droplatlng.latitude}")
                println("NAthiya ${droplatlng.longitude}")


                withContext(Dispatchers.Main) {
                    updateRouteDirectionUrl(directionModel!!.totalDuration, directionModel!!.distances, directionModel!!.stepPoints, directionModel!!.overviewPolyline, directionModel!!.lineOptions, directionModel!!.points)
                }

            }


        }
    }

    fun liveTracking(latLng1: LatLng) {
        var isonPath: Boolean = false


        val lineOptions = PolylineOptions()
        if (movepoints.size < 1) {
            movepoints.add(0, latLng1)
            movepoints.add(1, latLng1)

        } else {
            movepoints.set(1, movepoints.get(0))
            movepoints.set(0, latLng1)
        }

        CommonMethods.DebuggableLogE("Live tracking ", "First movepoints1 " + movepoints.get(0))
        CommonMethods.DebuggableLogE("Live tracking ", "First movepoints0 " + movepoints.get(1))

        val twoDForm = DecimalFormat("#.#######")
        val dfs = DecimalFormatSymbols()
        dfs.decimalSeparator = '.'
        twoDForm.decimalFormatSymbols = dfs

        val zerolat = twoDForm.format((movepoints.get(0)).latitude)
        val zerolng = twoDForm.format((movepoints.get(0)).longitude)

        val onelat = twoDForm.format((movepoints.get(1)).latitude)
        val onelng = twoDForm.format((movepoints.get(1)).longitude)

        //if (zerolat != onelat || zerolng != onelng) {
        println("PolylinePoints1 ${polylinepoints.size}")
        if (polylinepoints.size > 0) {
            val tolerance: Double = 80.toDouble()
            isonPath = PolyUtil.isLocationOnPath(latLng1, polylinepoints, false, tolerance)
            CommonMethods.DebuggableLogE("Livetracking ", "isonpath $isonPath")
        }

        if (!isonPath) {
            UpdateRoute(movepoints.get(1))
        } else {
            println("PolylinePoints4 ${polylinepoints.size}")
            val newPoints = ArrayList<LatLng>()
            val points = polylinepoints
            val location = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = latLng1.latitude
                longitude = latLng1.longitude
            }

            var isContracted: Boolean = false
            for (j in points.indices) {

                if (!isContracted) {
                    val distance = location.distanceTo(Location(LocationManager.GPS_PROVIDER).apply {
                        latitude = points[j].latitude
                        longitude = points[j].longitude
                    })

                    if (distance < 30.toFloat()) {
                        isContracted = true
                    }
                }
                if (isContracted) {

                    newPoints.add(points[j])
                }

                /* Location.distanceBetween(latLng1.latitude,latLng1.longitude,points[j].latitude,points[j].longitude,result)
                    println("distance$distance.")*/
            }


            if (newPoints.size > 0) {

                var oldloc = newPoints.first()

                var locations = Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = oldloc.latitude
                    longitude = oldloc.longitude
                }
                var distance = 0f

                for (k in 1 until newPoints.size step 5) {
                    val newdistance = locations.distanceTo(Location(LocationManager.GPS_PROVIDER).apply {
                        latitude = newPoints[k].latitude
                        longitude = newPoints[k].longitude
                    })
                    oldloc = newPoints[k]
                    locations = Location(LocationManager.GPS_PROVIDER).apply {
                        latitude = oldloc.latitude
                        longitude = oldloc.longitude
                    }
                    distance += newdistance
                }


                try {
                    val dKM = distance / 1000
                    println("Distance per KM $dKM")
                    if (speed < 5f) speed = 5f
                    val SKPH = speed * 3.6

                    val times = dKM / SKPH
                    ETACalculatingwithDistance = String.format("%.2f", dKM).toDouble()
                    println("EtaCalculation $ETACalculatingwithDistance")

                    println("dKM $dKM")
                    println("SKPH $SKPH")
                    println("times $times")

                    time = (times * 60).toInt().toString()
                    println("time $time")

                    if ((times * 60).toInt() < 1)
                        time = "1"
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
            if (newPoints.size > 0) {
                lineOptions.addAll(newPoints)
                lineOptions.width(6f)
                lineOptions.color(ContextCompat.getColor(this@RequestAcceptActivity, R.color.newtaxi_app_navy))
                lineOptions.geodesic(true)
                polyline!!.remove()
                // polyline.points=newPoints
                polyline = mMap!!.addPolyline(lineOptions)
                newPoints.clear()
            }

        }
        CommonMethods.DebuggableLogE("Live tracking ", "zerolat$zerolat $zerolng")
        CommonMethods.DebuggableLogE("Live tracking ", "movepoints1 " + movepoints.get(0))
        CommonMethods.DebuggableLogE("Live tracking ", "movepoints0 " + movepoints.get(1))
        adddefaultMarker(movepoints.get(1), movepoints.get(0))
        samelocation = false
        // }
    }
/*
 *  Move marker
 **/

    /*
     *  Move marker for given locations
     **/
    fun adddefaultMarker(latlng: LatLng, latlng1: LatLng) {
        val startbearlocation = Location(LocationManager.GPS_PROVIDER)
        val endbearlocation = Location(LocationManager.GPS_PROVIDER)

        startbearlocation.latitude = latlng.latitude
        startbearlocation.longitude = latlng.longitude

        endbearlocation.latitude = latlng1.latitude
        endbearlocation.longitude = latlng1.longitude

        if (endbear.toDouble() != 0.0) {
            startbear = endbear
        }


        //carmarker.setPosition(latlng);
        //carmarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.carmap));
        if (!(this::carmarker.isInitialized)) {
            val pickupOptions = MarkerOptions()

            // Setting the position of the marker

            pickupOptions.position(latLong)
            pickupOptions.anchor(0.5f, 0.5f)
            pickupOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carmap))
            // Add new marker to the Google Map Android API V2
            carmarker = mMap!!.addMarker(pickupOptions)
        }
        carmarker.isFlat = true
        marker = carmarker
        // Move map while marker gone
        ensureMarkerOnBounds(latlng, "updated", startbearlocation.bearingTo(endbearlocation))

        endbear = bearing(startbearlocation, endbearlocation).toFloat()
        endbear = (endbear * (180.0 / 3.14)).toFloat()

        //double distance = Double.valueOf(twoDForm.format(startbearlocation.distanceTo(endbearlocation)));
        val distance = java.lang.Double.valueOf(startbearlocation.distanceTo(endbearlocation).toDouble())

        if (distance > 0)
            animateMarker(latlng1, marker, speed, endbear)

    }

    fun animateMarker(destination: LatLng, marker: Marker?, speed: Float, endbear: Float) {

        val newPosition = arrayOfNulls<LatLng>(1)
        if (marker != null) {
            val startPosition = marker.position
            val endPosition = LatLng(destination.latitude, destination.longitude)
            var duration: Long
            val newLoc = Location(LocationManager.GPS_PROVIDER)
            newLoc.latitude = startPosition.latitude
            newLoc.longitude = startPosition.longitude
            val prevLoc = Location(LocationManager.GPS_PROVIDER)
            prevLoc.latitude = endPosition.latitude
            prevLoc.longitude = endPosition.longitude


            //double distance = Double.valueOf(twoDForm.format(newLoc.distanceTo(prevLoc)));
            val distance = java.lang.Double.valueOf(newLoc.distanceTo(prevLoc).toDouble())

            duration = (distance / speed * 1000).toLong() - 5

            if (duration >= 1000)
                duration = 950
            duration = 1015


            val startRotation = marker.rotation

            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            if (valueAnimator != null) {
                valueAnimator!!.cancel()
                valueAnimator!!.end()
            }
            valueAnimator = ValueAnimator.ofFloat(0F, 1F)
            valueAnimator!!.duration = duration
            valueAnimator!!.interpolator = LinearInterpolator()
            valueAnimator!!.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    newPosition[0] = latLngInterpolator.interpolate(v, startPosition, endPosition)
                    newPosition[0]?.let { marker.position = it } // Move Marker
                    marker.rotation = computeRotation(v, startRotation, endbear) // Rotate Marker
                } catch (ex: Exception) {
                    // I don't care atm..
                }
            }

            valueAnimator!!.start()
        }
    }

    /*
     *  Find GPS rotate position
     **/
    private fun bearing(startPoint: Location, endPoint: Location): Double {
        val deltaLongitude = endPoint.longitude - startPoint.longitude
        val deltaLatitude = endPoint.latitude - startPoint.latitude
        val angle = 3.14 * .5f - Math.atan(deltaLatitude / deltaLongitude)

        if (deltaLongitude > 0)
            return angle
        else if (deltaLongitude < 0)
            return angle + 3.14
        else if (deltaLatitude < 0) return 3.14

        return 0.0
    }

    /*
     *  move map to center position while marker hide
     **/
    private fun ensureMarkerOnBounds(toPosition: LatLng, type: String, bearing: Float) {
        if (marker != null) {
            val currentZoomLevel = mMap!!.cameraPosition.zoom
            //val bearing = mMap!!.cameraPosition.bearing
            /*if (16.5f > currentZoomLevel) {
                currentZoomLevel = 16.5f;
            }*/
            val cameraPosition = CameraPosition.Builder()
                    .target(toPosition).zoom(currentZoomLevel).bearing(bearing).build()

            if ("updated" == type) {
                mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            } else {
                if (!mMap!!.projection.visibleRegion.latLngBounds.contains(toPosition)) {
                    mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }
    }

    /**
     * To generate static map of that particular trip
     */

    fun drawStaticMap() {
        sessionManager.isEndTripCalled = true
        val offlineDistance = sessionManager.offlineDistance
        val onlineDistance = sessionManager.onlineDistance
        //calculatedDistance = sessionManager.totalDistance / 1000

        //float exkm= (float) (normalDistance*.10);
        //normalDistance=normalDistance+exkm;
        DebuggableLogE("locationupdate", "Offline Distance:" + sessionManager.offlineDistance)
        DebuggableLogE("locationupdate", "online Distance:" + sessionManager.onlineDistance)

        val endLat = java.lang.Double.valueOf(sessionManager.currentLatitude!!)
        val endLng = java.lang.Double.valueOf(sessionManager.currentLongitude!!)

        val location = Location("")
        location.latitude = endLat
        location.longitude = endLng
        positionProvider.calculateDistance(location)


        /*var newlatLngList = ArrayList<LatLng>()
        var trip_path = ""
        var beginLat : Double
        var beginLng : Double
        if (tripDetailsModel.isPool) {
            beginLat = java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lat)
            beginLng = java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lng)
            calculatedDistance = 0f
        } else {
            beginLat = java.lang.Double.valueOf(sessionManager.beginLatitude!!)
            beginLng = java.lang.Double.valueOf(sessionManager.beginLongitude!!)
        }


        if (calculatedDistance > 0) {
            //GpsService gps_service = new GpsService();

            //   newlatLngList = routePoints //gps_service.latLngList;

            //   newlatLngList = routePoints //gps_service.latLngList;

            val userdatalist = SqliteDB.ViewUserLocation()


            println("Vimal in if  size sqlite  ${userdatalist.size} ")

            if (userdatalist.size > 0) {
            }

            if (userdatalist.size > 0) {
                for (i in userdatalist.indices) {
                    val latlng = LatLng(userdatalist[i].lat, userdatalist[i].lng)
                    newlatLngList.add(latlng)
                }
            }

            var j = 1
            if (newlatLngList.size > 100) {
                j = newlatLngList.size / 100
            }

            var i = 0
            while (i < newlatLngList.size) {
                trip_path = trip_path + "|" + newlatLngList[i].latitude + "," + newlatLngList[i].longitude
                i = i + j
            }
            pickuplatlng = LatLng(beginLat, beginLng)
            droplatlng = LatLng(newlatLngList[newlatLngList.size - 1].latitude, newlatLngList[newlatLngList.size - 1].longitude)
        } else {
            pickuplatlng = LatLng(beginLat, beginLng)
            droplatlng = LatLng(endLat, endLng)
            trip_path = "$beginLat,$beginLng|$endLat,$endLng"

        }


        println("Begin Latitude : $beginLat")
        println("Begin Longitude : $beginLng")
        println("End latittude : $endLat")
        println("End Longitude : $endLng")

        pathString = "&path=color:0x000000ff|weight:4$trip_path"
        val pickupstr = pickuplatlng.latitude.toString() + "," + pickuplatlng.longitude
        val dropstr = droplatlng.latitude.toString() + "," + droplatlng.longitude
        positionOnMap = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "pickup.png|" + pickupstr
        positionOnMap1 = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "drop.png|" + dropstr

        val start = LatLng(beginLat, beginLng)
        val end = LatLng(endLat, endLng)


        uiScope.launch {
            //downloadTask(CommonKeys.DownloadTask.EndTrip, start, end)
            directionModel = GetDirectionData(this@RequestAcceptActivity).directionParse(CommonKeys.DownloadTask.EndTrip,start,end)

            withContext(Dispatchers.Main) {
                endTripDirectionUrl(end,directionModel!!.distances, directionModel!!.overviewPolyline, directionModel!!.stepPoints, directionModel!!.totalDuration)
            }

        }*/

    }


    /**
     * to save bitmap to file android
     *
     * @param filename name of the file
     * @return returns file that contains bitmap
     */


    private fun savebitmap(filename: String, bm: Bitmap): File {
        var file: File
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val extStorageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            file = File(extStorageDirectory, "$filename.png")
            if (file.exists()) {
                file.delete()
                file = File(extStorageDirectory, "$filename.png")
                CommonMethods.DebuggableLogE("file exist", "$file,Bitmap= $filename")
            }
        } else {
            val extStorageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            file = File(extStorageDirectory, "$filename.png")
            if (file.exists()) {
                file.delete()
                file = File(extStorageDirectory, "$filename.png")
                CommonMethods.DebuggableLogE("file exist", "$file,Bitmap= $filename")
            }
        }

        var outStream: OutputStream?

        try {
            // make a new bitmap from your file BitmapFactory.decodeFile(file.getName());

            outStream = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        CommonMethods.DebuggableLogE("file", "" + file)
        return file

    }


    /*
     *   Image upload function called
     */
    protected fun imageuploading() {
        // TODO Auto-generated method stub

        try {

            CommonMethods.DebuggableLogE("Image Upload", "Newtaxi")

            var connection: HttpsURLConnection?
            var outputStream: DataOutputStream?

            val pathToOurFile = imagepath


            val baseurl = getString(R.string.apiBaseUrl)
            val urlServer = baseurl + "map_upload"


            val lineEnd = "\r\n"
            val twoHyphens = "--"
            val boundary = "*****"

            var bytesRead: Int
            var bytesAvailable: Int
            var bufferSize: Int
            val buffer: ByteArray
            val maxBufferSize = 1 * 1024 * 1024

            val fileInputStream = FileInputStream(File(pathToOurFile))

            val url = URL(urlServer)
            connection = url.openConnection() as HttpsURLConnection


            // Allow Inputs & Outputs
            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false

            // Enable POST method
            connection.requestMethod = "POST"

            connection.setRequestProperty("Connection", "Keep-Alive")
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")

            outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(twoHyphens + boundary + lineEnd)
            outputStream.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"$pathToOurFile\"$lineEnd")

            outputStream.writeBytes(lineEnd)

            bytesAvailable = fileInputStream.available()
            bufferSize = Math.min(bytesAvailable, maxBufferSize)
            buffer = ByteArray(bufferSize)

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize)

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize)
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
            }

            outputStream.writeBytes(lineEnd)

            outputStream.writeBytes(twoHyphens + boundary + lineEnd)
            outputStream.writeBytes("Content-Disposition: form-data; name=\"token\"$lineEnd")

            outputStream.writeBytes(lineEnd)
            outputStream.writeBytes(sessionManager.accessToken + lineEnd)

            outputStream.writeBytes(lineEnd)

            outputStream.writeBytes(twoHyphens + boundary + lineEnd)
            outputStream.writeBytes("Content-Disposition: form-data; name=\"trip_id\"$lineEnd")

            outputStream.writeBytes(lineEnd)
            outputStream.writeBytes(sessionManager.tripId + lineEnd)

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

            if (connection.responseCode == HttpsURLConnection.HTTP_OK) {
                val serverResponseMessage = connection.responseMessage


                fileInputStream.close()
                outputStream.flush()
                outputStream.close()

                var inputStream1: DataInputStream?
                inputStream1 = DataInputStream(connection.inputStream)
                val str = ""
                var Str1_imageurl = inputStream1.readLine()

                CommonMethods.DebuggableLogE("Debug", "Server Response $str")

                Str1_imageurl = str
                CommonMethods.DebuggableLogE("Debug", "Server Response String imageurl$str")
                inputStream1.close()


                try {
                    val user_jsonobj = JSONObject(Str1_imageurl)
                    for (i in 0 until user_jsonobj.length()) {

                        val statuscode = user_jsonobj.getString("status_code")
                        val statusmessage = user_jsonobj.getString("status_message")
                        if (statuscode.matches("1".toRegex())) {
                            //String user_thumb_image = user_jsonobj.getString("documentUrl");
                            CommonMethods.DebuggableLogD("OUTPUT 0S", user_jsonobj.toString())

                            //sessionManager.setTripStatus("End Trip");
                            sessionManager.tripStatus = CommonKeys.TripDriverStatus.EndTrip
                            sessionManager.isTrip = false

                            startActivity(Intent(applicationContext, PaymentAmountPage::class.java))
                        } else {
                            commonMethods.showMessage(mContext, dialog, statusmessage)
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                commonMethods.showMessage(mContext, dialog, resources.getString(R.string.img_failed_msg))
            }


        } catch (e: Exception) {

            e.printStackTrace()

        }

    }

    override fun permissionGranted(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {


        drawStaticMap()
    }

    override fun permissionDenied(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int) {
        tripastatusbutton.showResultIcon(false, true)
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(AppUtils.LocationConstants.RESULT_DATA_KEY)

            mAreaOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_AREA)

            mCityOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_CITY)
            mStateOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_STREET)

        }
    }

    /*
     *  upload image background task
     */
    private inner class ProgressTask(requestAcceptActivity: RequestAcceptActivity) : AsyncTask<String, Void, Boolean>() {
        private var dialog: ProgressDialog? = null
        private val context: Context

        init {
            context = requestAcceptActivity
            dialog = ProgressDialog(context)
        }

        override fun onPreExecute() {
            dialog = ProgressDialog(context)
            dialog!!.setMessage("Processing...")
            dialog!!.isIndeterminate = false
            dialog!!.setCancelable(false)
            dialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            dialog!!.show()
        }

        override fun onPostExecute(success: Boolean?) {
            if (dialog!!.isShowing) {
                dialog!!.dismiss()
            }

        }

        override fun doInBackground(vararg args: String): Boolean? {
            try {
                imageuploading()
                return true
            } catch (e: Exception) {
                CommonMethods.DebuggableLogE("Schedule", "UpdateSchedule failed", e)
                return false
            }

        }

    }

    override fun onBackPressed() {
        CommonKeys.IS_ALREADY_IN_TRIP = true
        val redirectMain = Intent(applicationContext, MainActivity::class.java)
        startActivity(redirectMain)
        finish()
    }

    override fun onAutoSwipeConfirm(tripStatus: String) {
        if (tripStatus.equals(CommonKeys.TripDriverStatus.BeginTrip, ignoreCase = true)) {
            startBeginTripProgress()
        } else {
            initProcessForEndTrip()
        }
    }


    override fun onSuccessResponse(jsonResp: LiveData<JsonResponse>) {
        jsonResp.observe(this, Observer {

            user_details_lay.isEnabled = true
            val statuscode = commonMethods.getJsonValue(jsonResp.value?.strResponse!!, "status_code", String::class.java) as String

            when (it.requestCode) {

                REQ_ARRIVE_NOW -> if (it.isSuccess) {
                    commonMethods.hideProgressDialog()
                    tripastatusbutton.showResultIcon(false, true)
                    onSuccessArrive(jsonResp.value!!)
                } else if (statuscode.equals("2")) {
                    commonMethods.hideProgressDialog()
                    cancelFunction(it.statusMsg)

                } else if (!TextUtils.isEmpty(it.statusMsg)) {
                    commonMethods.hideProgressDialog()
                    if (it.statusMsg.equals(""))
                        commonMethods.showMessage(this, dialog, it.statusMsg)
                }
                REQ_BEGIN_TRIP -> if (it.isSuccess) {
                    commonMethods.hideProgressDialog()
                    onSuccessBegin(jsonResp.value!!)
                } else if (statuscode.equals("2")) {
                    commonMethods.hideProgressDialog()
                    cancelFunction(it.statusMsg)

                } else if (!TextUtils.isEmpty(it.statusMsg)) {
                    commonMethods.hideProgressDialog()
                    commonMethods.showMessage(this, dialog, it.statusMsg)
                }
                REQ_END_TRIP -> if (it.isSuccess) {
                    commonMethods.hideProgressDialog()
                    onSuccessEnd(jsonResp.value!!)
                } else if (!TextUtils.isEmpty(it.statusMsg)) {
                    commonMethods.hideProgressDialog()


                    commonMethods.showMessage(this, dialog, it.statusMsg)
                }
                REQ_TOLL_REASON -> if (it.isSuccess) {
                    commonMethods.hideProgressDialog()
                    toll_reasons = gson.fromJson(it.strResponse, Toll_reasons::class.java)
                    println("toll_reasons" + toll_reasons!!.extraFeeReason.size)
                    //  extraFeeReasons.add(extraFeeReason);
                    //   System.out.println("Extra Fee Reason"+extraFeeReason.getName());
                } else if (!TextUtils.isEmpty(it.statusMsg)) {
                    commonMethods.hideProgressDialog()
                    commonMethods.showMessage(this, dialog, it.statusMsg)
                }
                else ->

                    if (!TextUtils.isEmpty(it.statusMsg)) {
                        commonMethods.hideProgressDialog()
                        commonMethods.showMessage(this, dialog, it.statusMsg)
                    }
            }
        })
    }

    override fun onFailureResponse(jsonResponse: LiveData<JsonResponse>) {
        commonMethods.hideProgressDialog()
    }

    fun cancelFunction(statusMsg: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(statusMsg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    dialog.dismiss()
                    sessionManager.clearTripID()
                    sessionManager.clearTripStatus()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    val requestaccept = Intent(applicationContext, MainActivity::class.java)
                    startActivity(requestaccept)
                    this.finish()

                }
        builder.create().show()
    }

    companion object {

        private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private val
                SYSTEM_ALERT_WINDOW_PERMISSION = 2084

        private val TAG = "GPS Service accept"
        var isRequestAcceptActivity = true

        private var isTollFee = false
        var isFirst: Boolean = false

        /*
     *  Rotate marker
     **/
        private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
            val normalizeEnd = end - start // rotate start to 0
            val normalizedEndAbs = (normalizeEnd + 360) % 360

            val direction = (if (normalizedEndAbs > 180) -1 else 1).toFloat() // -1 = anticlockwise, 1 = clockwise
            val rotation: Float
            if (direction > 0) {
                rotation = normalizedEndAbs
            } else {
                rotation = normalizedEndAbs - 360
            }

            val result = fraction * rotation + start
            return (result + 360) % 360
        }
    }

    override fun onPositionUpdate(position: Position?) {
        if (!isFirst) {
            val currLatLng = position?.latitude?.let { LatLng(it, position.longitude) }
            val dropLatLng = LatLng(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lat!!.toDouble(), tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lng!!.toDouble())
            currLatLng?.let {
                isFirst = true
                val updateLocations = UpdateLocations(this)
                val targetLocation = Location("") //provider name is unnecessary
                targetLocation.latitude = position.latitude
                targetLocation.longitude = position.longitude
                updateLocations.updateLocationInSession(targetLocation)
                updateLocations.updateLocationInFirebaseDB(targetLocation, 0.0, this)
                updateLocations.updateDriverLocationInGeoFire(targetLocation, 0.0, this)
                updateLocations.updateLocationFireBaseForPool(position.latitude, position.longitude)
                //moveMapAndroid(it,dropLatLng)
            }
        }
        val driverlatlng = LatLng(position!!.latitude, position.longitude)
        liveTracking(driverlatlng)
    }

    override fun onPositionError(error: Throwable?) {
        println("onPositionError ${error?.message}")
    }

    override fun onCalculatedDistanceForEndTrip(distance: Double) {
        callEndTrip()
    }

    override fun on1mDistanceUpdate(distance: Double) {
        if (BuildConfig.DEBUG) {
            if (!commonMethods.getFileWriter()) {
                commonMethods.createFileAndUpdateDistance(this)
            }
        }
        commonMethods.updateDistanceInLocal(distance)

        callEndTrip()
    }

    override fun on1mGoogleDistanceUpdate(distance: Double) {
        if (BuildConfig.DEBUG) {
            if (!commonMethods.getFileWriter()) {
                commonMethods.createFileAndUpdateDistance(this)
            }
        }
        commonMethods.updateDistanceInLocal(distance)
        callEndTrip()
    }

    private fun callEndTrip() {
        if (sessionManager.isEndTripCalled) {
            sessionManager.isEndTripCalled = false
            updatedDistance()
        }
    }

    private fun updatedDistance() {
        var newlatLngList = ArrayList<LatLng>()
        var trip_path = ""
        var beginLat: Double
        var beginLng: Double
        if (tripDetailsModel.isPool) {
            beginLat = java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lat)
            beginLng = java.lang.Double.valueOf(tripDetailsModel.riderDetails.get(currentRiderPosition).pickup_lng)
            calculatedDistance = 0f
            distanceCalculatedEvery1Sec = 0f
        } else {
            beginLat = java.lang.Double.valueOf(sessionManager.beginLatitude!!)
            beginLng = java.lang.Double.valueOf(sessionManager.beginLongitude!!)

            distanceCalculatedEvery1Sec = (sessionManager.totalDistanceEverySec / 1000)
            calculatedDistance = (sessionManager.totalDistance / 1000)

        }
        val endLat = java.lang.Double.valueOf(sessionManager.currentLatitude!!)
        val endLng = java.lang.Double.valueOf(sessionManager.currentLongitude!!)

        if (calculatedDistance > 0 || distanceCalculatedEvery1Sec > 0) {
            //GpsService gps_service = new GpsService();

            //   newlatLngList = routePoints //gps_service.latLngList;

            //   newlatLngList = routePoints //gps_service.latLngList;

            val userdatalist = SqliteDB.ViewUserLocation()


            println("size sqlite  ${userdatalist.size} ")

            if (userdatalist.size > 0) {
            }

            if (userdatalist.size > 0) {
                for (i in userdatalist.indices) {
                    val latlng = LatLng(userdatalist[i].lat, userdatalist[i].lng)
                    newlatLngList.add(latlng)
                }
            }

            var j = 1
            if (newlatLngList.size > 100) {
                j = newlatLngList.size / 100
            }

            var i = 0
            while (i < newlatLngList.size) {
                trip_path = trip_path + "|" + newlatLngList[i].latitude + "," + newlatLngList[i].longitude
                i = i + j
            }
            pickuplatlng = LatLng(beginLat, beginLng)
            droplatlng = LatLng(newlatLngList[newlatLngList.size - 1].latitude, newlatLngList[newlatLngList.size - 1].longitude)
        } else {
            pickuplatlng = LatLng(beginLat, beginLng)
            droplatlng = LatLng(endLat, endLng)
            trip_path = "$beginLat,$beginLng|$endLat,$endLng"

        }


        println("Begin Latitude : $beginLat")
        println("Begin Longitude : $beginLng")
        println("End latittude : $endLat")
        println("End Longitude : $endLng")

        pathString = "&path=color:0x000000ff|weight:4$trip_path"
        val pickupstr = pickuplatlng.latitude.toString() + "," + pickuplatlng.longitude
        val dropstr = droplatlng.latitude.toString() + "," + droplatlng.longitude
        positionOnMap = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "pickup.png|" + pickupstr
        positionOnMap1 = "&markers=size:mid|icon:" + getString(R.string.imageUrl) + "drop.png|" + dropstr

        val start = LatLng(beginLat, beginLng)
        val end = LatLng(endLat, endLng)


        uiScope.launch {
            //downloadTask(CommonKeys.DownloadTask.EndTrip, start, end)
            directionModel = GetDirectionData(this@RequestAcceptActivity).directionParse(CommonKeys.DownloadTask.EndTrip, start, end)
            withContext(Dispatchers.Main) {
                endTripDirectionUrl(directionModel!!.distances, directionModel!!.overviewPolyline, directionModel!!.stepPoints, directionModel!!.totalDuration)
            }

        }
    }

    override fun onImageCompress(filePath: String, requestBody: RequestBody?) {
        compressPath = filePath

        if (isInternetAvailable) {
            endTrip(distance)
        } else {
            commonMethods.showMessage(mContext, dialog, resources.getString(R.string.no_connection))
        }
    }

    /*@Override
public void onSwipeConfirm() {
    initProcessForEndTrip();
}*/
}
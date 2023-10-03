package com.seentechs.newtaxidriver.home.fragments

/**
 * @package com.seentechs.newtaxidriver.home.fragments
 * @subpackage fragments
 * @category HomeFragment
 * @author Seen Technologies
 *
 */

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.Context.ALARM_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.AddFirebaseDatabase
import com.seentechs.newtaxidriver.common.helper.CarTypeAdapter
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.helper.LatLngInterpolator
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.network.PermissionCamer
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums
import com.seentechs.newtaxidriver.common.util.Enums.REQ_COMMON_DATA
import com.seentechs.newtaxidriver.common.util.Enums.REQ_DRIVER_STATUS
import com.seentechs.newtaxidriver.common.util.Enums.REQ_HEAT_MAP
import com.seentechs.newtaxidriver.common.util.Enums.REQ_TRIP_DETAILS
import com.seentechs.newtaxidriver.common.util.Enums.REQ_UPDATE_ONLINE
import com.seentechs.newtaxidriver.common.util.Enums.UPDATE_DEFAULT_VEHICLE
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.google.locationmanager.PositionProvider
import com.seentechs.newtaxidriver.google.locationmanager.TrackingService
import com.seentechs.newtaxidriver.google.locationmanager.TrackingServiceListener
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.MainActivity.Companion.selectedFrag
import com.seentechs.newtaxidriver.home.datamodel.CommonData
import com.seentechs.newtaxidriver.home.datamodel.HeatMap
import com.seentechs.newtaxidriver.home.datamodel.TripDetailsModel
import com.seentechs.newtaxidriver.home.datamodel.VehiclesModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.managevehicles.DocumentDetails
import com.seentechs.newtaxidriver.home.managevehicles.ManageVehicles
import com.seentechs.newtaxidriver.home.map.AppUtils
import com.seentechs.newtaxidriver.home.service.HeatMapUpdation
import com.seentechs.newtaxidriver.trips.RequestAcceptActivity
import com.seentechs.newtaxidriver.trips.rating.PaymentAmountPage
import com.seentechs.newtaxidriver.trips.rating.Riderrating
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONArray
import org.json.JSONException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import javax.inject.Inject

/* ************************************************************
                      HomeFragment
Its used get home screen main fragment details
*************************************************************** */
class HomeFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, ServiceListener,
    CompoundButton.OnCheckedChangeListener {
    private var defaultVehiclePosition: Int = 0
    val movepoints = ArrayList<LatLng>()

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var apiService: ApiService
    var mMap: GoogleMap? = null
    lateinit var mMapView: MapView
    lateinit var mContext: Context
    var newLatLng: LatLng? = null
    lateinit var v: View
    lateinit var carmarker: Marker
    var startbear = 0f
    var endbear = 0f
    var marker: Marker? = null
    var samelocation = false
    var speed = 13f
    lateinit var handler_movemap: Handler
    var valueAnimator: ValueAnimator? = null
    lateinit var twoDForm: DecimalFormat

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    /*@BindView(R.id.switch_driverstatus)
    lateinit var switch_driverstatus: SwitchCompat
*/
    @BindView(R.id.rb_heat_map)
    lateinit var rbHeatMap: CheckBox

    /* @BindView(R.id.tv_status)
     lateinit var tvStatus: TextView*/
    internal lateinit var dialog: AlertDialog

    @BindView(R.id.tv_add_driver_prooof)
    lateinit var tv_addDriverProof: TextView

    @BindView(R.id.tv_add_vehicle)
    lateinit var tv_addVehicle: TextView

    @BindView(R.id.rtl_driver_details)
    lateinit var rtl_driver_details: RelativeLayout

    @BindView(R.id.rltSelectedCar)
    lateinit var rltSelectedcar: RelativeLayout

    @BindView(R.id.rtl_document_details)
    lateinit var rlt_documentdetails: RelativeLayout

    @BindView(R.id.btn_check_status)
    lateinit var btn_checkstatus: Button

    internal var delay = 0 // delay for 0 sec.
    internal var periodDirection = 15000 // repeat every 15 sec.
    private val handler = Handler()
    lateinit internal var addFirebaseDatabase: AddFirebaseDatabase
    var isTrip = false
    private lateinit var navController: NavController
    private lateinit var timerLocation: Timer
    private lateinit var timerLocationTask: TimerTask
    val status: HashMap<String, String>
        get() {
            val driverStatusHashMap = HashMap<String, String>()
            driverStatusHashMap["user_type"] = sessionManager.type!!
            driverStatusHashMap["token"] = sessionManager.accessToken!!
            return driverStatusHashMap
        }

    lateinit var trackingServiceListener: TrackingServiceListener

    @SuppressLint("UseRequireInsteadOfGet")
    @OnClick(R.id.tv_car_change)
    fun openCarType() {
        /*val dialogs = Intent(context, CarTypeActivity::class.java)
        startActivity(dialogs)*/

        if (isTrip) {
            commonMethods.showMessage(context, dialog, context!!.getString(R.string.changeAlert))
        } else {
            vehicleTypeList()

        }

    }

    @OnClick(R.id.tv_add_vehicle)
    fun addVehicle() {
        val intent = Intent(context, ManageVehicles::class.java)
        intent.putExtra(
            CommonKeys.Intents.DocumentDetailsIntent,
            (activity as MainActivity).driverProfileModel.driverDocuments
        );
        intent.putExtra(
            CommonKeys.Intents.VehicleDetailsIntent,
            (activity as MainActivity).driverProfileModel.vehicle
        );
        intent.putExtra("New", newVehicle);
        startActivity(intent)
    }

    @OnClick(R.id.tv_add_driver_prooof)
    fun addDriverProof() {
        val intent = Intent(context, DocumentDetails::class.java)
        intent.putExtra(
            CommonKeys.Intents.DocumentDetailsIntent,
            (activity as MainActivity).driverProfileModel.driverDocuments
        );
        intent.putExtra(
            CommonKeys.Intents.VehicleDetailsIntent,
            (activity as MainActivity).driverProfileModel.vehicle
        );
        intent.putExtra("New", false);
        startActivity(intent)
    }

    @OnClick(R.id.btn_check_status)
    fun checkStatus() {
        apiService.updateCheckStatus(status)
            .enqueue(RequestCallback(Enums.REQ_DRIVER_STATUS, this@HomeFragment))
    }

    protected var isInternetAvailable: Boolean = false
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var lastLocation: Location? = null
    private var isTriggeredFromDriverAPIErrorMessage = false

    var vehicleTypeDialog: android.app.AlertDialog? = null
    lateinit var rvVehicleType: RecyclerView
    var adapter: CarTypeAdapter? = null
    var vehicleTypeModelList: ArrayList<VehiclesModel> = ArrayList<VehiclesModel>()
    private lateinit var linearLayoutManager: LinearLayoutManager

    //    HeatmapTileProvider mProvider5Min;
    //    TileOverlay mOverlay5Min;

    internal var mProvider2Hr: HeatmapTileProvider? = null
    internal var mOverlay2Hr: TileOverlay? = null

    var newVehicle = false
    internal lateinit var broadcastReceiver: BroadcastReceiver


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_home, container, false)
        ButterKnife.bind(this, v)
        AppController.getAppComponent().inject(this)
        addFirebaseDatabase = AddFirebaseDatabase()
        isInternetAvailable = commonMethods.isOnline(context)
        mMapView = v.findViewById<View>(R.id.mapview) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.getMapAsync(this) //this is important

        mContext = container!!.context
        handler_movemap = Handler()
        dialog = commonMethods.getAlertDialog(activity!!)
        trackingServiceListener = TrackingServiceListener(activity)

        rbHeatMap.setOnCheckedChangeListener(this)
        rbHeatMap.isChecked = sessionManager.isHeatMapChecked

        //startDistanceTimer()

        initView()
        /*
         *  Request permission for barrery optimization for run service for background
         **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = context!!.packageName
            val pm = context!!.getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            /* else{
                 intent.action=Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
             }
             context!!.startActivity(intent);*/
        }



        twoDForm = DecimalFormat("#.##########")
        val dfs = DecimalFormatSymbols()
        dfs.decimalSeparator = '.'
        twoDForm.decimalFormatSymbols = dfs

        /*
         *  Request permission for location
         **/

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Do something for lollipop and above versions
            showPermissionDialog()
        } else {
            // do something for phones running an SDK before lollipop
            checkGPSEnable()
        }

        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (mOverlay2Hr != null) {
                    mOverlay2Hr!!.clearTileCache()
                    mOverlay2Hr!!.remove()
                }
                //                if (mOverlay5Min != null) {
                //                    mOverlay5Min.clearTileCache();
                //                    mOverlay5Min.remove();
                //                }
                //commonMethods.showProgressDialog((MainActivity) getActivity(), customDialog);
                apiService.heatMap(sessionManager.accessToken!!, TimeZone.getDefault().id)
                    .enqueue(RequestCallback(REQ_HEAT_MAP, this@HomeFragment))
            }
        }
        context?.let {
            LocalBroadcastManager.getInstance(it)
                .registerReceiver(broadcastReceiver, IntentFilter("HeatMapTimer"))
        }

        return v
    }

    private fun initView() {
        /*  switch_driverstatus.switchPadding = 40
          switch_driverstatus.setOnCheckedChangeListener(this)

          if (sessionManager.driverSignupStatus == "pending") {

              switch_driverstatus.visibility = View.GONE

          }
          *//*
         * Set driver status
         *//*
        if (sessionManager.driverStatus == "Online") {

            switch_driverstatus.isChecked = true
            sessionManager.driverStatus = "Online"
        } else {
            switch_driverstatus.isChecked = false
            sessionManager.driverStatus = "Offline"
        }*/

        /*  println("driver status"+ arguments?.get("status"))
         // if(arguments["stat"])
          val status:String= arguments?.get("status") as String
          if(status?.equals("1"))
          {
              rltSelectedcar.visibility = View.VISIBLE
              rlt_documentdetails.visibility=View.GONE
          }else
          {
              rltSelectedcar.visibility = View.GONE
              rlt_documentdetails.visibility=View.VISIBLE
          }*/
        val animation =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_bottom_up)
        rtl_driver_details.layoutAnimation = animation

        //  navController = findNavController(activity as MainActivity,R.id.navHostFragment)

    }

    /*
     *  Show location permission dialog
     **/
    private fun showPermissionDialog() {
        println("Permission check " + !PermissionCamer.checkPermission(mContext))
        if (!PermissionCamer.checkPermission(mContext)) {
            // android.app.Fragment fragment=(android.app.Fragment)
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                4
            )
            //displayLocationSettingsRequest(mContext)

        } else {
            buildGoogleApiClient()
            displayLocationSettingsRequest(mContext);
        }
    }

    private fun requestLocationPermission(): Array<String> {
        val foreground = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (foreground) {
            val background = ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!background) {
                return arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        } else {
            return arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        return arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }


    // Load currency list deatils in dialog
    fun vehicleTypeList() {

        initRecyclerView()
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.app_select_vehicle_header, null)
        val buttonView = inflater.inflate(R.layout.manage_button, null)
        val btn_manageVehicle = buttonView.findViewById<Button>(R.id.btn_manage_vehicle)
        val btn_new = buttonView.findViewById<Button>(R.id.btn_new)
        val ll = LinearLayout(activity)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        layoutParams.setMargins(10, 10, 10, 10);
        //ll.background = context?.let { ContextCompat.getDrawable(it,R.drawable.app_curve_button_white) }
        ll.setBackgroundResource(R.drawable.app_curve_button_white)
        ll.setOrientation(LinearLayout.VERTICAL)
        ll.addView(rvVehicleType)
        ll.addView(buttonView, layoutParams)
        val parent = view.parent as ViewGroup?
        if (parent != null) {
            parent.removeView(view)
        }

        val laydir = getString(R.string.layout_direction)
        if ("1" == laydir)
            ll.rotationY = 180f
        btn_manageVehicle.setOnClickListener(View.OnClickListener {
            vehicleTypeDialog?.dismiss()
            val intent = Intent(activity, ManageVehicles::class.java)
            intent.putExtra(
                CommonKeys.Intents.DocumentDetailsIntent,
                (activity as MainActivity).driverProfileModel.driverDocuments
            );
            intent.putExtra(
                CommonKeys.Intents.VehicleDetailsIntent,
                (activity as MainActivity).driverProfileModel.vehicle
            );
            intent.putExtra("New", false);
            startActivity(intent)
        })

        btn_new.setOnClickListener(View.OnClickListener {
            vehicleTypeDialog?.dismiss()
            val intent = Intent(activity, ManageVehicles::class.java)
            intent.putExtra(
                CommonKeys.Intents.DocumentDetailsIntent,
                (activity as MainActivity).driverProfileModel.driverDocuments
            );
            intent.putExtra(
                CommonKeys.Intents.VehicleDetailsIntent,
                (activity as MainActivity).driverProfileModel.vehicle
            );
            intent.putExtra("New", true);
            startActivity(intent)
            /* val fragment =AddVehicleFragment()
               val transaction = (activity as MainActivity).supportFragmentManager.beginTransaction()
               transaction.replace(R.id.frame_layout,fragment)
               transaction.commit()*/
        })
        val header = view.findViewById<TextView>(R.id.header)
        header.text = context?.resources?.getString(R.string.select_your_vehicle)
        vehicleTypeDialog =
            android.app.AlertDialog.Builder(activity).setCustomTitle(view).setView(rvVehicleType)
                .setView(ll).setCancelable(true).show()
    }


    @SuppressLint("UseRequireInsteadOfGet")
    fun initRecyclerView() {
        rvVehicleType = RecyclerView(activity!!)
        //vehicleTypeModelList = (activity as MainActivity).driverProfileModel.vehicle
        adapter = CarTypeAdapter(context!!, vehicleTypeModelList)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvVehicleType.layoutManager = linearLayoutManager
        rvVehicleType.adapter = adapter
        adapter!!.notifyDataSetChanged()
        val laydir = getString(R.string.layout_direction)
        if ("1" == laydir)
            rvVehicleType.rotationY = 180f


        adapter?.setOnVehicleClickListner(object : CarTypeAdapter.onVehicleClickListener {
            override fun setVehicleClick(vehicleTypeMode: VehiclesModel, position: Int) {
                defaultVehiclePosition = position
                vehicleTypeDialog?.dismiss()
                setDefaultVehicle()
            }
        })
    }

    public fun updateUI(position: Int) {
        val defaultvehicle = (activity as MainActivity).driverProfileModel.vehicle.get(position)
        tv_car.setText(defaultvehicle.licenseNumber)
        tv_vehicle_type.setText(defaultvehicle.vehicleName)
        // Picasso.with(context).load((activity as MainActivity).driverProfileModel.profileImage).into(ivCar)
        // adapter?.notifyDataSetChanged()
    }

    private fun setDefaultVehicle() {
        apiService.updateDefaultVehicle(
            (activity as MainActivity).driverProfileModel.vehicle.get(
                defaultVehiclePosition
            ).id, sessionManager.accessToken!!
        ).enqueue(RequestCallback(UPDATE_DEFAULT_VEHICLE, this))

    }

    fun deselectSelectVehicles() {

        var position = 0
        while (position < vehicleTypeModelList.size) {
            vehicleTypeModelList.get(position).isDefault = "0"
            (activity as MainActivity).driverProfileModel.vehicle.get(position).isDefault = "0"
            position++
        }
    }

    /*
     *  Request for location permission
     **/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            4 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient()
                    displayLocationSettingsRequest(mContext)

                }
                /*val hasForegroundLocationPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                if (hasForegroundLocationPermission) {
                    val hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasBackgroundLocationPermission) {
                        // handle location update
                    } else {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), Constants.REQUEST_CODE_BACKGROUND)
                    }
                }*/
            }

            1220 -> {
                val backgroundLocationPermission = ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                println("backgroundLocationPermission " + backgroundLocationPermission)
            }
            else -> {
            }
        }

    }

    /*
     *  Check location permission enable or not and show dialog
     **/
    fun checkGPSEnable() {
        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            if (!AppUtils.isLocationEnabled(mContext)) {
                // notify user
                val dialog = AlertDialog.Builder(mContext)
                dialog.setMessage(R.string.location_not_enabled)
                dialog.setPositiveButton(R.string.location_settings) { _, _ ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                dialog.setNegativeButton(R.string.cancel) { _, _ ->
                    // TODO Auto-generated method stub
                }
                dialog.show()
                buildGoogleApiClient()
            } else {
                buildGoogleApiClient()
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        //CommonMethods.DebuggableLogV("Locale", "locale==" + resources.configuration.locale)
        CommonMethods.DebuggableLogV("Locale", "locale==" + Locale.ENGLISH)
        mMap = googleMap


        if (ActivityCompat.checkSelfPermission(
                this.mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap!!.isMyLocationEnabled = false
        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        mMap!!.uiSettings.isCompassEnabled = false

        // Customise the styling of the base map using a JSON object defined
        // in a raw resource file.
        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_style))
    }

    private fun addHeatMap(req2Hour: List<LatLng>) {

        if (!req2Hour.isEmpty()) {

            if (mProvider2Hr == null) {

                // Create a heat map tile provider, passing it the latlngs of the police stations.
                mProvider2Hr = HeatmapTileProvider.Builder()
                    .data(Objects.requireNonNull(req2Hour))
                    .gradient(ALT_HEATMAP_GRADIENT_2HR)
                    .build()

                // Add a tile overlay to the map, using the heat map tile provider.
                mOverlay2Hr = mMap!!.addTileOverlay(TileOverlayOptions().tileProvider(mProvider2Hr))

            } else {
                mProvider2Hr!!.setData(req2Hour)
                mOverlay2Hr!!.clearTileCache()
                mOverlay2Hr!!.remove()
                mOverlay2Hr = mMap!!.addTileOverlay(TileOverlayOptions().tileProvider(mProvider2Hr))
            }
        } else if (mProvider2Hr != null) {
            mOverlay2Hr!!.clearTileCache()
            mOverlay2Hr!!.remove()

        }
        //        if (!req5min.isEmpty()) {
        ////            addHeatMap5Min(req5min);
        //        }


    }

    //    private void addHeatMap5Min(List<LatLng> req5min) {
    //
    //        if (!req5min.isEmpty()) {
    //
    //            if (mProvider5Min == null) {
    //
    //                // Create a heat map tile provider, passing it the latlngs of the police stations.
    //                mProvider5Min = new HeatmapTileProvider.Builder()
    //                        .data(Objects.requireNonNull(req5min))
    //                        .gradient(ALT_HEATMAP_GRADIENT_5MIN)
    //                        .build();
    //
    //                // Add a tile overlay to the map, using the heat map tile provider.
    //                mOverlay5Min = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider5Min));
    //
    //            } else {
    //                mProvider5Min.setData(req5min);
    //                mOverlay5Min.clearTileCache();
    //                mOverlay5Min.remove();
    //                mOverlay5Min = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider5Min));
    //            }
    //        } else if (mProvider5Min != null) {
    //            mOverlay5Min.clearTileCache();
    //            mOverlay5Min.remove();
    //        }
    //    }


    @Throws(JSONException::class)
    private fun readItems(resource: Int): ArrayList<LatLng> {
        val list = ArrayList<LatLng>()
        val inputStream = resources.openRawResource(resource)
        val json = Scanner(inputStream).useDelimiter("\\A").next()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val `object` = array.getJSONObject(i)
            val lat = `object`.getDouble("lat")
            val lng = `object`.getDouble("lng")
            list.add(LatLng(lat, lng))
        }
        return list
    }

    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        try {

            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }


        val fusedLocationClient =
            activity?.let { LocationServices.getFusedLocationProviderClient(it) }



        fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            // location?.let { it: Location ->

            val mLastLocation = location



            if (mLastLocation != null) {
                println("mLastLocation : " + mLastLocation.latitude + ":" + mLastLocation.longitude)
                if (sessionManager.driverStatus == "Online") {

                }

                changeMap(mLastLocation)

                CommonMethods.DebuggableLogD(TAG, "ON connected")

            }
            //}
        }?.addOnFailureListener { e: Exception ->
            println("Error stackTrace : " + e.printStackTrace())
            println("Error message : " + e.message)
            println("Error toString : " + e.toString())

        }


    }


    /*
     *  Update location on Change
     **/

    override fun onConnectionSuspended(i: Int) {
        CommonMethods.DebuggableLogI(TAG, "Connection suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onLocationChanged(location: Location?) {
        try {
            if (location != null) {
                sessionManager.currentLatitude = java.lang.Double.toString(location.latitude)
                sessionManager.currentLongitude = java.lang.Double.toString(location.longitude)
                //Toast.makeText(getActivity(), "Current speed:" + location.getSpeed(),Toast.LENGTH_SHORT).show();
                speed = location.speed

                var calculatedSpeed = 0f
                if (lastLocation != null) {
                    var elapsedTime =
                        ((location.time - lastLocation!!.time) / 1000).toDouble() // Convert milliseconds to seconds
                    if (elapsedTime <= 0)
                        elapsedTime = 1.0
                    calculatedSpeed = (lastLocation!!.distanceTo(location) / elapsedTime).toFloat()
                }
                this.lastLocation = location

                val speedcheck = if (location.hasSpeed()) location.speed else calculatedSpeed
                if (!java.lang.Float.isNaN(speedcheck) && !java.lang.Float.isInfinite(speedcheck))
                    speed = speedcheck

                if (speed <= 0)
                    speed = 10f

                changeMap(location)

                /* sessionManager.currentLatitude = java.lang.Double.toString(location.latitude)
                 sessionManager.currentLongitude = java.lang.Double.toString(location.longitude)*/
                if (sessionManager.driverStatus == "Online") {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    /*
     *  Update Google connection
     **/

    override fun onConnectionFailed(connectionResult: ConnectionResult) {


    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this.mContext)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        try {
            mGoogleApiClient!!.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onStart() {
        super.onStart()
        try {
            mGoogleApiClient!!.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onStop() {
        super.onStop()

        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }
    }

    /*
     *  Check Google play service enable or not
     **/
    private fun checkPlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.mContext)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(
                    resultCode, this.activity,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                ).show()
            }
            return false
        }
        return true
    }

    /*
     *  Get driver current location in map
     **/
    private fun changeMap(location: Location) {


        if (ActivityCompat.checkSelfPermission(
                this.mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        // check if map is created successfully or not
        if (mMap != null) {
            //mMap.clear();
            val latLong: LatLng

            mMap!!.uiSettings.isZoomControlsEnabled = false
            mMap!!.isMyLocationEnabled = false
            mMap!!.uiSettings.isMyLocationButtonEnabled = false
            mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_style))

            latLong = LatLng(location.latitude, location.longitude)

            if (newLatLng == null) {


                val cameraPosition = CameraPosition.Builder()
                    .target(latLong).zoom(16.5f).build()
                mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                val pickupOptions = MarkerOptions()

                // Setting the position of the marker
                pickupOptions.position(latLong)
                pickupOptions.anchor(0.5f, 0.5f)


                pickupOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carmap))
                // Add new marker to the Google Map Android API V2
                carmarker = mMap!!.addMarker(pickupOptions)
                //carmarker.setRotation((float) (startbear * (180.0 / 3.14)));
                //carmarker.setFlat(true);


                println("Camera Position : one")

                callIncompleteTripDetailsAPI()

            }



            moveMarker(latLong)

            newLatLng = latLong


            val longitude = location.longitude
            val latitude = location.latitude

            val log = longitude.toString()
            val lat = latitude.toString()
            sessionManager.latitude = lat
            sessionManager.longitude = log
        }
    }


    override fun onResume() {
        super.onResume()
        mMapView.onResume()

        apiService.commonData(sessionManager.accessToken!!)
            .enqueue(RequestCallback(REQ_COMMON_DATA, this))
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver) }
        mMapView.onDestroy()
        //  LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }


    /*
     *  Show location request for setting page
     **/
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

        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { results ->
            val status = results.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> CommonMethods.DebuggableLogI(
                    TAG,
                    "All location settings are satisfied."
                )
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    CommonMethods.DebuggableLogI(
                        TAG,
                        "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                    )

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        status.startResolutionForResult(
                            mContext as Activity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        CommonMethods.DebuggableLogI(
                            TAG,
                            "PendingIntent unable to execute request."
                        )
                    }

                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> CommonMethods.DebuggableLogI(
                    TAG,
                    "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                )
                else -> {
                }
            }
        }
    }

    /* ***************************************************************** */
    /*                  Animate Marker for Live Tracking                 */
    /* ***************************************************************** */

    /*
     *  Check service is running or not
     **/
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /*
     *  Move marker for given location(Live tracking)
     **/
    fun moveMarker(latLng: LatLng) {

        if (movepoints.size < 1) {
            movepoints.add(0, latLng)
            movepoints.add(1, latLng)

        } else {
            movepoints.set(1, movepoints.get(0))
            movepoints.set(0, latLng)
        }

        val twoDForm = DecimalFormat("#.#######")
        val dfs = DecimalFormatSymbols()
        dfs.decimalSeparator = '.'
        twoDForm.decimalFormatSymbols = dfs

        val zerolat = twoDForm.format((movepoints.get(0)).latitude)
        val zerolng = twoDForm.format((movepoints.get(0)).longitude)

        val onelat = twoDForm.format((movepoints.get(1)).latitude)
        val onelng = twoDForm.format((movepoints.get(1)).longitude)

        if (zerolat != onelat || zerolng != onelng) {
            adddefaultMarker(movepoints.get(1), movepoints.get(0))
            samelocation = false
        }
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
        carmarker.isFlat = true
        carmarker.setAnchor(0.5f, 0.5f)
        marker = carmarker
        // Move map while marker gone
        ensureMarkerOnBounds(latlng, "updated", startbearlocation.bearingTo(endbearlocation))

        endbear = bearing(startbearlocation, endbearlocation).toFloat()
        endbear = (endbear * (180.0 / 3.14)).toFloat()
        CommonMethods.DebuggableLogV(
            "float",
            "doublehain" + startbearlocation.distanceTo(endbearlocation)
        )
        //double distance = Double.valueOf(twoDForm.format(startbearlocation.distanceTo(endbearlocation)));
        val distance =
            java.lang.Double.valueOf(startbearlocation.distanceTo(endbearlocation).toDouble())

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


            // double distance = Double.valueOf(twoDForm.format(newLoc.distanceTo(prevLoc)));
            val distance = java.lang.Double.valueOf(newLoc.distanceTo(prevLoc).toDouble())

            duration = (distance / speed * 1015).toLong()

            if (duration >= 1000)
                duration = 950
            duration = 1015

            val startRotation = marker.rotation

            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            if (valueAnimator != null) {
                valueAnimator!!.cancel()
                valueAnimator!!.end()
            }
            valueAnimator = ValueAnimator.ofFloat(0F, 1f)
            valueAnimator!!.duration = duration
            valueAnimator!!.interpolator = LinearInterpolator()
            valueAnimator!!.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    newPosition[0] = latLngInterpolator.interpolate(v, startPosition, endPosition)
                    marker.position = newPosition[0]!! // Move Marker
                    marker.setAnchor(0.5f, 0.5f)
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
        var bearingUpdated = 0f
        if (marker != null) {
            val currentZoomLevel = mMap!!.cameraPosition.zoom
            if (sessionManager.isTrip) {
                bearingUpdated = bearing
            } else {
                bearingUpdated = mMap!!.cameraPosition.bearing
            }
            /*if (16.5f > currentZoomLevel) {
                currentZoomLevel = 16.5f;
            }*/
            val cameraPosition = CameraPosition.Builder()
                .target(toPosition).zoom(currentZoomLevel).bearing(bearingUpdated).build()

            if (!rbHeatMap.isChecked) {
                if ("updated" == type) {
                    mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                } else {
                    if (!mMap!!.projection.visibleRegion.latLngBounds.contains(toPosition)) {
                        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    }
                }
            }

        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun onSuccessCarDetails(jsonResp: JsonResponse) {

        val heatMap = gson.fromJson(jsonResp.strResponse, HeatMap::class.java)
        if (heatMap != null) {
            val req2Hour = ArrayList<LatLng>()
            try {

                if (heatMap.heat_map_data != null) {
                    for (j in 0 until heatMap.heat_map_data!!.size) {
                        req2Hour.add(
                            LatLng(
                                java.lang.Double.parseDouble(heatMap.heat_map_data!![j].latitude!!),
                                java.lang.Double.parseDouble(heatMap.heat_map_data!![j].longitude!!)
                            )
                        )
                    }
                }

                //                if (req2Hour.isEmpty()) {
                //                    req2Hour = readItems(R.raw.trio1);
                //                }
                //                if (req5min.isEmpty()) {
                //                    req5min = readItems(R.raw.trio);
                //                }

            } catch (e: Exception) {
                Toast.makeText(
                    activity,
                    activity!!.resources.getString(R.string.er_read_loc),
                    Toast.LENGTH_LONG
                ).show()
            }


            addHeatMap(req2Hour)
        }

    }

    private fun onSuccessCommonData(jsonResp: JsonResponse) {
        try {
            try {
                sessionManager.payementModeWebView = commonMethods.getJsonValue(
                    jsonResp.strResponse,
                    "is_web_payment",
                    String::class.java
                ) as Boolean
            } catch (e: Exception) {
                sessionManager.payementModeWebView = false
                e.printStackTrace()
            }

            sessionManager.sinchKey = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "sinch_key",
                String::class.java
            ) as String
            sessionManager.sinchSecret = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "sinch_secret_key",
                String::class.java
            ) as String

            //sessionManager.googleMapKey = commonMethods.getJsonValue(jsonResp.strResponse, "google_map_key", String::class.java) as String

            sessionManager.googleMapKey = resources.getString(R.string.google_key_url)

            sessionManager.stripePublishKey = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "stripe_publish_key",
                String::class.java
            ) as String
            //sessionManager.paypal_mode = commonMethods.getJsonValue(jsonResp.strResponse, "paypal_mode", String::class.java) as Int
            //sessionManager.paypal_app_id = commonMethods.getJsonValue(jsonResp.strResponse, "paypal_client", String::class.java) as String
            sessionManager.cardBrand = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "brand",
                String::class.java
            ) as String
            sessionManager.cardValue = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "last4",
                String::class.java
            ) as String
            sessionManager.firebaseCustomToken = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "firebase_token",
                String::class.java
            ) as String
            commonMethods.initStripeData(this.mContext)
            //val sinchKey = commonMethods.getJsonValue(jsonResp.strResponse, "sinch_key", String::class.java) as String
            //val sinchSecret = commonMethods.getJsonValue(jsonResp.strResponse, "sinch_secret_key", String::class.java) as String
            sessionManager.isExtraFeeCollectable = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "apply_trip_extra_fee",
                Boolean::class.java
            ) as Boolean
            //initSinchClient(sinchKey, sinchSecret);

            val commonData = gson.fromJson(jsonResp.strResponse, CommonData::class.java)
            if (commonData != null) {
                sessionManager.payementModeWebView = commonData.isWebPaymentEnable
                if (commonData.status.equals("pending", ignoreCase = true)) {
                    updateActInActStatus(true)
                } else {
                    updateActInActStatus(false)
                }
                if (commonData.heatMap!!.equals("1", ignoreCase = true)) {
                    //rbHeatMap.setChecked(false);
                    rbHeatMap.visibility = View.VISIBLE
                } else {
                    rbHeatMap.visibility = View.GONE
                    rbHeatMap.isChecked = false
                    sessionManager.isHeatMapChecked = false
                }
            } else {
                rbHeatMap.visibility = View.GONE
                rbHeatMap.isChecked = false
                sessionManager.isHeatMapChecked = false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        signinFirebase()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun signinFirebase() {
        val auth = Firebase.auth
        sessionManager.firebaseCustomToken?.let {
            if (it.isNotEmpty()) {
                auth.signInWithCustomToken(it)
                    .addOnCompleteListener(activity!!) { task ->
                        if (task.isSuccessful) {
                            sessionManager.isFirebaseTokenUpdated = true
                            println("signInWithCustomToken:Success")
                        } else {
                            println("signInWithCustomToken:failure" + task.exception)
                        }
                    }
            } else {
                println("firebaseCustomToken: Empty")
            }
        }

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(mContext, dialog, data)
            return
        }
        when (jsonResp.requestCode) {

            REQ_COMMON_DATA -> if (jsonResp.isSuccess) {
                onSuccessCommonData(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(mContext, dialog, jsonResp.statusMsg)
            }
            REQ_DRIVER_STATUS -> if (jsonResp.isSuccess) {
                onSuccessDriverStatus(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(mContext, dialog, jsonResp.statusMsg)
            }

            UPDATE_DEFAULT_VEHICLE -> if (jsonResp.isSuccess) {
                onSuccessDefaultVehicle(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(mContext, dialog, jsonResp.statusMsg)

            }

            REQ_UPDATE_ONLINE -> {
                commonMethods.hideProgressDialog()
                if (jsonResp.isSuccess) {
                    onSuccessUpdateOnline()

                } else if (!TextUtils.isEmpty(jsonResp.statusMsg) && jsonResp.statusMsg == "Please Complete your current trip") {
                    val onlinetext = (activity as MainActivity).tvStatus.text.toString()
                    if (onlinetext == resources.getString(R.string.offline)) {
                        (activity as MainActivity).dialogfunction2(jsonResp.statusMsg)
                        isTriggeredFromDriverAPIErrorMessage = true
                        (activity as MainActivity).tvStatus.text =
                            resources.getString(R.string.online)
                        sessionManager.driverStatus = "Online"
                    }
                    if (!commonMethods.isMyServiceRunning(TrackingService::class.java, mContext)) {
                        trackingServiceListener.startTrackingService(true, true)
                    }
                }
            }
            REQ_TRIP_DETAILS -> {
                commonMethods.hideProgressDialog()
                sessionManager.isTrip = false
                if (jsonResp.isSuccess) {
                    onSuccessIncompleteTripDetails(jsonResp)
                } else {
                    sessionManager.poolIds = ""
                    sessionManager.totalDistance = 0f
                    sessionManager.totalDistanceEverySec = 0f
                    PositionProvider.lastDistanceCalculationLocation = null
                    println("Distance emptied : ")
                }
            }
            REQ_HEAT_MAP -> if (jsonResp.isSuccess) {
                onSuccessCarDetails(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(mContext, dialog, jsonResp.statusMsg)
            }
        }
    }

    private fun onSuccessDriverStatus(jsonResp: JsonResponse) {
        try {
            val driver_status = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "driver_status",
                Int::class.java
            ) as Int
            val driverStatusMessage = commonMethods.getJsonValue(
                jsonResp.strResponse,
                "driver_status_message",
                String::class.java
            ) as String
            if (driver_status == 1) {
                rltSelectedcar.visibility = View.VISIBLE
                rlt_documentdetails.visibility = View.GONE
                (activity as MainActivity).setOnlineVisbility(0)
                (activity as MainActivity).getDriverProfile()
            } else {
                rltSelectedcar.visibility = View.GONE
                rlt_documentdetails.visibility = View.VISIBLE
                (activity as MainActivity).setOnlineVisbility(8)
                showDialog(driverStatusMessage)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    /*
 *    Show dialog like arrive now push notification
 */
    fun showDialog(message: String) {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.addphoto_header, null)
        val tit = view.findViewById<View>(R.id.header) as TextView
        tit.text = message
        val builder = android.app.AlertDialog.Builder(context)
        builder.setCustomTitle(view)
        builder.setTitle(message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { dialog, which ->
                dialog.dismiss()
            }

            .show()
    }

    private fun onSuccessDefaultVehicle(jsonResp: JsonResponse) {
        deselectSelectVehicles()
        vehicleTypeModelList.get(defaultVehiclePosition).isDefault = "1"
        (activity as MainActivity).driverProfileModel.vehicle.get(defaultVehiclePosition).isDefault =
            "1"
        (activity as MainActivity).getDriverProfile()
        // updateUI(defaultVehiclePosition)
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelTimer()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun onSuccessUpdateOnline() {
        if (sessionManager.driverStatus == "Offline") {
            addFirebaseDatabase.removeDriverFromGeofire(context!!)
            trackingServiceListener.stopTrackingService()
        } else {
            if (!commonMethods.isMyServiceRunning(TrackingService::class.java, mContext)) {
                trackingServiceListener.startTrackingService(true, true)
            }
        }

    }


    private fun onSuccessIncompleteTripDetails(jsonResp: JsonResponse) {
        (activity as MainActivity).labled_switch.isOn = true
        if (sessionManager.driverStatus.equals("Offline", true)) {
            sessionManager.driverStatus = "Online"
            (activity as MainActivity).updateOnlineStatus()
        }
        val tripDetailsModel = gson.fromJson(jsonResp.strResponse, TripDetailsModel::class.java)
        val tripStatus = tripDetailsModel.riderDetails.get(0).status
        sessionManager.tripId = tripDetailsModel.riderDetails.get(0).tripId!!.toString()
        sessionManager.bookingType = tripDetailsModel.riderDetails.get(0).bookingType.toString()
        sessionManager.beginLatitude = tripDetailsModel.riderDetails.get(0).pickup_lat
        sessionManager.beginLongitude = tripDetailsModel.riderDetails.get(0).pickup_lng
        val invoiceModels = tripDetailsModel.riderDetails.get(0).invoice
        // sessionManager.paymentMethod = tripDetailsModel.paymentMode

        var poolIDs = ""
        sessionManager.poolIds = ""
        for (i in tripDetailsModel.riderDetails.indices) {
            if (tripDetailsModel.isPool) {
                poolIDs = if (poolIDs.isNotEmpty()) {
                    poolIDs + "," + tripDetailsModel.riderDetails.get(i).tripId
                } else {
                    tripDetailsModel.riderDetails.get(i).tripId!!
                }
            }
        }
        sessionManager.poolIds = poolIDs
        // Pass different data based on trip status
        /*  sessionManager.isPool=tripDetailsModel.isPool
          if(!sessionManager.isPool)
          {
              AddFirebaseDatabase().removeDriverFromGeofire(context!!)
          }*/
        isTrip = false
        if (CommonKeys.TripStatus.Scheduled == tripStatus || CommonKeys.TripStatus.Begin_Trip == tripStatus || CommonKeys.TripStatus.End_Trip == tripStatus) {
            val requstreceivepage = Intent(mContext, RequestAcceptActivity::class.java)
            requstreceivepage.putExtra("riderDetails", tripDetailsModel)
            commonMethods.hideProgressDialog()
            if (CommonKeys.TripStatus.Scheduled == tripStatus) {
                isTrip = true
                sessionManager.isTrip = true
                //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
                sessionManager.subTripStatus = resources.getString(R.string.confirm_arrived)
                CommonKeys.isTripBegin = false
                requstreceivepage.putExtra("isTripBegin", false)
                requstreceivepage.putExtra(
                    "tripstatus",
                    resources.getString(R.string.confirm_arrived)
                )
            } else if (CommonKeys.TripStatus.Begin_Trip == tripStatus) {
                isTrip = true
                sessionManager.isTrip = true
                //sessionManager.setTripStatus("CONFIRM YOU'VE ARRIVED");
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.ConfirmArrived
                sessionManager.subTripStatus = resources.getString(R.string.begin_trip)

                requstreceivepage.putExtra("isTripBegin", false)
                requstreceivepage.putExtra("tripstatus", resources.getString(R.string.begin_trip))
            } else if (CommonKeys.TripStatus.End_Trip == tripStatus) {
                isTrip = true
                sessionManager.isTrip = true
                //sessionManager.setTripStatus("Begin Trip");
                sessionManager.tripStatus = CommonKeys.TripDriverStatus.BeginTrip
                sessionManager.subTripStatus = resources.getString(R.string.end_trip)

                requstreceivepage.putExtra("isTripBegin", true)
                requstreceivepage.putExtra("tripstatus", resources.getString(R.string.end_trip))
            }
            startActivity(requstreceivepage)
        } else if (CommonKeys.TripStatus.Rating == tripStatus) {
            //sessionManager.setTripStatus("End Trip");
            isTrip = false
            sessionManager.tripStatus = CommonKeys.TripDriverStatus.EndTrip
            val rating = Intent(mContext, Riderrating::class.java)
            rating.putExtra("imgprofile", tripDetailsModel.riderDetails.get(0).profileImage)
            commonMethods.hideProgressDialog()
            startActivity(rating)

        } else if (CommonKeys.TripStatus.Payment == tripStatus) {
            isTrip = false
            val bundle = Bundle()
            bundle.putSerializable("invoiceModels", invoiceModels)
            val main = Intent(mContext, PaymentAmountPage::class.java)
            main.putExtra("AmountDetails", jsonResp.strResponse)
            main.putExtras(bundle)
            commonMethods.hideProgressDialog()
            startActivity(main)
        }
        activity?.overridePendingTransition(R.anim.ub__slide_in_right, R.anim.ub__slide_out_left)


    }


    private fun callIncompleteTripDetailsAPI() {
        if (!CommonKeys.IS_ALREADY_IN_TRIP) {
            apiService.getTripDetails(sessionManager.accessToken!!, "")
                .enqueue(RequestCallback(REQ_TRIP_DETAILS, this))
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun cancelTimer() {
        val intent = Intent(mContext, HeatMapUpdation::class.java)
        val pendingIntent =
            if (Build.VERSION.SDK_INT >= 31) {
                PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getBroadcast(mContext, 0, intent, 0)
            }
        val alarmManager = context!!.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun startTimer() {
        val repeatTime = 300 * 1000  //Repeat alarm time in seconds
        val processTimer = context?.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(mContext, HeatMapUpdation::class.java)
        val pendingIntent =
            if (Build.VERSION.SDK_INT >= 31) {
                PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        //Repeat alarm every second
        processTimer.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + repeatTime,
            repeatTime.toLong(),
            pendingIntent
        )
    }

    /**
     * startTimer
     */
    private fun startDistanceTimer() {
        timerLocation = Timer()
        timerLocationTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    isInternetAvailable = commonMethods.isOnline(context)
                    if (isInternetAvailable) {
                        if (sessionManager.driverStatus.equals("Online"))
                            updateOnlineStatus()
                    }
                }
            }
        }
        timerLocation.schedule(timerLocationTask, delay.toLong(), periodDirection.toLong())
    }


    fun updateOnlineStatus() {
        println("UPDATE FROM HOME")
        //apiService.updateLocation(location).enqueue(RequestCallback(REQ_UPDATE_ONLINE, this))

    }


    /*
 *  Check driver status is online or offline
 */
    /*override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.switch_driverstatus -> {
                CommonMethods.DebuggableLogI("switch_compat", isChecked.toString() + "")
                if (switch_driverstatus.isChecked) {

                    sessionManager.driverStatus = "Online"

                    tvStatus.text = getString(R.string.go_offline)
                    isInternetAvailable = commonMethods.isOnline(context)
                    if (isInternetAvailable) {
                        if (!isTriggeredFromDriverAPIErrorMessage) {
                            updateOnlineStatus()
                        } else {
                            isTriggeredFromDriverAPIErrorMessage = false
                        }

                    } else {
                        commonMethods.showMessage(context, dialog, resources.getString(R.string.no_connection))
                    }

                } else {
                    sessionManager.driverStatus = "Offline"

                    tvStatus.text = getString(R.string.go_onlines)


                    if (isInternetAvailable) {
                        if (!isTriggeredFromDriverAPIErrorMessage) {
                            updateOnlineStatus()
                        } else {
                            isTriggeredFromDriverAPIErrorMessage = false
                        }

                    } else {
                        commonMethods.showMessage(context, dialog, resources.getString(R.string.no_connection))
                    }
                }
            }
            else -> {
                sessionManager.isHeatMapChecked = isChecked
                if (!isChecked) {
                    if (mOverlay2Hr != null) {
                        mOverlay2Hr!!.clearTileCache()
                        mOverlay2Hr!!.remove()
                    }
                    *//*if (mOverlay5Min != null) {
                       mOverlay5Min.clearTileCache();
                      mOverlay5Min.remove();
                   }*//*
                    cancelTimer()
                } else {
                    startTimer()
                    //commonMethods.showProgressDialog((MainActivity) getActivity(), customDialog);
                    apiService.heatMap(sessionManager.accessToken!!, TimeZone.getDefault().id)
                            .enqueue(RequestCallback(REQ_HEAT_MAP, this))

                }
            }
        }

    }*/

    fun updateDocumentstatus(driverstatus: String) {

        if (driverstatus.equals("1")) {
            rltSelectedcar.visibility = View.VISIBLE
            rlt_documentdetails.visibility = View.GONE
            (activity as MainActivity).setOnlineVisbility(0)
        } else {
            rltSelectedcar.visibility = View.GONE
            rlt_documentdetails.visibility = View.VISIBLE
            (activity as MainActivity).setOnlineVisbility(8)
        }


    }

    fun updateActInActStatus(isInActive: Boolean) {

        if (isAdded) {
            /* if (isInActive) {
                 sessionManager.driverSignupStatus = "pending"
                 txt_checkdriverstatus.visibility = View.VISIBLE
                 txt_driverstatus.visibility = View.GONE
                 switch_driverstatus.visibility = View.GONE
                 view.visibility = View.GONE

             } else {
                 sessionManager.driverSignupStatus = "Active"
                 txt_checkdriverstatus.visibility = View.GONE
                 txt_driverstatus.visibility = View.VISIBLE
                 switch_driverstatus.visibility = View.VISIBLE
                 view.visibility = View.VISIBLE

             }*/
        }


    }


    companion object {

        protected val REQUEST_CHECK_SETTINGS = 0x1
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private val TAG = "MAP LOCATION"


        //    private static final int[] ALT_HEATMAP_GRADIENT_COLORS_5MIN = {
        //            Color.argb(0, 247, 207, 5),// transparent
        //            Color.argb(255 / 3 * 2, 247, 207, 5),
        //            Color.rgb(247, 178, 5),
        //            Color.rgb(245, 39, 39),
        //            Color.rgb(255, 0, 0)
        //    };
        //
        //    private static final int[] ALT_HEATMAP_GRADIENT_COLORS_2HR = {
        //            Color.argb(0, 0, 255, 255),// transparent
        //            Color.argb(255 / 3 * 2, 0, 255, 255),
        //            Color.rgb(0, 191, 255),
        //            Color.rgb(0, 0, 127),
        //            Color.rgb(255, 0, 0)
        //    };

        val ALT_HEATMAP_GRADIENT_START_POINTS = floatArrayOf(0.0f, 0.10f, 0.20f, 0.60f, 1.0f)

        var colors_2hr = intArrayOf(
            Color.argb(0, 102, 225, 0), // green
            Color.argb(255 / 3 * 2, 102, 225, 0), // green
            Color.rgb(247, 109, 2), // orange
            Color.rgb(255, 0, 0), // red
            Color.rgb(255, 0, 0)    // red
        )

        var colors_5min = intArrayOf(
            Color.argb(0, 255, 255, 0), // navy
            Color.argb(255 / 3 * 2, 255, 255, 0), //  navy
            Color.rgb(9, 186, 9), // light green
            Color.rgb(0, 100, 0), // green
            Color.rgb(0, 100, 0)    // green
        )

        //    public static float[] startPoints = {
        //            0.2f, 0.5f, 1f
        //    };


        //    public static final Gradient ALT_HEATMAP_GRADIENT_5MIN = new Gradient(ALT_HEATMAP_GRADIENT_COLORS_5MIN,
        //            ALT_HEATMAP_GRADIENT_START_POINTS);
        //
        //    public static final Gradient ALT_HEATMAP_GRADIENT_2HR = new Gradient(ALT_HEATMAP_GRADIENT_COLORS_2HR,
        //            ALT_HEATMAP_GRADIENT_START_POINTS);

        val ALT_HEATMAP_GRADIENT_5MIN = Gradient(
            colors_5min,
            ALT_HEATMAP_GRADIENT_START_POINTS
        )

        val ALT_HEATMAP_GRADIENT_2HR = Gradient(
            colors_2hr,
            ALT_HEATMAP_GRADIENT_START_POINTS
        )

        fun newInstance(): HomeFragment {
            /*   val args: Bundle = Bundle()
               args.putString("status", status)*/
            val fragment = HomeFragment()
            /*  fragment.arguments = args*/
            selectedFrag = 1
            return fragment
        }

        /*
     *  Rotate marker
     **/
        private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
            val normalizeEnd = end - start // rotate start to 0
            val normalizedEndAbs = (normalizeEnd + 360) % 360

            val direction =
                (if (normalizedEndAbs > 180) -1 else 1).toFloat() // -1 = anticlockwise, 1 = clockwise
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

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        sessionManager.isHeatMapChecked = isChecked
        if (!isChecked) {
            if (mOverlay2Hr != null) {
                mOverlay2Hr!!.clearTileCache()
                mOverlay2Hr!!.remove()
            }
            /*if (mOverlay5Min != null) {
               mOverlay5Min.clearTileCache();
              mOverlay5Min.remove();
           }*/
            cancelTimer()
        } else {
            startTimer()
            //commonMethods.showProgressDialog((MainActivity) getActivity(), customDialog);
            apiService.heatMap(sessionManager.accessToken!!, TimeZone.getDefault().id)
                .enqueue(RequestCallback(Enums.REQ_HEAT_MAP, this))

        }
    }


}

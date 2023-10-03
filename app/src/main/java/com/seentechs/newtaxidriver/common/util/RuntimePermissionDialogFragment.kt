package com.seentechs.newtaxidriver.common.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.seentechs.newtaxidriver.BuildConfig
import com.seentechs.newtaxidriver.R
import java.util.*

/*
 * Created by: umasankar
 * description: this class will handle all runtime permission in single dialog fragment*/
class RuntimePermissionDialogFragment : DialogFragment() {

    //    icon Lists
    val cameraIcon = R.drawable.app_ic_permission_camera
    val locationIcon = R.drawable.app_ic_permission_location
    val storageIcon = R.drawable.app_ic_permission_gallery
    val contactIcon = R.drawable.app_ic_permission_call
    val defaultIcon = cameraIcon

    private var requestCodeForCallbackIdentificationSubDivision: Int = 0


    //butterknife view binds
    lateinit @BindView(R.id.imgv_df_permissionImage)
    var permissionTypeImage: ImageView

    lateinit @BindView(R.id.tv_df_permissionAllow)
    var permissionAllow: TextView

    lateinit @BindView(R.id.tv_df_permissionNotAllow)
    var permissionNotAllow: TextView

    lateinit @BindView(R.id.tv_df_permissionDescription)
    var tv_permissionDescription: TextView


    private val PERMISSION_REQUEST_CODE = 11
    private var permissionsRequestFor: Array<String>? = null
    private var mContext: Context? = null
    private var callbackListener: RuntimePermissionRequestedCallback? = null
    private var requestCodeForCallbackIdentification = 0
    private var permissionIcon: Int = 0 // default 0
    private var permissionDescription: String? = null


    // this variable is declared to handle the allow Textview onClick process Dynamically,
    // if true -> it will request permission,
    // else open settings page to grand permission by user manually
    protected var ableToRequestPermission = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.popup_permission_helper, container, false)
        isCancelable = false
        Objects.requireNonNull(dialog!!.window!!).setBackgroundDrawableResource(android.R.color.transparent)
        ButterKnife.bind(this, rootView)
        setImageResourceAndPermissionDescriptionForPopup()
        return rootView
    }

    @OnClick(R.id.tv_df_permissionNotAllow)
    fun notAllowPermission() {
        afterPermissionDenied()
    }

    @OnClick(R.id.tv_df_permissionAllow)
    fun allowPermission() {
        if (ableToRequestPermission) {
            requestNecessaryPermissions()
        } else {
            mContext!!.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
            dismiss()
        }
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
        callbackListener = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (permissions.size != 0 && grantResults.size != 0) {
            val isAnyPermissionDenied = false
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (!shouldShowRequestPermissionRationale(permission) && grantResult != PackageManager.PERMISSION_GRANTED) {
                    notAbleToRequestPermission()
                    return
                } else if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    afterPermissionDenied()
                    return
                }
            }
            callbackListener!!.permissionGranted(requestCodeForCallbackIdentification, requestCodeForCallbackIdentificationSubDivision)
            dismiss()

        } else {
            Toast.makeText(mContext, "permission size 0", Toast.LENGTH_SHORT).show()
        }
    }

    fun setImageResourceAndPermissionDescriptionForPopup() {
        getPermissionRequestedForIconAndDescription()
        permissionTypeImage!!.setImageResource(permissionIcon)
        tv_permissionDescription!!.text = permissionDescription
    }

    fun getPermissionRequestedForIconAndDescription() {
        when (permissionsRequestFor!![0]) {
            CAMERA_PERMISSION -> {
                permissionIcon = cameraIcon
                permissionDescription = mContext!!.resources.getString(R.string.camera_permission_description)
            }
            WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                permissionIcon = storageIcon
                permissionDescription = mContext!!.resources.getString(R.string.storage_permission_description)
            }
            LOCATION_PERMISSION -> {
                permissionIcon = locationIcon
                permissionDescription = mContext!!.resources.getString(R.string.location_permission_description)
            }
            /*BACKGROUND_LOCATION_PERMISSION -> {
                permissionIcon = locationIcon
                permissionDescription = mContext!!.resources.getString(R.string.location_permission_description)
            }*/
            RECORD_AUDIO_PERMISSION -> {
                permissionIcon = contactIcon
                permissionDescription = mContext!!.resources.getString(R.string.audio_permission_description)
            }
            else -> {
                permissionIcon = cameraIcon
                permissionDescription = mContext!!.resources.getString(R.string.camera_permission_description)
            }
        }
    }

    private fun requestNecessaryPermissions() {
        ableToRequestPermission = true
        requestPermissions(permissionsRequestFor!!, PERMISSION_REQUEST_CODE)
    }

    private fun notAbleToRequestPermission() {
        permissionAllow!!.text = mContext!!.resources.getString(R.string.settings)
        ableToRequestPermission = false

    }

    private fun afterPermissionDenied() {
        showPermissionDeniedMessageToUser()
        callbackListener!!.permissionDenied(requestCodeForCallbackIdentification, requestCodeForCallbackIdentificationSubDivision)
        dismiss()
    }

    fun showPermissionDeniedMessageToUser() {
        Toast.makeText(mContext, mContext!!.resources.getString(R.string.enable_permissions_to_proceed_further), Toast.LENGTH_SHORT).show()

    }

    interface RuntimePermissionRequestedCallback {
        fun permissionGranted(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int)

        fun permissionDenied(requestCodeForCallbackIdentificationCode: Int, requestCodeForCallbackIdentificationCodeSubDivision: Int)
    }

    companion object {

        /*if you need to add permission
     * first add in permission list and then add icon list*/

        //    Permissions List
        val CAMERA_PERMISSION = Manifest.permission.CAMERA
        val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        val MODIFY_AUDIO_PERMISSION = Manifest.permission.MODIFY_AUDIO_SETTINGS

        // camera permission Array list

        val CAMERA_PERMISSION_ARRAY = arrayOf(CAMERA_PERMISSION, WRITE_EXTERNAL_STORAGE_PERMISSION, READ_EXTERNAL_STORAGE_PERMISSION)
        val STORAGEPERMISSIONARRAY = arrayOf(WRITE_EXTERNAL_STORAGE_PERMISSION)
        //public final int storageIcon = android.R.drawable.sym_contact_card; // this may be used as alternative icon for SDcard access


        // request Codes For Callback Identifications
        val cameraAndGallaryCallBackCode = 0
        val externalStoreageCallbackCode = 1
        val locationCallbackCode = 2
        val contactCallbackCode = 3
        val audioCallbackCode = 4

        fun checkPermissionStatus(mContext: Context, fragmentManager: FragmentManager, callbackListener: RuntimePermissionRequestedCallback, permissionsRequestFor: Array<String>, requestCodeForCallbackIdentification: Int, requestCodeForCallbackIdentificationSubDivision: Int) {

            /*
         * here function check permission status and then checks shouldAskPermissionForThisAndroidOSVersion or not
         * because some custom phone below Android M request permission from user at Run time example: redmi phones*/

            var allPermissionGranted: Boolean? = true

            for (permissionRequestFor in permissionsRequestFor) {
                if (checkSelfPermissions(mContext, permissionRequestFor)) {
                    allPermissionGranted = false
                    break
                }
            }
            if ((!allPermissionGranted!!)!!) {
                if (shouldAskPermissionForThisAndroidOSVersion()) {
                    val runtimePermissionDialogFragment = RuntimePermissionDialogFragment()
                    runtimePermissionDialogFragment.permissionsRequestFor = permissionsRequestFor
                    runtimePermissionDialogFragment.callbackListener = callbackListener
                    runtimePermissionDialogFragment.requestCodeForCallbackIdentification = requestCodeForCallbackIdentification
                    runtimePermissionDialogFragment.requestCodeForCallbackIdentificationSubDivision = requestCodeForCallbackIdentificationSubDivision
                    runtimePermissionDialogFragment.mContext = mContext
                    runtimePermissionDialogFragment.show(fragmentManager, RuntimePermissionDialogFragment::class.java.name)
                } else {
                    //                we write code here becoz of static method, it not static method we call afterPermissionDenied()
                    callbackListener.permissionDenied(requestCodeForCallbackIdentification, requestCodeForCallbackIdentificationSubDivision)
                    Toast.makeText(mContext, mContext.resources.getString(R.string.enable_permissions_to_proceed_further), Toast.LENGTH_SHORT).show()

                }
            } else {
                callbackListener.permissionGranted(requestCodeForCallbackIdentification, requestCodeForCallbackIdentificationSubDivision)
            }
        }

        fun checkSelfPermissions(mContext: Context, permissionRequestFor: String): Boolean {
            return ContextCompat.checkSelfPermission(mContext, permissionRequestFor) != PackageManager.PERMISSION_GRANTED
        }

        fun shouldAskPermissionForThisAndroidOSVersion(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }
    }
}

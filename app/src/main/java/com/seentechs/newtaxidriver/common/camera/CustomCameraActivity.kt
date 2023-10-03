package com.seentechs.newtaxidriver.common.camera

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage Camera
 * @category CustomCameraActivity
 * @author Seen Technologies
 *
 */

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

import com.github.florent37.camerafragment.CameraFragment
import com.github.florent37.camerafragment.CameraFragmentApi
import com.github.florent37.camerafragment.PreviewActivity
import com.github.florent37.camerafragment.configuration.Configuration
import com.github.florent37.camerafragment.listeners.CameraFragmentControlsAdapter
import com.github.florent37.camerafragment.listeners.CameraFragmentResultAdapter
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener
import com.github.florent37.camerafragment.listeners.CameraFragmentStateAdapter
import com.github.florent37.camerafragment.listeners.CameraFragmentVideoRecordTextAdapter
import com.github.florent37.camerafragment.widgets.CameraSettingsView
import com.github.florent37.camerafragment.widgets.CameraSwitchView
import com.github.florent37.camerafragment.widgets.FlashSwitchView
import com.github.florent37.camerafragment.widgets.MediaActionSwitchView
import com.github.florent37.camerafragment.widgets.RecordButton
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.views.CommonActivity

import java.io.File
import java.util.ArrayList

/* ************************************************************
                      CustomCameraActivity
    Customized camera for upload and images using camera
*************************************************************** */
class CustomCameraActivity : CommonActivity(), View.OnClickListener {
    private var settingsView: CameraSettingsView? = null
    private var flashSwitchView: FlashSwitchView? = null
    private var cameraSwitchView: CameraSwitchView? = null
    private var recordButton: RecordButton? = null
    private var mediaActionSwitchView: MediaActionSwitchView? = null
    private var recordDurationText: TextView? = null
    private var recordSizeText: TextView? = null
    private var cameraLayout: RelativeLayout? = null
    private var addCameraButton: Button? = null

    /*
     *  Fragement for camera
     **/
    private val cameraFragment: CameraFragmentApi?
        get() = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as CameraFragmentApi?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_camera)

        settingsView = findViewById<View>(R.id.settings_view) as CameraSettingsView
        flashSwitchView = findViewById<View>(R.id.flash_switch_view) as FlashSwitchView
        cameraSwitchView = findViewById<View>(R.id.front_back_camera_switcher) as CameraSwitchView
        recordButton = findViewById<View>(R.id.record_button) as RecordButton
        mediaActionSwitchView = findViewById<View>(R.id.photo_video_camera_switcher) as MediaActionSwitchView
        recordDurationText = findViewById<View>(R.id.record_duration_text) as TextView
        recordSizeText = findViewById<View>(R.id.record_size_mb_text) as TextView
        addCameraButton = findViewById<View>(R.id.addCameraButton) as Button
        cameraLayout = findViewById<View>(R.id.cameraLayout) as RelativeLayout

        /*
    *  Request permission
    **/
        if (Build.VERSION.SDK_INT > 15) {
            val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

            val permissionsToRequest = ArrayList<String>()
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission)
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_CAMERA_PERMISSIONS)
            } else
                addCamera()
        } else {
            addCamera()
        }

        settingsView!!.setOnClickListener(this)
        flashSwitchView!!.setOnClickListener(this)
        cameraSwitchView!!.setOnClickListener(this)
        recordButton!!.setOnClickListener(this)
        mediaActionSwitchView!!.setOnClickListener(this)
    }

    /*
    *   Flash on / off
    **/
    fun onFlashSwitcClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.toggleFlashMode()
    }

    /*
    *  Switch to camera front and back
    **/
    fun onSwitchCameraClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.switchCameraTypeFrontBack()
    }

    /*
    *  Take photo or record video
    **/
    fun onRecordButtonClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.takePhotoOrCaptureVideo(object : CameraFragmentResultAdapter() {
            override fun onVideoRecorded(filePath: String?) {
                Toast.makeText(baseContext, "onVideoRecorded " + filePath!!, Toast.LENGTH_SHORT).show()
            }

            override fun onPhotoTaken(bytes: ByteArray?, filePath: String?) {
                Toast.makeText(baseContext, "onPhotoTaken " + filePath!!, Toast.LENGTH_SHORT).show()

            }
        }, null, "photo0")

        // "/storage/self/primary",
        //  "photo0"
    }

    /*
     *  Activity result for get data from camera
     **/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*Intent resultIntent = new Intent();
            if (view.getId() == com.github.florent37.camerafragment.R.id.confirm_media_result) {
                resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_CONFIRM).putExtra(FILE_PATH_ARG, previewFilePath);
            } else if (view.getId() == com.github.florent37.camerafragment.R.id.re_take_media) {
                deleteMediaFile();
                resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_RETAKE);
            } else if (view.getId() == com.github.florent37.camerafragment.R.id.cancel_media_action) {
                deleteMediaFile();
                resultIntent.putExtra(RESPONSE_CODE_ARG, ACTION_CANCEL);
            }*/
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    /*
     *  Open setting page
     **/
    fun onSettingsClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.openSettingDialog()
    }

    /*
    *  Switch photo and video
    **/
    fun onMediaActionSwitchClicked() {
        val cameraFragment = cameraFragment
        cameraFragment?.switchActionPhotoVideo()
    }


    /*
    *  Permission for camera
   **/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size != 0) {
            addCamera()
        }
    }

 /*   @RequiresPermission(Manifest.permission.CAMERA)*/
    fun addCamera() {
        addCameraButton!!.visibility = View.GONE
        cameraLayout!!.visibility = View.VISIBLE

        val cameraFragment = CameraFragment.newInstance(Configuration.Builder()
                .setCamera(Configuration.CAMERA_FACE_REAR).build())
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, cameraFragment!!, FRAGMENT_TAG)
                .commitAllowingStateLoss()

        cameraFragment.setResultListener(object : CameraFragmentResultListener {
            override fun onVideoRecorded(filePath: String) {
                val intent = PreviewActivity.newIntentVideo(this@CustomCameraActivity, filePath)
                startActivityForResult(intent, REQUEST_PREVIEW_CODE)
            }

            override fun onPhotoTaken(bytes: ByteArray, filePath: String) {
                val intent = PreviewActivity.newIntentPhoto(this@CustomCameraActivity, filePath)
                startActivityForResult(intent, REQUEST_PREVIEW_CODE)
            }
        })

        cameraFragment.setStateListener(object : CameraFragmentStateAdapter() {

            override fun onCurrentCameraBack() {
                cameraSwitchView!!.displayBackCamera()
            }

            override fun onCurrentCameraFront() {
                cameraSwitchView!!.displayFrontCamera()
            }

            override fun onFlashAuto() {
                flashSwitchView!!.displayFlashAuto()
            }

            override fun onFlashOn() {
                flashSwitchView!!.displayFlashOn()
            }

            override fun onFlashOff() {
                flashSwitchView!!.displayFlashOff()
            }

            override fun onCameraSetupForPhoto() {
                mediaActionSwitchView!!.displayActionWillSwitchVideo()

                recordButton!!.displayPhotoState()
                flashSwitchView!!.visibility = View.VISIBLE
            }

            override fun onCameraSetupForVideo() {
                mediaActionSwitchView!!.displayActionWillSwitchPhoto()

                recordButton!!.displayVideoRecordStateReady()
                flashSwitchView!!.visibility = View.GONE
            }

            override fun shouldRotateControls(degrees: Int) {
                ViewCompat.setRotation(cameraSwitchView!!, degrees.toFloat())
                ViewCompat.setRotation(mediaActionSwitchView!!, degrees.toFloat())
                ViewCompat.setRotation(flashSwitchView!!, degrees.toFloat())
                ViewCompat.setRotation(recordDurationText!!, degrees.toFloat())
                ViewCompat.setRotation(recordSizeText!!, degrees.toFloat())
            }

            override fun onRecordStateVideoReadyForRecord() {
                recordButton!!.displayVideoRecordStateReady()
            }

            override fun onRecordStateVideoInProgress() {
                recordButton!!.displayVideoRecordStateInProgress()
            }

            override fun onRecordStatePhoto() {
                recordButton!!.displayPhotoState()
            }

            override fun onStopVideoRecord() {
                recordSizeText!!.visibility = View.GONE
                //cameraSwitchView.setVisibility(View.VISIBLE);
                settingsView!!.visibility = View.VISIBLE
            }

            override fun onStartVideoRecord(outputFile: File?) {

            }
        })

        cameraFragment.setControlsListener(object : CameraFragmentControlsAdapter() {
            override fun lockControls() {
                cameraSwitchView!!.isEnabled = false
                recordButton!!.isEnabled = false
                settingsView!!.isEnabled = false
                flashSwitchView!!.isEnabled = false
            }

            override fun unLockControls() {
                cameraSwitchView!!.isEnabled = true
                recordButton!!.isEnabled = true
                settingsView!!.isEnabled = true
                flashSwitchView!!.isEnabled = true
            }

            override fun allowCameraSwitching(allow: Boolean) {
                cameraSwitchView!!.visibility = if (allow) View.VISIBLE else View.GONE
            }

            override fun allowRecord(allow: Boolean) {
                recordButton!!.isEnabled = allow
            }

            override fun setMediaActionSwitchVisible(visible: Boolean) {
                mediaActionSwitchView!!.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            }
        })

        cameraFragment.setTextListener(object : CameraFragmentVideoRecordTextAdapter() {
            override fun setRecordSizeText(size: Long, text: String?) {
                recordSizeText!!.text = text
            }

            override fun setRecordSizeTextVisible(visible: Boolean) {
                recordSizeText!!.visibility = if (visible) View.VISIBLE else View.GONE
            }

            override fun setRecordDurationText(text: String?) {
                recordDurationText!!.text = text
            }

            override fun setRecordDurationTextVisible(visible: Boolean) {
                recordDurationText!!.visibility = if (visible) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.flash_switch_view -> onFlashSwitcClicked()
            R.id.photo_video_camera_switcher -> onMediaActionSwitchClicked()
            R.id.settings_view -> onSettingsClicked()
            R.id.record_button -> onRecordButtonClicked()
            R.id.front_back_camera_switcher -> onSwitchCameraClicked()
            else -> {
            }
        }
    }

    companion object {
        val FRAGMENT_TAG = "camera"
        private val REQUEST_CAMERA_PERMISSIONS = 931
        private val REQUEST_PREVIEW_CODE = 1001
        private val MEDIA_ACTION_ARG = "media_action_arg"
        private val FILE_PATH_ARG = "file_path_arg"

    }
}

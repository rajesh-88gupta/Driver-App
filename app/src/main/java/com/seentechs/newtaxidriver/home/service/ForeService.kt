package com.seentechs.newtaxidriver.home.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods

class ForeService : Service() {



    private var mWindowManager: WindowManager ? = null
    private lateinit var floatingWidget: View

    init {
        AppController.getAppComponent().inject(this)
    }
    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        CommonMethods.DebuggableLogE("Foreground Service", "onCreate")

        //floatingWidget();

        val title = "foreService"
        val body = "this is a foreground notification"
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CommonMethods.DebuggableLogE("Foreground Service", "service o and above")
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        startForeground(10000, notificationBuilder.build())
    }

    fun stopForegroundService() {
        CommonMethods.DebuggableLogE("Foreground Service", "Stop")

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }


    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent =
            if (Build.VERSION.SDK_INT >= 31) {
                PendingIntent.getService(
                    applicationContext,
                    1,
                    restartServiceIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }else {
                PendingIntent.getService(
                    applicationContext,
                    1,
                    restartServiceIntent,
                    PendingIntent.FLAG_ONE_SHOT
                )
            }
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent)

        super.onTaskRemoved(rootIntent)
    }


    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null)
        {
            String action = intent.getAction();

            switch (action)
            {
                case ACTION_START_FOREGROUND_SERVICE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    }
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    break;

            }
        }
        return super.onStartCommand(intent, flags, startId);
    }*/

    fun floatingWidget() {
        floatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget, null)


        //Add the view to the window.
        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        //Specify the chat head position
        //Initially view will be added to top-left corner
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 100

        //Add the view to the window
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        mWindowManager?.let {
            it.addView(floatingWidget, params)
        }


        //Set the close button.
        val closeButton = floatingWidget.findViewById<View>(R.id.close_btn) as ImageView
        closeButton.setOnClickListener {
            //close the service and remove the chat head from the window
            //stopSelf();
        }

        //Drag and move chat head using user's touch action.
        val chatHeadImage = floatingWidget.findViewById<View>(R.id.chat_head_profile_iv) as ImageView
        chatHeadImage.setOnTouchListener(object : View.OnTouchListener {
            private var lastAction: Int = 0
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {

                        //remember the initial position.
                        initialX = params.x
                        initialY = params.y

                        //get the touch location
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY

                        lastAction = event.action
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            //Open the chat conversation click.
                            val intent = Intent(this@ForeService, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            //startActivity(intent);

                            //close the service and remove the chat heads
                            //stopSelf();
                        }
                        lastAction = event.action
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()

                        //Update the layout with new X & Y coordinate

                        mWindowManager?.let {
                            it.updateViewLayout(floatingWidget, params)
                        }

                        lastAction = event.action
                        return true
                    }
                }
                return false
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()

        mWindowManager?.let {
            it.removeView(floatingWidget)
        }

        //if (floatingWidget != null) mWindowManager!!.removeView(floatingWidget)
    }

    companion object {

        private val CHANNEL_ID = "fore"
        private val CHANNEL_NAME = "test"

        val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"

        val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
    }

}

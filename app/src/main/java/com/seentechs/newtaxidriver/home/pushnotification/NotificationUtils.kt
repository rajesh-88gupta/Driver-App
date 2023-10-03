package com.seentechs.newtaxidriver.home.pushnotification

/**
 * @package com.seentechs.newtaxidriver.home.pushnotification
 * @subpackage pushnotification model
 * @category NotificationUtils
 * @author Seen Technologies
 *
 */

import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.util.Patterns
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.MainActivity
import com.seentechs.newtaxidriver.home.firebaseChat.ActivityChat
import java.io.IOException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection


/* ************************************************************
                NotificationUtils
Its used to send the push notification message function
*************************************************************** */
class NotificationUtils(private val mContext: Context) {
    //private val notificationChannelName = mContext.resources.getString(R.string.app_name)+"DriverChat"
    private val notificationChannelName = "Chat"

    //var defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    @JvmOverloads
    fun showNotificationMessage(title: String, message: String, timeStamp: String, intent: Intent, imageUrl: String? = null, duration: Long) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return


        // notification icon
        val icon = R.mipmap.ic_launcher

        CommonMethods.DebuggableLogD("Icon", "Iconone")

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val resultPendingIntent =
            if (Build.VERSION.SDK_INT >= 31){
                PendingIntent.getActivity(
                    mContext,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }else {
                PendingIntent.getActivity(
                    mContext,
                    0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            }

        val mBuilder = NotificationCompat.Builder(
                mContext)


        if (!TextUtils.isEmpty(imageUrl)) {

            if (imageUrl != null && imageUrl.length > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {

                val bitmap = getBitmapFromURL(imageUrl)

                if (bitmap != null) {
                    showBigNotification(bitmap, mBuilder, icon, title, message, timeStamp, resultPendingIntent, duration)
                } else {
                    showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, duration)
                }
            }
        } else {
            showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, duration)
            //playNotificationSound();
        }
    }

    private fun showSmallNotification(mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String, timeStamp: String, resultPendingIntent: PendingIntent, duration: Long) {

        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine(message)

        val notificationManager = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        /* mBuilder.setSmallIcon(icon);
        mBuilder.setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setColor(ContextCompat.getColor(mContext, R.color.ub__black))
                .setContentText(message);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelID, notificationChannelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{1000,1000});
            assert notificationManager != null;
            mBuilder.setChannelId("10010");
            notificationManager.createNotificationChannel(notificationChannel);
        }



        assert notificationManager != null;
        notificationManager.notify(Config.NOTIFICATION_ID, mBuilder.build());*/

        //mBuilder = new NotificationCompat.Builder(mContext,notificationChannelName);
        mBuilder.setSmallIcon(Constants.notificationIcon)
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                //.setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(null)
                .setDefaults(0)
                .setTimeoutAfter(duration)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(resultPendingIntent).color = ContextCompat.getColor(mContext, R.color.newtaxi_app_black)



        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(notificationChannelID, notificationChannelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = ContextCompat.getColor(mContext, R.color.newtaxi_app_black)
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(1000, 1000)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            /*val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()*/
            //notificationChannel.setSound(sound, audioAttributes)
            notificationChannel.setSound(null, null)

            mBuilder.setChannelId(notificationChannelID)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0 /* Request Code */, mBuilder.build())
        soundNotification()

    }

    private fun showBigNotification(bitmap: Bitmap, mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String, timeStamp: String, resultPendingIntent: PendingIntent, duration: Long) {
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
        bigPictureStyle.setBigContentTitle(title)
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString())
        bigPictureStyle.bigPicture(bitmap)

        val notificationManager = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                //.setOngoing(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(0)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(Constants.notificationIcon)
                .setSound(null)
                .setTimeoutAfter(duration)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, icon))
                .setContentText(message)


        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(notificationChannelID, notificationChannelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(1000, 1000)
            notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            mBuilder.setChannelId(notificationChannelID)
            notificationManager.createNotificationChannel(notificationChannel)
        }


        notificationManager.notify(0 /* Request Code */, mBuilder.build())

    }


    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    fun getBitmapFromURL(strURL: String): Bitmap? {
        try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpsURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }

    // Playing notification sound
    fun playNotificationSound() {
        /*try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            *//* Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/notification");*//*
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

    }
//To Play a custom ringtone upon receipt of ride request
    fun soundNotification() {
        println("soundNotification @")
        try {
            var alarmSound: Uri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + mContext.getPackageName() + "/raw/notify");
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun generateNotification(context: Context, message: String, title: String) {


        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        showNotificationMessage(title, message, timeStamp, notificationIntent, null, 0L)


        CommonMethods.DebuggableLogD("Icon", "Iconfour")

    }


    fun generateFirebaseChatNotification(context: Context, message: String) {

        val title = context.getString(R.string.app_name)

        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());

        val notificationIntent = Intent(context, ActivityChat::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        showNotificationMessage(title, message, timeStamp, notificationIntent, null, 0L)


        CommonMethods.DebuggableLogD("Icon", "Iconfour")

    }

    companion object {
        private val notificationChannelID = "125002"

        /**
         * Method checks if the app is in background or not
         */
        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                val runningProcesses = am.runningAppProcesses
                for (processInfo in runningProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.packageName) {
                                isInBackground = false
                            }
                        }
                    }
                }
            } else {
                val taskInfo = am.getRunningTasks(1)
                val componentInfo = taskInfo[0].topActivity
                if (componentInfo?.packageName == context.packageName) {
                    isInBackground = false
                }
            }

            return isInBackground
        }

        // Clears notification tray messages
        public fun clearNotifications(context: Context) {
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }

        fun getTimeMilliSec(timeStamp: String): Long {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                val date = format.parse(timeStamp)
                return date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return 0
        }
    }


}

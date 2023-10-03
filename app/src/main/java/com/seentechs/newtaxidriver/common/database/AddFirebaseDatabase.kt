package com.seentechs.newtaxidriver.common.database

import android.content.Context
import android.text.TextUtils
import com.firebase.geofire.GeoFire
import com.google.firebase.database.*
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.CommonMethods.Companion.DebuggableLogE
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys
import com.seentechs.newtaxidriver.trips.rating.PaymentAmountPage
import org.json.JSONObject
import javax.inject.Inject

class AddFirebaseDatabase {
    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    private var mFirebaseDatabase: DatabaseReference? = null
    private var geofire: GeoFire? = null
    private var mSearchedDriverReferenceListener: ValueEventListener? = null
    private var query: Query? = null
    private val TAG = "Android_Debug"
    private val firebaseReqListener: IFirebaseReqListener? = null

    init {
        AppController.getAppComponent().inject(this)
    }

    fun updateRequestTable(riderId: String, tripId: String, context: Context) {
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
        mFirebaseDatabase!!.child(FirebaseDbKeys.Rider).child(riderId).child(FirebaseDbKeys.TripId).setValue(tripId)
        query = mFirebaseDatabase!!.child(riderId)
    }


    fun removeNodesAfterCompletedTrip(context: Context) {
        try {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))

            sessionManager.tripId?.let {
                mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE)!!.child(FirebaseDbKeys.TRIPLIVEPOLYLINE).removeValue()
                mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE)!!.child(it).removeValue()
            }
            query!!.removeEventListener(mSearchedDriverReferenceListener!!)
            mFirebaseDatabase!!.removeEventListener(mSearchedDriverReferenceListener!!)
            sessionManager.clearTripID()
            sessionManager.poolIds = ""
            sessionManager.isPool = false
            mSearchedDriverReferenceListener = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeLiveTrackingNodesAfterCompletedTrip(context: Context) {
        try {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            sessionManager.tripId?.let { mFirebaseDatabase!!.child(FirebaseDbKeys.LIVE_TRACKING_NODE).child(it).removeValue() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        removeNodesAfterCompletedTrip(context)
    }

    fun removeRequestTable() {

        /* mFirebaseDatabase.getDatabase().getReference(FirebaseDbKeys.Rider).removeValue();
        query.removeEventListener(mSearchedDriverReferenceListener);
        mFirebaseDatabase.removeEventListener(mSearchedDriverReferenceListener);
        mSearchedDriverReferenceListener = null;*/
        query!!.removeEventListener(mSearchedDriverReferenceListener!!)
    }

    fun removeDriverFromGeofire(context: Context) {
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db)).child(FirebaseDbKeys.GEOFIRE)
        geofire = GeoFire(mFirebaseDatabase)
        geofire!!.removeLocation(sessionManager.userId)
    }


    fun firebasePushLisener(context: Context) {
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
        pushNotifQuery = mFirebaseDatabase!!.child(FirebaseDbKeys.Notification).child(sessionManager.userId!!)

        firebaseDbPush = pushNotifQuery!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                println("JSON FROM DB : ")

                if (dataSnapshot.key.equals(sessionManager.userId!!)) {
                    val pushJson = dataSnapshot.getValue(String::class.java)
                    println("JSON FROM DB : " + pushJson)
                    if (!TextUtils.isEmpty(pushJson) && dataSnapshot.value != null) {
                        val json = JSONObject(pushJson)
                        commonMethods.handleDataMessage(json, context)

                        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
                        mFirebaseDatabase!!.child(FirebaseDbKeys.Notification).child(sessionManager.userId!!).removeValue()

                    }
                } else {
                    pushNotifQuery!!.removeEventListener(this)
                    mFirebaseDatabase!!.removeEventListener(this)
                    mFirebaseDatabase!!.onDisconnect()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                DebuggableLogE(TAG, "Failed to read user", error.toException())
            }
        })

    }


    fun initPaymentChangeListener(context: Context) {
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))

        // mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(FirebaseDbKeys.TRIP_PAYMENT_NODE)

        query = mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE).child(sessionManager.tripId!!).child(FirebaseDbKeys.TRIP_PAYMENT_NODE_REFRESH_PAYMENT_TYPE_KEY)

        mSearchedDriverReferenceListener = query!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                PaymentAmountPage.paymentAmountPageInstance.callGetInvoiceAPI()
                DebuggableLogE(TAG, "Database Updated Successfully")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                DebuggableLogE(TAG, "Failed to read user", error.toException())
            }
        })
    }

    fun updateEtaToFirebase(eta: String, context: Context) {
        if (sessionManager.isTrip) {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE).child(sessionManager.tripId!!).child(FirebaseDbKeys.TRIPETA).setValue(eta)
        }

    }

    fun UpdatePolyLinePoints(overviewpolylines: String, context: Context) {
        if (sessionManager.isTrip) {
            val value = overviewpolylines
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE).child(sessionManager.tripId!!).child(FirebaseDbKeys.TRIPLIVEPOLYLINE).setValue(value)


            var poolIds: List<String> = sessionManager.poolIds!!.split(",").map { it.trim() }

            for (i in poolIds.indices) {
                if (!poolIds.get(i).equals(sessionManager.tripId!!))
                    mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE).child(poolIds.get(i)).child(FirebaseDbKeys.TRIPLIVEPOLYLINE).setValue("0")


            }
        }

    }

    companion object {
        private var firebaseDbPush: ValueEventListener? = null
        private var query: Query? = null
        private var pushNotifQuery: Query? = null
    }
}

package com.seentechs.newtaxidriver.home.firebaseChat

import android.content.Context
import android.text.TextUtils
import com.google.firebase.database.*
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.network.AppController.Companion.context
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.home.datamodel.firebase_keys.FirebaseDbKeys
import java.util.*
import javax.inject.Inject


class FirebaseChatHandler(private val callbackListener: FirebaseChatHandlerInterface, @param:CommonKeys.FirebaseChatServiceTriggeredFrom @field:CommonKeys.FirebaseChatServiceTriggeredFrom
private val firebaseChatserviceTriggeredFrom: Int) {

    lateinit @Inject
    var sessionManager: SessionManager

    private val root: DatabaseReference

    private var isChatTriggerable: Boolean? = false

    private val childCount = 0

    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            if (firebaseChatserviceTriggeredFrom == CommonKeys.FirebaseChatServiceTriggeredFrom.backgroundService) {

                if (isChatTriggerable!!) {
                    callbackListener.pushMessage(dataSnapshot.getValue(FirebaseChatModelClass::class.java))
                }
            } else {
                callbackListener.pushMessage(dataSnapshot.getValue(FirebaseChatModelClass::class.java))
            }

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {


        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            isChatTriggerable = true
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    init {
        AppController.getAppComponent().inject(this)
        root = FirebaseDatabase.getInstance().reference.child(context!!.getString(R.string.real_time_db)).child(FirebaseDbKeys.chatFirebaseDatabaseName).child(sessionManager.tripId!!)


        if (firebaseChatserviceTriggeredFrom == CommonKeys.FirebaseChatServiceTriggeredFrom.backgroundService) {
            root.addListenerForSingleValueEvent(valueEventListener)
            root.addChildEventListener(childEventListener)
            //root.addValueEventListener(valueEventListener);
        } else if (firebaseChatserviceTriggeredFrom == CommonKeys.FirebaseChatServiceTriggeredFrom.chatActivity) {
            root.addChildEventListener(childEventListener)
        }


    }

    internal fun addMessage(message: String) {
        try {
            if (!TextUtils.isEmpty(message)) {
                val map = HashMap<String, Any>()
                val temp_key = root.push().key
                root.updateChildren(map)

                val message_root = root.child(temp_key!!)
                val map2 = HashMap<String, Any>()
                map2[CommonKeys.FIREBASE_CHAT_MESSAGE_KEY] = message
                map2[CommonKeys.FIREBASE_CHAT_TYPE_KEY] = CommonKeys.FIREBASE_CHAT_TYPE_DRIVER

                message_root.updateChildren(map2)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun unRegister() {

        root.removeEventListener(childEventListener)
    }

    interface FirebaseChatHandlerInterface {
        fun pushMessage(firebaseChatModelClass: FirebaseChatModelClass?)
    }

    companion object {

        fun deleteChatNode(tripID: String,context: Context) {
            try {
                val root = FirebaseDatabase.getInstance().reference.child(context.getString(R.string.real_time_db)).child(FirebaseDbKeys.chatFirebaseDatabaseName).child(tripID)
                root.removeValue()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}

package com.seentechs.newtaxidriver.common.helper

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.seentechs.newtaxidriver.common.util.CommonKeys


class FirebaseDatabaseAdder(internal var dbNodeName: String, internal var type: Int, internal var data: String?, internal var tripid: String) {
    private var mFirebaseDatabase: DatabaseReference? = null

    private var mDataUpateListners: ValueEventListener? = null

    init {
        addDataToDb()
    }

    private fun addDataToDb() {
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(dbNodeName)
        if (type == 1) {
            mFirebaseDatabase!!.child(data!!).child(CommonKeys.TripId).setValue(tripid)
        }

        if (mDataUpateListners == null) {
            addLatLngChangeListener() // Get Driver Lat Lng
        }
    }

    private fun addLatLngChangeListener() {

        // User data change listener
        val query = mFirebaseDatabase!!.child(data!!)

        mDataUpateListners = query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (data != null) {
                    println("DataBase Created")

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }

}

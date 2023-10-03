package com.seentechs.newtaxidriver.home.fragments.Referral

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.home.datamodel.CompletedOrPendingReferrals
import com.seentechs.newtaxidriver.common.util.CommonKeys

import com.seentechs.newtaxidriver.common.util.CommonKeys.CompletedReferralArray
import com.seentechs.newtaxidriver.common.util.CommonKeys.IncompleteReferralArray

class ReferralFriendsListRecyclerViewAdapter(private val mContext: Context, private val referredFriendsModelArrayList: List<CompletedOrPendingReferrals>?, private val referralArrayType: Int) : RecyclerView.Adapter<ReferralFriendsListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_recyclerview_adapter_referral_friends_status_single_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val referredFriendsModelArrayListSingleObject = referredFriendsModelArrayList!![position]
        holder.tvReferredFriend.text = referredFriendsModelArrayListSingleObject.name
        holder.tvAmountGain.text = referredFriendsModelArrayListSingleObject.earnableAmount

        if (referralArrayType == IncompleteReferralArray) {
            if (referredFriendsModelArrayListSingleObject.remainingTrips != 0L && referredFriendsModelArrayListSingleObject.remainingDays != 0L) {
                var daysLeft = mContext.resources.getString(R.string.days_left)
                var tripsLeft = mContext.getString(R.string.trips)
                if (referredFriendsModelArrayListSingleObject.remainingDays == 1L) {
                    daysLeft = mContext.resources.getString(R.string.day_left)
                }

                if (referredFriendsModelArrayListSingleObject.remainingTrips == 1L) {
                    tripsLeft = mContext.resources.getString(R.string.trip_left)
                }
                val tripsAndDaysRemainingText = StringBuilder(referredFriendsModelArrayListSingleObject.remainingDays.toString() + " " + daysLeft + " | " + mContext.getString(R.string.need_to_complete) + " " + referredFriendsModelArrayListSingleObject.remainingTrips + " " + tripsLeft)
                holder.tvRemainingDaysAndTrips.text = tripsAndDaysRemainingText
            } else {
                holder.tvRemainingDaysAndTrips.visibility = View.GONE
            }
        } else if (referralArrayType == CompletedReferralArray) {
            if (referredFriendsModelArrayListSingleObject.status == CommonKeys.ReferralStatus.Expired) {
                holder.tvRemainingDaysAndTrips.setTextColor(ContextCompat.getColor(mContext,R.color.newtaxi_app_navy))
                holder.tvRemainingDaysAndTrips.setCompoundDrawables(ContextCompat.getDrawable(mContext,R.drawable.app_ic_alert_warning),null,null,null)
                holder.tvRemainingDaysAndTrips.text = mContext.getString(R.string.referral_expired)
            } else if (referredFriendsModelArrayListSingleObject.status == CommonKeys.ReferralStatus.Completed) {
                holder.tvRemainingDaysAndTrips.visibility = View.GONE
                /*holder.tvRemainingDaysAndTrips.setTextColor(mContext.getResources().getColor(R.color.ub__green));
                holder.tvRemainingDaysAndTrips.setText(mContext.getString(R.string.referral_completed));*/
            }
        }

        try {
            Picasso.get().load(referredFriendsModelArrayListSingleObject.profileImage).into(holder.profilePicture)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return referredFriendsModelArrayList!!.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val profilePicture: ImageView
        internal val tvReferredFriend: TextView
        internal val tvRemainingDaysAndTrips: TextView
        internal val tvAmountGain: TextView

        init {
            profilePicture = view.findViewById(R.id.profile_image1)
            tvReferredFriend = view.findViewById(R.id.tv_referral_friend_name)
            tvRemainingDaysAndTrips = view.findViewById(R.id.tv_remaining_days_and_trips)
            tvAmountGain = view.findViewById(R.id.tv_amount_gain)
        }
    }
}
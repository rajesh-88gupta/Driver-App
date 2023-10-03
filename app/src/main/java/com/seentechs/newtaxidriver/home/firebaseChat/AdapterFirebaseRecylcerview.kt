package com.seentechs.newtaxidriver.home.firebaseChat

import android.content.Context
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys

import java.util.ArrayList

import javax.inject.Inject

class AdapterFirebaseRecylcerview(private val mContext: Context) : RecyclerView.Adapter<AdapterFirebaseRecylcerview.RecyclerViewHolder>() {

    lateinit @Inject
    var sessionManager: SessionManager

    private val inflater: LayoutInflater
    private val chatList = ArrayList<FirebaseChatModelClass>()

    init {
        inflater = LayoutInflater.from(mContext)
        AppController.getAppComponent().inject(this)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = inflater.inflate(R.layout.adapter_firebase_chat_single_row, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        if (chatList[position].type == CommonKeys.FIREBASE_CHAT_TYPE_DRIVER) {
            holder.myMessageCard.visibility = View.VISIBLE
            holder.opponentChatMessageLayout.visibility = View.GONE
            holder.myMessage.text = chatList[position].message
        } else {
            holder.opponentChatMessageLayout.visibility = View.VISIBLE
            holder.myMessageCard.visibility = View.GONE
            holder.opponentMessage.text = chatList[position].message

            handleOpponentProfilePicture(holder, position)
        }

    }

    private fun handleOpponentProfilePicture(holder: RecyclerViewHolder, position: Int) {
        try {
            if (position != 0) {
                if (chatList[position - 1].type == CommonKeys.FIREBASE_CHAT_TYPE_DRIVER) {
                    Picasso.get().load(sessionManager.riderProfilePic).error(R.drawable.car).into(holder.opponentProfileImageView)
                    holder.imageCardView.visibility = View.VISIBLE
                    holder.opponentProfileImageView.visibility = View.VISIBLE
                } else {
                    holder.opponentProfileImageView.visibility = View.INVISIBLE
                    holder.imageCardView.visibility = View.INVISIBLE
                }
            } else {
                Picasso.get().load(sessionManager.riderProfilePic).error(R.drawable.car).into(holder.opponentProfileImageView)
                holder.imageCardView.visibility = View.VISIBLE
                holder.opponentProfileImageView.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun updateChat(firebaseChatModelClass: FirebaseChatModelClass) {
        chatList.add(firebaseChatModelClass)
        notifyItemChanged(chatList.size - 1)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }


    class RecyclerViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var opponentMessage: TextView
        internal var myMessage: TextView
        internal var opponentProfileImageView: ImageView
        internal var myMessageCard: CardView
        internal var opponentChatMessageLayout: LinearLayout
        internal var imageCardView: CardView

        init {
            opponentMessage = itemView.findViewById(R.id.tv_opponent_message)
            myMessage = itemView.findViewById(R.id.tv_my_message)
            opponentProfileImageView = itemView.findViewById(R.id.imgv_opponent_profile_pic)
            imageCardView = itemView.findViewById(R.id.card_view)
            myMessageCard = itemView.findViewById(R.id.cv_my_messages)
            opponentChatMessageLayout = itemView.findViewById(R.id.lv_opponnent_chat_messages)
        }
    }
}

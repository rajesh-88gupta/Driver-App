package com.seentechs.newtaxidriver.common.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.model.Support
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import javax.inject.Inject


class SupportAdapter(private val context: Context, private val supportList: ArrayList<Support>, public var onClickListener: OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        AppController.getAppComponent().inject(this)
    }

    lateinit var dialog: AlertDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.support_layout_service, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int = supportList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView
        (holder as ViewHolder).bindData(supportList[position], position)
    }


    interface OnClickListener {
        fun onClick(pos: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(support: Support, position: Int) {
            tvSupportName.setText(support.name)
            Picasso.get().load(support.image).into(ivSupport)

            rltSupport.setOnClickListener {
                onClickListener.onClick(position)
            }
        }


        var tvSupportName: TextView
        var rltSupport: RelativeLayout
        var ivSupport: ImageView


        init {
            tvSupportName = itemView.findViewById<View>(R.id.tv_support_name) as TextView
            rltSupport = itemView.findViewById<View>(R.id.rlt_support) as RelativeLayout
            ivSupport = itemView.findViewById<View>(R.id.cv_support) as ImageView

        }
    }


}


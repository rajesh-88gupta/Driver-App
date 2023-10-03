package com.seentechs.newtaxidriver.home.managevehicles.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.databinding.DriverDetailsLayoutBinding
import com.seentechs.newtaxidriver.home.datamodel.RiderDetailsModelList
import com.seentechs.newtaxidriver.common.network.AppController


class DriverDetailsAdapter(context : Context) : RecyclerView.Adapter<DriverDetailsAdapter.ViewHolder>(){



    lateinit private var riderDetailsModelList: List<RiderDetailsModelList>
    init {
        AppController.getAppComponent().inject(this)
    }
    private var riderClickListener: OnRiderClickListener? = null

    fun initRiderModel (make: List<RiderDetailsModelList>){
        this.riderDetailsModelList = make
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DriverDetailsLayoutBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = riderDetailsModelList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.bind(riderDetailsModelList[position])

    }


    interface OnRiderClickListener {
        fun setRiderClick(make: RiderDetailsModelList, position: Int)
    }


    fun setOnRiderClickListner(clickListner:OnRiderClickListener ) {
        this.riderClickListener = clickListner
    }



    inner class ViewHolder(val binding: DriverDetailsLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(riderDetailsModelList: RiderDetailsModelList) {
            binding.riderDetailsModelList = riderDetailsModelList

            binding.tvRiderName.setOnClickListener {
                riderClickListener?.setRiderClick(riderDetailsModelList,adapterPosition)
            }
            binding.executePendingBindings()
        }
    }


}

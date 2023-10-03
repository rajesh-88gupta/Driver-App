package com.seentechs.newtaxidriver.home.managevehicles.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.databinding.YearLayoutBinding
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import javax.inject.Inject

class YearAdapter(context : Context) : RecyclerView.Adapter<YearAdapter.ViewHolder>(){


    lateinit var dialog: AlertDialog
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var customDialog: CustomDialog


    lateinit private var year: List<Int>
    private var currentYear: String = ""
    init {
        AppController.getAppComponent().inject(this)
    }
    private var yearClickListener: onYearClickListener? = null

    fun initYearModel (year: List<Int>){
        this.year = year
    }

    fun initCurrentYear (currentYear: String){
        this.currentYear = currentYear
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = YearLayoutBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = year.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.bind(year[position])
        holder.setIsRecyclable(false)
    }


    interface onYearClickListener {
        fun setYearClick(year: Int, position: Int)
    }


    fun setOnYearClickListner(clickListner:onYearClickListener ) {
        this.yearClickListener = clickListner
    }



    inner class ViewHolder(val binding: YearLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(year: Int) {
            binding.tvVehicleYear.text = year.toString()
            binding.ivTick.visibility = View.GONE
            if(currentYear.equals(year.toString())){
                binding.ivTick.visibility = View.VISIBLE
            }else{
                binding.ivTick.visibility = View.GONE
            }

            binding.tvVehicleYear.setOnClickListener {
                yearClickListener?.setYearClick(year,adapterPosition)
            }
            binding.executePendingBindings()
        }
    }


}
package com.seentechs.newtaxidriver.home.managevehicles.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.databinding.MakeLayoutBinding
import com.seentechs.newtaxidriver.home.datamodel.Make
import com.seentechs.newtaxidriver.common.network.AppController


class MakeAdapter(context : Context) : RecyclerView.Adapter<MakeAdapter.ViewHolder>(){



    lateinit private var make: List<Make>
    init {
        AppController.getAppComponent().inject(this)
    }
    private var makeClickListener: onMakeClickListener? = null

    fun initMakeModel (make: List<Make>){
        this.make = make
    }
    private var currentMake: String = ""



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MakeLayoutBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = make.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.bind(make[position])

    }


    interface onMakeClickListener {
        fun setMakeClick(make: Make, position: Int)
    }


    fun setOnMakeClickListner(clickListner:onMakeClickListener ) {
        this.makeClickListener = clickListner
    }

    fun initCurrentMake(make: String) {
        currentMake = make
    }


    inner class ViewHolder(val binding: MakeLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(make: Make) {
            binding.make = make

            if(make.name.equals(currentMake))
                binding.ivTick.visibility = View.VISIBLE
            else
                binding.ivTick.visibility = View.GONE

            binding.tvCarName.setOnClickListener {
                makeClickListener?.setMakeClick(make,adapterPosition)
            }
            binding.executePendingBindings()
        }
    }


}

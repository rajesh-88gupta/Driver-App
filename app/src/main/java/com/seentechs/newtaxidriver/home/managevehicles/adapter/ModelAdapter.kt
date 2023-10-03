package com.seentechs.newtaxidriver.home.managevehicles.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.databinding.ModelLayoutBinding
import com.seentechs.newtaxidriver.home.datamodel.Model
import com.seentechs.newtaxidriver.common.network.AppController


class ModelAdapter(context : Context) : RecyclerView.Adapter<ModelAdapter.ViewHolder>(){



    lateinit private var model: List<Model>
    init {
        AppController.getAppComponent().inject(this)
    }
    private var modelClickListener: onModelClickListener? = null
    private var currentModel: String = ""

    fun initModel (model: List<Model>){
        this.model = model
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ModelLayoutBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = model.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.bind(model[position])

    }


    interface onModelClickListener {
        fun setModelClick(make: Model, position: Int)
    }


    fun setOnModelClickListner(clickListner:onModelClickListener ) {
        this.modelClickListener = clickListner
    }

    fun initCurrentModel(model: String) {
        currentModel = model
    }


    inner class ViewHolder(val binding: ModelLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Model) {
            binding.model = model

            if(model.name.equals(currentModel))
                binding.ivTick.visibility = View.VISIBLE
            else
                binding.ivTick.visibility = View.GONE

            binding.tvVehicleModel.setOnClickListener {
                modelClickListener?.setModelClick(model,adapterPosition)
            }
            binding.executePendingBindings()
        }
    }


}

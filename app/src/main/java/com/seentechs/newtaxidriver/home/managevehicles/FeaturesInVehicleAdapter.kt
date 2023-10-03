package com.seentechs.newtaxidriver.home.managevehicles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.databinding.UpdateFiltersBinding

class FeaturesInVehicleAdapter(private val isNewVehicle:Boolean,private val featuresInCarModel: List<FeaturesInCarModel>, private var featureSelectListener: FeatureSelectListener) : RecyclerView.Adapter<FeaturesInVehicleAdapter.ViewHolder>() {

    init {
        AppController.getAppComponent().inject(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturesInVehicleAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UpdateFiltersBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return featuresInCarModel.size
    }

    override fun onBindViewHolder(holder: FeaturesInVehicleAdapter.ViewHolder, position: Int) {
       holder.bind(featuresInCarModel[position])
    }


    inner class ViewHolder(val binding: UpdateFiltersBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(featuresInCarModel: FeaturesInCarModel) {
            binding.tvFilters.text = featuresInCarModel.name
            if (isNewVehicle){
                binding.cbSelectFilter.isChecked = false
            }else {
                binding.cbSelectFilter.isChecked = featuresInCarModel.isSelected
            }
            binding.cbSelectFilter.setOnCheckedChangeListener { buttonView, isChecked ->
                featureSelectListener.onFeatureChoosed(featuresInCarModel.id,isChecked)
            }
            binding.features = featuresInCarModel
            binding.executePendingBindings()
        }

    }
}
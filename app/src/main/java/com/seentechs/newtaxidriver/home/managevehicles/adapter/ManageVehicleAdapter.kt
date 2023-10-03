package com.seentechs.newtaxidriver.home.managevehicles.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.databinding.VehiclesLayoutBinding
import com.seentechs.newtaxidriver.home.datamodel.VehiclesModel
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import javax.inject.Inject


class ManageVehicleAdapter(val context : Context, private val vehiclesModelList: List<VehiclesModel>, public var onClickListener : OnClickListener) : RecyclerView.Adapter<ManageVehicleAdapter.ViewHolder>(){

    init {
        AppController.getAppComponent().inject(this)
    }

    lateinit var dialog: AlertDialog
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = VehiclesLayoutBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = vehiclesModelList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.bind(vehiclesModelList[position])


    }


    interface OnClickListener {
        fun onClick(pos:Int ,clickType :String)
    }

    inner class ViewHolder(val binding: VehiclesLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vehicleType: VehiclesModel) {
            binding.vehicleType = vehicleType

            if(vehicleType.isActive == 0){
              binding.tvStatus.setTextColor(context.resources.getColor(R.color.red_text))
            }else{
                binding.tvStatus.setTextColor(context.resources.getColor(R.color.newtaxi_app_navy))
            }

            binding.ivDocument.setOnClickListener({
               /* if(sessionManager.driverStatus.equals("online",ignoreCase = true))
                {
                    Toast.makeText(context,context.getString(R.string.vehicleAlert),Toast.LENGTH_SHORT).show()
                  //  commonMethods.showMessage(context,dialog,"since your are online ,you can't edit your vehicles,please completed your trips ")
                }else{

                }*/

                onClickListener.onClick(adapterPosition,CommonKeys.DOCUMENT)

            })
            binding.ivEdit.setOnClickListener({
                /*if(sessionManager.driverStatus.equals("online",ignoreCase = true))
                {
                    Toast.makeText(context,context.getString(R.string.vehicleAlert),Toast.LENGTH_SHORT).show()
                    //  commonMethods.showMessage(context,dialog,"since your are online ,you can't edit your vehicles,please completed your trips ")
                }else{
                    onClickListener.onClick(adapterPosition,CommonKeys.EDIT)
                }*/
                onClickListener.onClick(adapterPosition,CommonKeys.EDIT)

            })
            binding.ivDelete.setOnClickListener({
                /*if(sessionManager.driverStatus.equals("online",ignoreCase = true))
                {
                    Toast.makeText(context,context.getString(R.string.deleteAlert),Toast.LENGTH_SHORT).show()
                }else{
                    onClickListener.onClick(adapterPosition,CommonKeys.DELETE)
                }*/
                onClickListener.onClick(adapterPosition,CommonKeys.DELETE)
            })
            binding.executePendingBindings()
        }
    }


}


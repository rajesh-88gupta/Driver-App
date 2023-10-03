package com.seentechs.newtaxidriver.taxiapp.views.permissionoverview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.data.local.ApplicationPermissionModel
import com.seentechs.newtaxidriver.databinding.ItemPermissionListBinding

class PermissionOverViewAdapter(
    var permissionList: List<ApplicationPermissionModel>,
    val context: Context
) : RecyclerView.Adapter<PermissionOverViewAdapter.PermissionVH>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemPermissionListBinding>(
            layoutInflater,
            R.layout.item_permission_list,
            parent,
            false
        )

        return PermissionVH(binding)
    }

    override fun onBindViewHolder(holder: PermissionVH, position: Int) {
        holder.bind(permissionList[position])
    }

    override fun getItemCount(): Int {
        return permissionList.size
    }

    class PermissionVH(val binding: ItemPermissionListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: ApplicationPermissionModel) {
            binding.imageViewIcon.setImageResource(model.imageId)
            binding.textViewTitle.text = model.title
            binding.textViewDescription.text = model.description
        }
    }
}
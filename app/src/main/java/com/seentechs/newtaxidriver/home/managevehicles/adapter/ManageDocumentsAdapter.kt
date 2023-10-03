
package com.seentechs.newtaxidriver.home.managevehicles.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.databinding.ManageDocumentsLayoutBinding
import com.seentechs.newtaxidriver.home.datamodel.DocumentsModel
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import javax.inject.Inject


class ManageDocumentsAdapter(val context : Context,private val documentModel: List<DocumentsModel>, var onClickListener : OnClickListener) : RecyclerView.Adapter<ManageDocumentsAdapter.ViewHolder>(){

    @Inject
    lateinit var commonMethods: CommonMethods


    init {
        AppController.getAppComponent().inject(this)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ManageDocumentsLayoutBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = documentModel.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.bind(documentModel[position],position)
    }

    interface OnClickListener {
        fun onClick(pos:Int)
    }

    inner class ViewHolder(val binding: ManageDocumentsLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(docType: DocumentsModel,position: Int) {


            binding.docType = docType
            binding.rltManageDoc.setOnClickListener{
                onClickListener.onClick(position)
            }
            binding.executePendingBindings()
        }
    }


}


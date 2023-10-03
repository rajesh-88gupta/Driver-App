package com.seentechs.newtaxidriver.home.datamodel

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.databinding.library.baseAdapters.BR
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import com.seentechs.newtaxidriver.R
import java.io.Serializable


class DocumentsModel : Serializable, BaseObservable() {
    @SerializedName("name")
    @Expose
    @get:Bindable
    var documentName: String? = null
        set(documentName) {
            field = documentName
            notifyPropertyChanged(BR.documentName)
        }

    @SerializedName("id")
    @Expose
    var documentId: Int? = null

    @SerializedName("document")
    @Expose
    @get:Bindable
    var documentUrl: String? = null
        set(documentUrl) {
            field = documentUrl
            notifyPropertyChanged(BR.documentUrl)
        }


    @SerializedName("status")
    @Expose
    @get:Bindable
    var documentStatus = ""
        set(documentStatus) {
            field = documentStatus
            notifyPropertyChanged(BR.documentStatus)
        }


    @SerializedName("expired_date")
    @Expose
    @get:Bindable
    var expiredDate = ""
        set(expiredDate) {
            field = expiredDate
            notifyPropertyChanged(BR.expiredDate)
        }

    @SerializedName("expiry_required")
    @Expose
    @get:Bindable
    var expiryRequired = ""
        set(expiryRequired) {
            field = expiryRequired
            notifyPropertyChanged(BR.expiryRequired)
        }
    companion object {
        @BindingAdapter("android:setStatus")
        @JvmStatic
        fun setStatus(view: TextView, documentsModel: DocumentsModel) {
            
            if(documentsModel.documentUrl.equals("")) {
                view.visibility = View.VISIBLE
                view.setTextColor(ContextCompat.getColor(view.context,R.color.red_text))
                view.text = view.context.getString(R.string.upload_doc)

            }else if (documentsModel.documentStatus.equals("0")) {
                view.visibility = View.VISIBLE
                view.setTextColor(ContextCompat.getColor(view.context,R.color.red_text))
                view.text = view.context.getString(R.string.pending)

            }else if (documentsModel.documentStatus.equals("2")) {
                view.visibility = View.VISIBLE
                view.setTextColor(ContextCompat.getColor(view.context,R.color.red_text))
                view.text = view.context.getString(R.string.rejected)

            }else if (documentsModel.documentStatus.equals("1")) {
                view.visibility = View.VISIBLE
                view.setTextColor(ContextCompat.getColor(view.context,R.color.green_text_color))
                view.text = view.context.getString(R.string.approved)
            }

        }

        @BindingAdapter("android:loadImage")
        @JvmStatic
        fun loadImage(view: ImageView, url: String) {

            if(!url.equals(""))
            {
                Picasso.get().load(url).into(view)
            }


        }

        @BindingAdapter("android:changeText")
        @JvmStatic
        fun changeText(view:TextView,url: String)
        {
            if(!url.equals(""))
              view.setText(view.context.getString(R.string.taptochange))
            else
                view.setText(view.context.getString(R.string.taptoadd))

        }
    }


}

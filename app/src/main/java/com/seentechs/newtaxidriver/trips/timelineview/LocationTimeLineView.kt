package com.seentechs.newtaxidriver.trips.timelineview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R


class LocationTimeLineView(private val context: Context, private val addressList: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_TOP = 0;
    private val VIEW_TYPE_MIDDLE = 1;
    private val VIEW_TYPE_BOTTOM = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder?
        val inflater = LayoutInflater.from(parent.context)
        val viewItem: View

        viewItem = inflater.inflate(R.layout.app_pick_drop_location, parent, false)

        viewHolder = ViewHolder(viewItem)

        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TYPE_TOP
        } else if (position == addressList.size - 1) {
            return VIEW_TYPE_BOTTOM
        }
        return VIEW_TYPE_MIDDLE;
    }


    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: String = addressList[position]
        val holder = holder as ViewHolder


        if (position == 1) {
            holder.addressTitle.text = context.resources.getString(R.string.drop_address)
            holder.vCircle.setColorFilter(ContextCompat.getColor(context, R.color.newtaxi_app_navy), android.graphics.PorterDuff.Mode.SRC_IN)
        } else {
            holder.addressTitle.text = context.resources.getString(R.string.pickup_address)
        }
        holder.mItemSubtitle.text = item
        when (holder.itemViewType) {
            VIEW_TYPE_TOP ->
                // The top of the line has to be rounded
                holder.mItemLine.background = ContextCompat.getDrawable(context, R.drawable.app_doted_line)
            VIEW_TYPE_MIDDLE -> {            // Only the color could be enough
                // but a drawable can be used to make the cap rounded also here
                if (position != 1) holder.mItemLine.background = ContextCompat.getDrawable(context, R.drawable.app_doted_line)
                if (position == 1) holder.mItemLine.visibility = View.VISIBLE
            }
            VIEW_TYPE_BOTTOM -> {
                if (position != 1) holder.mItemLine.background = ContextCompat.getDrawable(context, R.drawable.app_doted_line)
                if (position == 1) holder.mItemLine.visibility = View.GONE
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mItemSubtitle: TextView
        var addressTitle: TextView
        var mItemLine: View
        var vCircle: ImageView

        init {
            mItemSubtitle = itemView.findViewById(R.id.item_subtitle)
            addressTitle = itemView.findViewById(R.id.address_title)
            mItemLine = itemView.findViewById(R.id.v_top)
            vCircle = itemView.findViewById(R.id.v_circle)
        }
    }
}
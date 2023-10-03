package com.seentechs.newtaxidriver.trips.rating

/**
 * @package com.seentechs.newtaxidriver.trips.rating
 * @subpackage rating
 * @category CommentsRecycleAdapter
 * @author Seen Technologies
 *
 */

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView

import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.Locale

import javax.inject.Inject

/* ************************************************************
                CommentsRecycleAdapter
Its used to view the feedback comments with rider screen page function
*************************************************************** */
class CommentsRecycleAdapter(private val feedbackarraylist: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<CommentsRecycleAdapter.ViewHolder>() {

    @Inject

    lateinit var sessionManager: SessionManager


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CommentsRecycleAdapter.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.commant_cards_layout, viewGroup, false)

        AppController.getAppComponent().inject(this)
        return ViewHolder(view)
    }

    /*
   *  Get rider feedback list bind
   */
    override fun onBindViewHolder(viewHolder: CommentsRecycleAdapter.ViewHolder, i: Int) {
        if(feedbackarraylist[i]["rating_comments"] == ""){
           viewHolder.comant.visibility = View.GONE
        }else {
            viewHolder.comant.visibility = View.VISIBLE
        }
        viewHolder.comant.text = feedbackarraylist[i]["rating_comments"]
        viewHolder.date.text = feedbackarraylist[i]["date"]
        viewHolder.go_rating.rating = java.lang.Float.parseFloat(feedbackarraylist[i]["rating"]!!)
        val originalFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)
        var targetFormat: DateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        if (sessionManager.languageCode == "es") {
            targetFormat = SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES"))
        } else if (sessionManager.languageCode == "fa") {
            targetFormat = SimpleDateFormat("dd MMMM yyyy", Locale("fa", "AF"))
        } else if (sessionManager.languageCode == "ar") {
            targetFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ar", "DZ"))
        }
        var date: Date?
        try {
            date = originalFormat.parse(feedbackarraylist[i]["date"])
            val dat = targetFormat.format(date)



            viewHolder.date.text = dat
        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return feedbackarraylist.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal val go_rating: RatingBar
        internal val comant: TextView
        internal val date: TextView


        init {

            comant = view.findViewById<View>(R.id.comant) as TextView
            date = view.findViewById<View>(R.id.date) as TextView
            go_rating = view.findViewById<View>(R.id.go_rating) as RatingBar
        }
    }


}

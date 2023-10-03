package com.seentechs.newtaxidriver.trips.rating

/**
 * @package com.seentechs.newtaxidriver.trips.rating
 * @subpackage rating
 * @category CommentsRecycleAdapter
 * @author Seen Technologies
 *
 */

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.home.datamodel.RiderFeedBackArrayModel
import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/* ************************************************************
                CommentsRecycleAdapter
Its used to view the feedback comments with rider screen page function
*************************************************************** */
class CommentsPaginationAdapter(private val context: Context, private val mCallback: PaginationAdapterCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM = 0
    private val LOADING = 1
    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var errorMsg: String? = null

    private var feedbackArraylist: ArrayList<RiderFeedBackArrayModel> = ArrayList()

    private var feedBackModel = RiderFeedBackArrayModel()


    lateinit var dialog: AlertDialog

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    init {
        AppController.getAppComponent().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem = inflater.inflate(R.layout.commant_cards_layout, parent, false)
                viewHolder = RiderFeedbackViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return if (feedbackArraylist == null) 0 else feedbackArraylist.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val feedbackData = feedbackArraylist[position]
        when (getItemViewType(position)) {
            ITEM -> {
                val viewHolder = holder as RiderFeedbackViewHolder

                viewHolder.comment.text = feedbackData.riderComments
                viewHolder.date.text = feedbackData.date
                viewHolder.goRating.rating = java.lang.Float.parseFloat(feedbackData.riderRating)

                val originalFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)
                var targetFormat: DateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                when (sessionManager.languageCode) {
                    "es" -> {
                        targetFormat = SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES"))
                    }
                    "fa" -> {
                        targetFormat = SimpleDateFormat("dd MMMM yyyy", Locale("fa", "AF"))
                    }
                    "ar" -> {
                        targetFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ar", "DZ"))
                    }
                }
                var date: Date?
                try {
                    date = originalFormat.parse(feedbackData.date)
                    val dat = targetFormat.format(date)
                    viewHolder.date.text = dat
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
            LOADING -> {
                val loadingVH = holder as LoadingViewHolder

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.visibility = View.VISIBLE
                    loadingVH.mProgressBar.visibility = View.GONE

                    loadingVH.mErrorTxt.text = if (errorMsg != null)
                        errorMsg
                    else
                        context.getString(R.string.error_msg_unknown)

                } else {
                    loadingVH.mErrorLayout.visibility = View.GONE
                    loadingVH.mProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        feedBackModel = feedbackArraylist[position]
        return if (position == feedbackArraylist.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    /*
      Helpers - Pagination
    */

    fun add(feedBackModel: RiderFeedBackArrayModel) {
        this.feedbackArraylist.add(feedBackModel)
        notifyItemInserted(this.feedbackArraylist.size - 1)
    }

    fun addAll(feedBackArrayList: ArrayList<RiderFeedBackArrayModel>) {
        for (feedBackModel in feedBackArrayList) {
            add(feedBackModel)
        }
    }

    fun remove(feedBackModel: RiderFeedBackArrayModel?) {
        val position = feedbackArraylist.indexOf(feedBackModel)
        if (position > -1) {
            feedbackArraylist.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    fun clearAll() {
        isLoadingAdded = false
        feedbackArraylist.clear()
        notifyDataSetChanged()
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(RiderFeedBackArrayModel())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = feedbackArraylist.size - 1
        val tripStatusModel = getItem(position)

        if (tripStatusModel != null) {
            feedbackArraylist.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getItem(position: Int): RiderFeedBackArrayModel? {
        return feedbackArraylist[position]
    }


    inner class RiderFeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goRating: RatingBar = itemView.findViewById<View>(R.id.go_rating) as RatingBar
        val comment: TextView = itemView.findViewById<View>(R.id.comant) as TextView
        val date: TextView = itemView.findViewById<View>(R.id.date) as TextView
    }


    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mProgressBar: ProgressBar = itemView.findViewById(R.id.loadmore_progress)
        val mRetryBtn: ImageButton = itemView.findViewById(R.id.loadmore_retry)
        val mErrorTxt: TextView = itemView.findViewById(R.id.loadmore_errortxt)
        val mErrorLayout: LinearLayout = itemView.findViewById(R.id.loadmore_errorlayout)


        init {

            mRetryBtn.setOnClickListener(this)
            mErrorLayout.setOnClickListener(this)
        }


        override fun onClick(view: View) {
            when (view.id) {
                R.id.loadmore_retry, R.id.loadmore_errorlayout -> {
                    showRetry(false, null)
                    mCallback.retryPageLoad()
                }
            }
        }
    }


    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(feedbackArraylist.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }


}

package com.seentechs.newtaxidriver.trips.rating

/**
 * @package com.seentechs.newtaxidriver.trips.rating
 * @subpackage rating
 * @category Comments
 * @author Seen Technologies
 *
 */


import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.database.Sqlite
import com.seentechs.newtaxidriver.common.helper.Constants
import com.seentechs.newtaxidriver.common.helper.Constants.PAGE_START
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.PaginationScrollListener
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.common.views.CommonActivity
import com.seentechs.newtaxidriver.home.datamodel.RiderFeedBackArrayModel
import com.seentechs.newtaxidriver.home.datamodel.RiderFeedBackModel
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.PaginationAdapterCallback
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import kotlinx.android.synthetic.main.activity_comments.*
import org.json.JSONException
import java.util.*
import javax.inject.Inject

/* ************************************************************
                Comment
Its used to view the comments with rider screen page function
*************************************************************** */
class Comments : CommonActivity(), ServiceListener, PaginationAdapterCallback {

    @Inject
    lateinit var dbHelper: Sqlite
    private var isViewUpdatedWithLocalDB: Boolean = false


    lateinit var dialog: AlertDialog

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var customDialog: CustomDialog

    @BindView(R.id.my_recycler_view2)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.norating)
    lateinit var Def_rating_text: TextView

    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 0
    private var currentPage = PAGE_START

    var isInternetAvailable: Boolean = false

    lateinit var riderFeedBackModel: RiderFeedBackModel

    /**
     * Hash map for user comments api
     *
     * @return
     */

    private val userComments: HashMap<String, String>
        get() {
            val userRatingHashMap = HashMap<String, String>()
            userRatingHashMap["user_type"] = sessionManager.type!!
            userRatingHashMap["token"] = sessionManager.accessToken!!
            userRatingHashMap["page"] = currentPage.toString()

            return userRatingHashMap
        }

    lateinit var commentsPaginationAdapter: CommentsPaginationAdapter

    @OnClick(R.id.back)
    fun onpBack() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        dialog = commonMethods.getAlertDialog(this)

        /* common Header */
        commonMethods.setheaderText(resources.getString(R.string.riderfeedback), common_header)

        isInternetAvailable = commonMethods.isOnline(this)

        recyclerView.setHasFixedSize(false)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.isNestedScrollingEnabled = false


        commentsPaginationAdapter = CommentsPaginationAdapter(this, this)
        recyclerView.adapter = commentsPaginationAdapter

        recyclerView.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                if (commonMethods.isOnline(this@Comments)) {
                    isLoading = true
                    currentPage += 1
                    updateUserComments(false)
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGES
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        getUserComments()
    }


    private fun getUserComments() {
        val allHomeDataCursor: Cursor = dbHelper.getDocument(Constants.DB_KEY_RIDER_COMMENTS.toString())
        if (allHomeDataCursor.moveToFirst()) {
            isViewUpdatedWithLocalDB = true
            try {
                onSuccessComments(allHomeDataCursor.getString(0), true)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            followProcedureForNoDataPresentInDB()
        }
    }

    fun followProcedureForNoDataPresentInDB() {
        if (commonMethods.isOnline(this)) {
            updateUserComments(true)
        } else {
            CommonMethods.showNoInternetAlert(this, object : CommonMethods.INoInternetCustomAlertCallback {
                override fun onOkayClicked() {
                    finish()
                }

                override fun onRetryClicked() {
                    followProcedureForNoDataPresentInDB()
                }

            })
        }
    }

    private fun initView() {

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data))
                commonMethods.showMessage(this, dialog, data)
            return
        }

        if (jsonResp.isSuccess) {
            val getCurrentPage = commonMethods.getJsonValue(jsonResp.strResponse, "current_page", Int::class.java) as Int
            currentPage = getCurrentPage
            if (currentPage == 1) {
                dbHelper.insertWithUpdate(Constants.DB_KEY_RIDER_COMMENTS.toString(), jsonResp.strResponse)
                onSuccessComments(jsonResp.strResponse, false)
            } else {
                onLoadMoreComments(jsonResp.strResponse)
            }
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }


    /**
     * success response for comments
     *
     * @param jsonResp
     */

    private fun onSuccessComments(jsonResp: String, isFromDatabase: Boolean) {
        riderFeedBackModel = gson.fromJson(jsonResp, RiderFeedBackModel::class.java)
        if (riderFeedBackModel != null) {
            if (riderFeedBackModel.riderFeedBack?.size!! > 0) {
                commentsPaginationAdapter.clearAll()
                Def_rating_text.text = resources.getString(R.string.ratingsncomment)
                Def_rating_text.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                TOTAL_PAGES = riderFeedBackModel.totalPages?.toInt()!!
                commentsPaginationAdapter.addAll(riderFeedBackModel.riderFeedBack as ArrayList<RiderFeedBackArrayModel>)
                commentsPaginationAdapter.notifyDataSetChanged()
                if (isFromDatabase) {
                    isLastPage = true
                    if (isViewUpdatedWithLocalDB) {
                        isViewUpdatedWithLocalDB = false
                        currentPage = 1
                        updateUserComments(false)
                    }
                } else {
                    if (currentPage <= TOTAL_PAGES && TOTAL_PAGES > 1) {
                        isLastPage = false
                        commentsPaginationAdapter.addLoadingFooter()
                    } else
                        isLastPage = true
                }
            } else {
                if (isFromDatabase) {
                    isLastPage = true
                    if (isViewUpdatedWithLocalDB) {
                        isViewUpdatedWithLocalDB = false
                        currentPage = 1
                        updateUserComments(false)
                    }
                }
                Def_rating_text.text = resources.getString(R.string.noratings)
                Def_rating_text.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        } else {
            if (isFromDatabase) {
                isLastPage = true
                if (isViewUpdatedWithLocalDB) {
                    isViewUpdatedWithLocalDB = false
                    currentPage = 1
                    updateUserComments(false)
                }
            }
            Def_rating_text.text = resources.getString(R.string.noratings)
            Def_rating_text.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    private fun onLoadMoreComments(response: String) {
        riderFeedBackModel = gson.fromJson(response, RiderFeedBackModel::class.java)
        TOTAL_PAGES = riderFeedBackModel.totalPages?.toInt()!!
        commentsPaginationAdapter.removeLoadingFooter()
        isLoading = false

        commentsPaginationAdapter.addAll(riderFeedBackModel.riderFeedBack as ArrayList<RiderFeedBackArrayModel>)
        commentsPaginationAdapter.notifyDataSetChanged()
        if (currentPage != TOTAL_PAGES)
            commentsPaginationAdapter.addLoadingFooter()
        else
            isLastPage = true
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        commonMethods.hideProgressDialog()
        commonMethods.showMessage(this, dialog, jsonResp.statusMsg)

    }

    /**
     * User Comments Api Call
     */

    private fun updateUserComments(showLoader: Boolean) {
        if (commonMethods.isOnline(this)) {
            if (currentPage == 1) {
                if (showLoader) {
                    commonMethods.showProgressDialog(this as CommonActivity)
                }
            }
            apiService.updateRiderFeedBack(userComments).enqueue(RequestCallback(this))
        } else {
            CommonMethods.showInternetNotAvailableForStoredDataViewer(this)
        }
    }

    override fun retryPageLoad() {
        updateUserComments(false)
    }
}

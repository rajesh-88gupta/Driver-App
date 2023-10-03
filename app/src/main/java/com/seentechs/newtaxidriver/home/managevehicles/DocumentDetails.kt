package com.seentechs.newtaxidriver.home.managevehicles

import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.home.datamodel.DocumentsModel
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonKeys
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.views.CommonActivity
import javax.inject.Inject


class DocumentDetails : CommonActivity() {
    @OnClick(R.id.ivBack)
    fun onBack() {
        onBackPressed()
    }

    @BindView(R.id.tvTitle)
    lateinit var tvTitle: TextView

    @Inject
    lateinit var commonMethods: CommonMethods

    var documentDetails  = ArrayList<DocumentsModel>()
    var documentPosition  : Int?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_details)

        ButterKnife.bind(this)
        AppController.getAppComponent().inject(this)
        getIntentValues()
    }

    private fun getIntentValues() {

       if(intent.extras!=null)
       {
           documentDetails = intent.getSerializableExtra(CommonKeys.Intents.DocumentDetailsIntent) as ArrayList<DocumentsModel>
           setHeader(getString(R.string.manage_documents))
       }

    }




    internal fun getAppCompatActivity() : CommonActivity {
        return this
    }


    internal fun setHeader(title: String) {
        tvTitle.text = title
    }


}

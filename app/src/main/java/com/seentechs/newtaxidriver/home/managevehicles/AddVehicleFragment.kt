package com.seentechs.newtaxidriver.home.managevehicles

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.gson.Gson
import com.seentechs.newtaxidriver.R
import com.seentechs.newtaxidriver.common.configs.SessionManager
import com.seentechs.newtaxidriver.common.helper.CustomDialog
import com.seentechs.newtaxidriver.common.model.JsonResponse
import com.seentechs.newtaxidriver.common.network.AppController
import com.seentechs.newtaxidriver.common.util.CommonMethods
import com.seentechs.newtaxidriver.common.util.Enums.ADD_UPDATE_VEHICLE
import com.seentechs.newtaxidriver.common.util.RequestCallback
import com.seentechs.newtaxidriver.home.datamodel.AddedVehiclesModel
import com.seentechs.newtaxidriver.home.datamodel.Make
import com.seentechs.newtaxidriver.home.datamodel.Model
import com.seentechs.newtaxidriver.home.datamodel.VehicleTypes
import com.seentechs.newtaxidriver.home.interfaces.ApiService
import com.seentechs.newtaxidriver.home.interfaces.ServiceListener
import com.seentechs.newtaxidriver.home.managevehicles.adapter.MakeAdapter
import com.seentechs.newtaxidriver.home.managevehicles.adapter.ModelAdapter
import com.seentechs.newtaxidriver.home.managevehicles.adapter.YearAdapter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet


class AddVehicleFragment : androidx.fragment.app.Fragment(), VehicleTypeAdapter.OnClickListener, ServiceListener, FeatureSelectListener {


    private var addedVehicleDetails = AddedVehiclesModel()

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

    var make: Make? = null
    var model: Model? = null

    private var dialog: AlertDialog? = null
    private var makePosition: Int = 0
    private lateinit var yearAdapter: YearAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var makeAdapter: MakeAdapter
    private lateinit var modelAdapter: ModelAdapter

    private var yearList = ArrayList<Int>()
    private lateinit var rvVehicleDesc: RecyclerView
    var vehicleDescriptionDialog: android.app.AlertDialog? = null

    lateinit var addVehicles: View
    lateinit var vehicleTypeAdapter: VehicleTypeAdapter

    lateinit var featuresInVehicleAdapter: FeaturesInVehicleAdapter

    @BindView(R.id.rv_vehicle_type)
    lateinit var rvVehicles: RecyclerView

    @BindView(R.id.rv_features_list)
    lateinit var rvFeatures: RecyclerView

    private var selectedIds = LinkedHashSet<Int>()

    @BindView(R.id.tv_make_type)
    lateinit var tvMakeType: TextView

    @BindView(R.id.tv_license_type)
    lateinit var tvLicenseNumber: TextView

    @BindView(R.id.tv_vehicle_color)
    lateinit var tvVehicleColor: TextView


    @BindView(R.id.btnAddVehicle)
    lateinit var btnAddVehicle: Button


    @BindView(R.id.tv_model_type)
    lateinit var tvModelType: TextView


    @BindView(R.id.tv_year_type)
    lateinit var tvYearType: TextView


    @BindView(R.id.tv_vehicle_name)
    lateinit var tvVehicleName: TextView


    @OnClick(R.id.rlt_make)
    fun onMake() {
        vehicleDescPopup(0)
    }


    @OnClick(R.id.rlt_model)
    fun onModel() {
        vehicleDescPopup(1)
    }


    @OnClick(R.id.btnAddVehicle)
    fun onBtnAdd() {


        vehTypeSelcIds = ""

        for (i in (activity as ManageVehicles).makeModelDetails.vehicleTypes.indices) {

            if ((activity as ManageVehicles).makeModelDetails.vehicleTypes.get(i).isChecked) {
                if (vehTypeSelcIds.equals("")) {
                    vehTypeSelcIds = (activity as ManageVehicles).makeModelDetails.vehicleTypes.get(i).id.toString()
                } else {
                    vehTypeSelcIds = vehTypeSelcIds + "," + (activity as ManageVehicles).makeModelDetails.vehicleTypes.get(i).id.toString()
                }
            }

        }



        if (isParamsNotEmpty()) {
            updateVehicleApi()
        }


    }

    private fun isParamsNotEmpty(): Boolean {

        if (tvMakeType.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_make), Toast.LENGTH_LONG).show()
            tvMakeType.setError(resources.getString(R.string.please_choose_make))
            tvMakeType.requestFocus()
            return false
        }
        if (tvModelType.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_model), Toast.LENGTH_LONG).show()
            tvModelType.setError(resources.getString(R.string.please_choose_model))
            tvModelType.requestFocus()
            return false
        }
        if (tvYearType.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_year), Toast.LENGTH_LONG).show()
            tvYearType.setError(resources.getString(R.string.please_choose_year))
            tvYearType.requestFocus()
            return false
        }
        if (tvLicenseNumber.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_license_number), Toast.LENGTH_LONG).show()
            tvLicenseNumber.setError(resources.getString(R.string.please_choose_license_number))
            tvLicenseNumber.requestFocus()
            return false
        }
        /* if(tvVehicleName.text.toString().equals("")){
              Toast.makeText(context,resources.getString(R.string.please_enter_vehicle_name),Toast.LENGTH_LONG).show()
             tvVehicleName.setError(resources.getString(R.string.please_enter_vehicle_name))
             tvVehicleName.requestFocus()
             return false
         }*/
        if (tvVehicleColor.text.toString().equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_enter_vehicle_color), Toast.LENGTH_LONG).show()
            tvVehicleColor.setError(resources.getString(R.string.please_enter_vehicle_color))
            tvVehicleColor.requestFocus()
            return false
        }
        if (vehTypeSelcIds.equals("")) {
            Toast.makeText(context, resources.getString(R.string.please_choose_vehicle_type), Toast.LENGTH_LONG).show()
            return false
        }

        return true

    }


    var vehTypeSelcIds: String = ""


    @OnClick(R.id.rlt_year)
    fun onYear() {
        vehicleDescPopup(2)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        addVehicles = inflater.inflate(R.layout.add_vehicle_layout, container, false)
        AppController.getAppComponent().inject(this)
        ButterKnife.bind(this, addVehicles)

        initRecyclerView()
        initRequestOptionviews()
        initViews()
        (activity as ManageVehicles).hideAddButton()


        tvModelType.addTextChangedListener(TextCheckwatcher(tvModelType))
        tvMakeType.addTextChangedListener(TextCheckwatcher(tvMakeType))
        tvYearType.addTextChangedListener(TextCheckwatcher(tvYearType))
        tvVehicleColor.addTextChangedListener(TextCheckwatcher(tvVehicleColor))
        tvVehicleName.addTextChangedListener(TextCheckwatcher(tvVehicleName))

        return addVehicles
    }

    class TextCheckwatcher(val textview: TextView) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textview.setError(null)
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initViews() {
        dialog = commonMethods.getAlertDialog(context!!)
        if ((activity as ManageVehicles).vehicleClickPosition != null) {
            tvMakeType.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).make?.name
            tvModelType.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).model?.name
            tvYearType.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).year
            tvLicenseNumber.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).licenseNumber
            tvVehicleColor.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).vehicleColor
            tvVehicleName.text = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).vehicleName
            (activity as ManageVehicles).setHeader(getString(R.string.update_vehicles))
            btnAddVehicle.setText(getString(R.string.update_vehicle))
            getMakePosition()
            make = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).make
            model = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).model


        } else {
            (activity as ManageVehicles).setHeader(getString(R.string.add_vehicles))
            btnAddVehicle.setText(getString(R.string.add_vehicles))
        }
    }

    private fun getMakePosition() {

        for (i in (activity as ManageVehicles).makeModelDetails.make.indices) {
            if ((activity as ManageVehicles).makeModelDetails.make.get(i).name.equals(tvMakeType.text.toString())) {
                makePosition = i
                break
            }
        }
    }


    /**
     * type 1 : Make
     */

    // Load currency list deatils in dialog
    fun vehicleDescPopup(type: Int) {
        rvVehicleDesc = RecyclerView((activity as ManageVehicles).getContFromAct())
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.header, null)
        val header = view.findViewById<TextView>(R.id.header)



        if (type == 0) {
            initMakeRv()
            header.text = getString(R.string.choose_make)
        } else if (type == 1) {
            initModelRv()
            header.text = getString(R.string.choose_model)
        } else if (type == 2) {
            initYearRv()
            header.text = getString(R.string.choose_year)
        }




        vehicleDescriptionDialog = android.app.AlertDialog.Builder(context)
                .setCustomTitle(view)
                .setView(rvVehicleDesc)
                .setCancelable(true)
                .show()


    }


    private fun initYearRv() {

        var year = (activity as ManageVehicles).makeModelDetails.year
        yearList.clear()

        while (year <= Calendar.getInstance().get(Calendar.YEAR)) {
            yearList.add(year)
            year++

        }

        yearAdapter = YearAdapter((activity as ManageVehicles).getContFromAct())
        yearAdapter.initYearModel(yearList)
        //(activity as ManageVehicles).vehicleClickPosition?.let { (activity as ManageVehicles).vehicleDetails.get(it).year.let { yearAdapter.initCurrentYear(it) } }

        yearAdapter.initCurrentYear(tvYearType.text.toString())
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvVehicleDesc.layoutManager = linearLayoutManager
        rvVehicleDesc.adapter = yearAdapter

        /*  val laydir = getString(R.string.layout_direction)
          if ("1" == laydir)
              rvVehicleDesc.rotationY = 180f*/



        yearAdapter.setOnYearClickListner(object : YearAdapter.onYearClickListener {
            override fun setYearClick(year: Int, position: Int) {
                vehicleDescriptionDialog?.dismiss()
                /*if ((activity as ManageVehicles).vehicleClickPosition != null)
                    (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).year = yearList.get(position).toString()*/
                tvYearType.text = yearList.get(position).toString()
            }
        })
    }


    private fun initMakeRv() {


        makeAdapter = MakeAdapter((activity as ManageVehicles).getContFromAct())
        makeAdapter.initMakeModel((activity as ManageVehicles).makeModelDetails.make)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.reverseLayout = true
        rvVehicleDesc.layoutManager = linearLayoutManager
        rvVehicleDesc.setHasFixedSize(true)
        rvVehicleDesc.smoothScrollToPosition(0)
        rvVehicleDesc.adapter = makeAdapter
        /*  val laydir = getString(R.string.layout_direction)
          if ("1" == laydir)
              rvVehicleDesc.rotationX = 180f
  */
        makeAdapter.initCurrentMake(tvMakeType.text.toString())


        makeAdapter.setOnMakeClickListner(object : MakeAdapter.onMakeClickListener {
            override fun setMakeClick(selectedMake: Make, position: Int) {
                makePosition = position
                make = selectedMake
                vehicleDescriptionDialog?.dismiss()
                tvMakeType.text = (activity as ManageVehicles).makeModelDetails.make.get(position).name
                (activity as ManageVehicles).deselectMake()
                emptyYear()
                tvModelType.text = ""
                (activity as ManageVehicles).makeModelDetails.make.get(position).isSelected = true
            }
        })
    }

    fun emptyYear() {
        tvYearType.text = ""
    }


    private fun updateVehicleApi() {
        (activity as ManageVehicles).vehicleUpdate = false
        commonMethods.showProgressDialog((activity as ManageVehicles).getAppCompatActivity())
        apiService.updateVehicle(getVehicleHashMap()).enqueue(RequestCallback(ADD_UPDATE_VEHICLE, this))

    }

    private fun getVehicleHashMap(): LinkedHashMap<String, String> {

        var ids = ""
        var makeId: String? = null
        var modelId: String? = null

        if ((activity as ManageVehicles).vehicleClickPosition != null) {
            ids = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).id
        }

        if (make == null) {
            makeId = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).make?.id
        } else {
            makeId = make!!.id
        }

        if (make == null) {
            modelId = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).model?.id
        } else {
            modelId = model!!.id
        }


        val hashMap = LinkedHashMap<String, String>()
        hashMap["token"] = sessionManager.accessToken!!
        hashMap["id"] = ids
        hashMap["make_id"] = makeId!!
        hashMap["model_id"] = modelId!!
        hashMap["year"] = tvYearType.text.toString()
        hashMap["license_no"] = tvLicenseNumber.text.toString()
        hashMap["name"] = tvVehicleName.text.toString()
        hashMap["color"] = tvVehicleColor.text.toString()
        hashMap["vehicle_type"] = vehTypeSelcIds
        hashMap["options"] = TextUtils.join(",", selectedIds)

        return hashMap
    }


    private fun initModelRv() {


        modelAdapter = ModelAdapter((activity as ManageVehicles).getContFromAct())
        modelAdapter.initModel((activity as ManageVehicles).makeModelDetails.make.get(makePosition).model)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvVehicleDesc.layoutManager = linearLayoutManager
        rvVehicleDesc.setHasFixedSize(true)
        rvVehicleDesc.smoothScrollToPosition(0)
        rvVehicleDesc.adapter = modelAdapter
        /* val laydir = getString(R.string.layout_direction)
         if ("1" == laydir)
             rvVehicleDesc.rotationY = 180f*/
        modelAdapter.initCurrentModel(tvModelType.text.toString())




        modelAdapter.setOnModelClickListner(object : ModelAdapter.onModelClickListener {
            override fun setModelClick(selectedModel: Model, position: Int) {
                vehicleDescriptionDialog?.dismiss()
                model = selectedModel
                tvModelType.text = (activity as ManageVehicles).makeModelDetails.make.get(makePosition).model.get(position).name
                (activity as ManageVehicles).deselectModel(makePosition)
                emptyYear()
                (activity as ManageVehicles).makeModelDetails.make.get(makePosition).model.get(position).isSelected = true
            }
        })
    }


    override fun onClick(pos: Int, isChecked: Boolean) {


    }


    override fun onSuccess(jsonResp: JsonResponse, data: String) {

        commonMethods.hideProgressDialog()
        if (!jsonResp.isOnline) {
            if (!TextUtils.isEmpty(data)) commonMethods.showMessage(context, dialog, data)
            return
        }

        when (jsonResp.requestCode) {
            ADD_UPDATE_VEHICLE -> if (jsonResp.isSuccess) {
                onSuccessVehicleUpdated(jsonResp)
            } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
                commonMethods.showMessage(context, dialog, jsonResp.statusMsg)
            }

            else -> {
            }
        }
    }

    private fun onSuccessVehicleUpdated(jsonResp: JsonResponse) {
        addedVehicleDetails = gson.fromJson(jsonResp.strResponse, AddedVehiclesModel::class.java)
        (activity as ManageVehicles).vehicleDetails.clear()
        (activity as ManageVehicles).vehicleDetails.addAll(addedVehicleDetails.vehicle)

        //addVehicle()
        (activity as ManageVehicles).deselectVehicleType()
        (activity as ManageVehicles).deselectMake()
        (activity as ManageVehicles).deselectModel(makePosition)
        emptyYear()
        (activity as ManageVehicles).onBackPressed()

    }


    override fun onFailure(jsonResp: JsonResponse?, data: String?) {

    }

    fun initRecyclerView() {
        var vehicleTypes = ArrayList<VehicleTypes>()
        if ((activity as ManageVehicles).vehicleClickPosition != null) {

            vehicleTypes = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).vehicleTypes
        }
        vehicleTypeAdapter = VehicleTypeAdapter(context!!, vehicleTypes, (activity as ManageVehicles).makeModelDetails.vehicleTypes, this)
        rvVehicles.adapter = vehicleTypeAdapter
    }


    fun initRequestOptionviews() {
        var isAddNewVehicle = false
        if ((activity as ManageVehicles).makeModelDetails.requestOptions.isNotEmpty()) {
            if ((activity as ManageVehicles).vehicleClickPosition != null) {
                for (i in 0 until (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).requestOptions.size) {
                    (activity as ManageVehicles).makeModelDetails.requestOptions[i].isSelected = (activity as ManageVehicles).vehicleDetails.get((activity as ManageVehicles).vehicleClickPosition!!).requestOptions[i].isSelected
                }
            } else {
                isAddNewVehicle = true
                for (i in 0 until (activity as ManageVehicles).makeModelDetails.requestOptions.size) {
                    (activity as ManageVehicles).makeModelDetails.requestOptions[i].isSelected = false
                }
            }
            featuresInVehicleAdapter = FeaturesInVehicleAdapter(isAddNewVehicle, (activity as ManageVehicles).makeModelDetails.requestOptions, this)
            rvFeatures.adapter = featuresInVehicleAdapter
            rvFeatures.visibility = VISIBLE
        } else {
            rvFeatures.visibility = GONE
        }

        if (!isAddNewVehicle) {
            for (i in 0 until (activity as ManageVehicles).makeModelDetails.requestOptions.size) {
                if ((activity as ManageVehicles).makeModelDetails.requestOptions[i].isSelected) {
                    selectedIds.add((activity as ManageVehicles).makeModelDetails.requestOptions[i].id)
                }
            }
        }
    }

    override fun onFeatureChoosed(id: Int, isSelected: Boolean) {
        if (!isSelected) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
    }

}
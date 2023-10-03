package com.seentechs.newtaxidriver.trips.viewmodel

import androidx.lifecycle.LiveData
import com.seentechs.newtaxidriver.common.model.JsonResponse

interface RequestAcceptActivityInterface {

    fun onSuccessResponse(jsonResponse: LiveData<JsonResponse>)
    fun onFailureResponse(jsonResponse: LiveData<JsonResponse>)

}
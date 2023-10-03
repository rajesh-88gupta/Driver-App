package com.seentechs.newtaxidriver.home.interfaces;

import com.seentechs.newtaxidriver.common.model.JsonResponse;

/**
 * Created by Seen Technologies on 9/7/18.
 */

public interface ServiceListener {

    void onSuccess(JsonResponse jsonResp, String data);

    void onFailure(JsonResponse jsonResp, String data);


    /*void onSuccessResponse(JsonResponse jsonResp, String data);*/
}


package com.seentechs.newtaxidriver.home.interfaces

import okhttp3.RequestBody

/**
 * Created by Seen Technologies on 9/7/18.
 */

interface ImageListener {
    fun onImageCompress(filePath: String, requestBody: RequestBody?)
}


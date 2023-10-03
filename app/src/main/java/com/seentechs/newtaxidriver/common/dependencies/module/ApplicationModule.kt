package com.seentechs.newtaxidriver.common.dependencies.module

/**
 * @package com.seentechs.newtaxidriver
 * @subpackage dependencies.module
 * @category ApplicationModule
 * @author Seen Technologies
 *
 */

import android.app.Application

import com.seentechs.newtaxidriver.common.util.CommonMethods

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/*****************************************************************
 * Application Module
 */
@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    fun application(): Application {
        return application
    }

    @Provides
    @Singleton
    fun providesCommonMethods(): CommonMethods {
        return CommonMethods()
    }

    /* @Provides
    @Singleton
    public JsonResponse providesJsonResponse() {
        return new JsonResponse();
    }*/
}

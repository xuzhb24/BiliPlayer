package com.android.project

import com.android.base.BaseApplication
import com.android.http.Config

/**
 * Created by xuzhb on 2020/11/2
 */
class AppApplication : BaseApplication() {

    companion object {
        init {
            Config.switchEnvironment(BuildConfig.ENVIRONMENT)
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

}
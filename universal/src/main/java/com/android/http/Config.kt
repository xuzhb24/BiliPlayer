package com.android.http

import com.android.universal.BuildConfig

/**
 * Created by xuzhb on 2020/7/28
 * Desc:开发环境切换
 */
object Config {

    val DEBUG = BuildConfig.DEBUG       //是否是debug环境
    const val ENVIRONMENT_OFFICIAL = 1  //正式环境
    const val ENVIRONMENT_DEVELOP = 2   //测试环境

    var BASE_URL = ""

    var currentEnvironment = ENVIRONMENT_OFFICIAL  //当前开发环境

    //切换开发环境
    fun switchEnvironment(environment: Int) {
        currentEnvironment = environment
        when (environment) {
            ENVIRONMENT_OFFICIAL -> {
                BASE_URL = "http://baobab.kaiyanapp.com/"
            }
            ENVIRONMENT_DEVELOP -> {
                BASE_URL = "http://baobab.kaiyanapp.com/"
            }
        }
    }

}
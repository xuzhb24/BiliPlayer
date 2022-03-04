package com.android.project.func

import com.android.base.BaseApplication
import com.android.util.SPUtil

/**
 * Created by xuzhb on 2020/10/30
 * Desc:存储用户数据
 */
class UserInfo private constructor() {

    companion object {
        //单例对象
        val instance = SingleTonHolder.holder

        private const val SP_NAME = "user_info"
        private const val KEY_IS_FIRST_USE = "KEY_IS_FIRST_USE"
    }

    //静态内部类单例模式
    private object SingleTonHolder {
        val holder = UserInfo()
    }

    //是否是第一次使用
    var isFirstUse: Boolean by SPUtil(BaseApplication.instance, SP_NAME, KEY_IS_FIRST_USE, true)

}
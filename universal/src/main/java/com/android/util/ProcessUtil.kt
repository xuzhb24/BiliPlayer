package com.android.util

import android.app.ActivityManager
import android.content.Context

/**
 * Created by xuzhb on 2021/5/26
 * Desc:进程工具
 */
object ProcessUtil {

    //获取进程名称
    fun getProcessName(context: Context?): String? {
        context?.let {
            val manager = it.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            manager?.let {
                for (processInfo in it.runningAppProcesses) {
                    if (processInfo.pid == android.os.Process.myPid()) {
                        return processInfo.processName
                    }
                }
            }
        }
        return null
    }

}
package com.android.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by xuzhb on 2020/7/30
 * Desc:崩溃异常监听
 */
class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    companion object {
        private const val TAG = "CrashHandler"

        val instance = SingleTonHolder.holder
    }

    private object SingleTonHolder {
        val holder = CrashHandler()
    }

    private var mContext: Context? = null
    private var mDefaultCrashHandler: Thread.UncaughtExceptionHandler? = null

    fun init(context: Context) {
        mContext = context.applicationContext
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this) //设置为线程默认的异常处理器
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            saveExceptionToCache(e)
        } catch (ex: Exception) {
            Log.e(TAG, "dump exception fail!")
            ex.printStackTrace()
        }
        e.printStackTrace()
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler!!.uncaughtException(t, e)
        } else {
            Process.killProcess(Process.myPid())
        }
    }

    @Throws(Exception::class)
    private fun saveExceptionToCache(e: Throwable) {
        val fileName = Environment.getExternalStorageDirectory().absolutePath + File.separator + mContext!!.packageName + File.separator + "/log/crash.trace"
        LogUtil.i(TAG, fileName)
        val file = File(fileName)
        val dir = file.parentFile
        if (!dir.exists()) {
            dir.mkdirs()
        }
        if (file.exists() && file.length() > 1024 * 1024) {  //大于1Mb时清空
            file.delete()
        }
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val pw = PrintWriter(BufferedWriter(FileWriter(file, true))) //追加在文件末尾
        pw.println("=======================================================================")
        savePhoneInfo(pw)
        pw.println()
        pw.println(time)
        e.printStackTrace(pw)
        pw.println()
        pw.println()
        pw.close()
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun savePhoneInfo(pw: PrintWriter) {
        val pm = mContext!!.applicationContext.packageManager
        val pi = pm.getPackageInfo(mContext!!.packageName, PackageManager.GET_ACTIVITIES)
        pw.print("应用版本：")
        pw.print(pi.versionName)
        pw.print("_")
        pw.println(pi.versionCode)
        pw.print("系统版本：")
        pw.print(Build.VERSION.RELEASE)
        pw.print("_")
        pw.println(Build.VERSION.SDK_INT)
        pw.print("手机制造商：")
        pw.println(Build.MANUFACTURER)
        pw.print("手机型号：")
        pw.println(Build.MODEL)
        pw.print("CPU型号：")
        pw.println(Build.CPU_ABI)
    }

}
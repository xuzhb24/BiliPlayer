package com.android.util

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import com.android.base.BaseApplication

/**
 * Created by xuzhb on 2020/3/29
 * Desc:屏幕相关工具类
 */
object ScreenUtil {

    //获取屏幕宽度，单位像素
    fun getScreenWidth() = getScreenWidth(BaseApplication.instance)

    //获取屏幕宽度，单位像素
    fun getScreenWidth(context: Context): Int = getDisplayMetrics(context).widthPixels

    //获取屏幕高度，单位像素
    fun getScreenHeight() = getScreenHeight(BaseApplication.instance)

    //获取屏幕高度，单位像素
    fun getScreenHeight(context: Context): Int = getDisplayMetrics(context).heightPixels

    //获取屏幕参数
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        val manager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        manager.defaultDisplay.getMetrics(dm)
        return dm
    }

    //判断是否是横屏
    fun isLandscape(context: Context) =
        context.applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    //判断是否是竖屏
    fun isPortrait(context: Context) =
        context.applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    //在代码中设置屏幕为横屏
    fun setLandscape(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    //在代码中设置屏幕为竖屏
    fun setPortrait(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    //设置屏幕跟随系统sensor的状态
    fun setSensor(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    //判断是否锁屏
    fun isScreenLock(context: Context): Boolean {
        val km = context.applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return km.inKeyguardRestrictedInputMode()
    }

    //获取屏幕截图，包含状态栏
    fun captureWithStatusBar(activity: Activity): Bitmap {
        val view = activity.window.decorView
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        val bitmap = view.drawingCache
        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)
        val result = Bitmap.createBitmap(bitmap, 0, 0, dm.widthPixels, dm.heightPixels)
        view.destroyDrawingCache()
        return result
    }

    //获取屏幕截图，不包含状态栏
    fun captureWithoutStatusBar(activity: Activity): Bitmap {
        val view = activity.window.decorView
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        val bitmap = view.drawingCache
        val statusBarHeight = StatusBarUtil.getStatusBarHeight(activity)
        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)
        val result = Bitmap.createBitmap(bitmap, 0, statusBarHeight, dm.widthPixels, dm.heightPixels - statusBarHeight)
        view.destroyDrawingCache()
        return result
    }

    //通过shell命令获取屏幕截图，需要系统权限或root权限
    fun captureByCommand(fileName: String): Boolean {
        val command = "screencap -p $fileName"
        val result = ShellUtil.execCmd(command, true)
        return result.isSuccess()
    }

    //获取屏幕旋转角度
    fun getScreenRotation(activity: Activity): Int =
        when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_0 -> 0
            else -> 0
        }


}
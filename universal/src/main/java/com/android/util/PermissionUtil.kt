package com.android.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Created by xuzhb on 2020/2/6
 * Desc:权限管理工具
 */
object PermissionUtil {

    private var mRequestCode = -1

    //权限申请，返回true：申请成功，返回false：申请失败
    fun requestPermissions(activity: Activity, requestCode: Int, vararg permissions: String): Boolean {
        mRequestCode = requestCode
        if (!isPermissionGranted(activity, *permissions)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //Android 6.0以后权限动态申请
                activity.requestPermissions(permissions, requestCode)
            }
            return false
        }
        return true
    }

    //处理权限申请结果，对应Activity中onRequestPermissionsResult()方法
    fun onRequestPermissionsResult(
        activity: Activity, requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray,
        @NonNull listener: OnPermissionListener
    ) {
        if (mRequestCode != -1 && requestCode == mRequestCode) {
            val deniedForeverPermissions =
                getDeniedForeverPermissions(activity, *permissions)
            val deniedPermissions =
                getDeniedPermissions(activity, *permissions)
            when {
                deniedForeverPermissions.isNotEmpty() -> {
                    listener.onPermissionDeniedForever(deniedForeverPermissions)
                }
                deniedPermissions.isNotEmpty() -> {
                    listener.onPermissionDenied(deniedPermissions)
                }
                else -> {
                    listener.onPermissionGranted()
                }
            }
        }
    }

    //是否有指定的权限
    fun isPermissionGranted(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    //是否拒绝了权限
    fun isPermissionDenied(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                return true
            }
        }
        return false
    }

    //是否拒绝了权限且选择了不再提示
    fun isPermissionDeniedForever(activity: Activity, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    return true
                }
            }
        }
        return false
    }

    //获取请求权限中被拒绝的权限
    fun getDeniedPermissions(context: Context, vararg permissions: String): Array<String> {
        val deniedPermissions: MutableList<String> = mutableListOf()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission)
            }
        }
        return deniedPermissions.toTypedArray()
    }

    //获取请求权限中被拒绝且选择不再提示的权限
    fun getDeniedForeverPermissions(activity: Activity, vararg permissions: String): Array<String> {
        val deniedPermissions: MutableList<String> = mutableListOf()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    deniedPermissions.add(permission)
                }
            }
        }
        return deniedPermissions.toTypedArray()
    }

    //打开应用设置页面
    fun openSettings(activity: Activity, packageName: String, requestCode: Int) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.setData(Uri.fromParts("package", packageName, null))
        activity.startActivityForResult(intent, requestCode)
    }

    //权限申请监听
    interface OnPermissionListener {

        //权限申请成功
        fun onPermissionGranted()

        //权限申请被拒绝
        fun onPermissionDenied(deniedPermissions: Array<String>)

        //权限申请被拒绝，且选择了不再提示
        fun onPermissionDeniedForever(deniedForeverPermissions: Array<String>)

    }
}
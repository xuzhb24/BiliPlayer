package com.android.project.ui.entry

import android.Manifest
import android.content.Intent
import android.os.Bundle
import com.android.base.CommonBaseActivity
import com.android.project.databinding.ActivityWelcomeBinding
import com.android.project.func.UserInfo
import com.android.project.ui.main.MainActivity
import com.android.project.util.DialogUtil
import com.android.util.PermissionUtil
import com.android.util.postDelayed
import com.android.video.util.VideoConfig
import com.shuyu.gsyvideoplayer.GSYVideoManager

/**
 * Created by xuzhb on 2021/8/13
 * Desc:启动页
 */
class WelcomeActivity : CommonBaseActivity<ActivityWelcomeBinding>() {

    companion object {
        private val REQUEST_PERMISSION = arrayOf(
            //电话
//            Manifest.permission.READ_PHONE_STATE,
            //相机
//            Manifest.permission.CAMERA,
            //存储
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val REQUEST_PERMISSION_CODE = 11
        private const val REQUEST_SETTINGS_CODE = 22
    }

    override fun handleView(savedInstanceState: Bundle?) {
        VideoConfig.hasNoticeWithoutWifi = false
        VideoConfig.muteInTheList = true
        //申请权限
        if (PermissionUtil.isPermissionGranted(this, *REQUEST_PERMISSION)) { //拥有全部权限
            doNext()
        } else {
            //申请权限
            DialogUtil.showConfirmDialog(supportFragmentManager, "权限申请",
                "为了提供更好的服务，请授予应用相应的权限",
                {
                    it?.dismiss()
                    PermissionUtil.requestPermissions(this@WelcomeActivity, REQUEST_PERMISSION_CODE, *REQUEST_PERMISSION)
                }, {
                    it?.dismiss()
                    requestDenied()
                }, cancelable = false
            )
        }
    }

    override fun initListener() {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //处理权限申请结果
        PermissionUtil.onRequestPermissionsResult(
            this, requestCode,
            permissions, grantResults,
            object : PermissionUtil.OnPermissionListener {
                override fun onPermissionGranted() {
                    doNext()
                }

                override fun onPermissionDenied(deniedPermissions: Array<String>) {
                    DialogUtil.showConfirmDialog(supportFragmentManager, "权限申请",
                        "请授予应用相应的权限，否则app可能无法正常工作",
                        {
                            it?.dismiss()
                            PermissionUtil.requestPermissions(this@WelcomeActivity, REQUEST_PERMISSION_CODE, *REQUEST_PERMISSION)
                        }, {
                            it?.dismiss()
                            requestDenied()
                        }, cancelable = false
                    )
                }

                override fun onPermissionDeniedForever(deniedForeverPermissions: Array<String>) {
                    DialogUtil.showConfirmDialog(supportFragmentManager, "权限申请",
                        "请到应用设置页面-权限中开启相应权限，保证app的正常使用",
                        {
                            it?.dismiss()
                            PermissionUtil.openSettings(this@WelcomeActivity, packageName, REQUEST_SETTINGS_CODE)
                        }, {
                            it?.dismiss()
                            requestDenied()
                        }, "去设置", cancelable = false
                    )
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //从设置页面返回
        if (requestCode == REQUEST_SETTINGS_CODE) {
            //是否已申请所有权限
            if (PermissionUtil.isPermissionGranted(this, *REQUEST_PERMISSION)) {
                doNext()
            } else {
                requestDenied()
            }
        }
    }

    //权限申请成功
    private fun doNext() {
        postDelayed {
            if (UserInfo.instance.isFirstUse) {
                startActivity(GuideActivity::class.java)
                finish()
            } else {
                startActivity(MainActivity::class.java)
                finish()
            }
        }
    }

    //权限申请失败
    private fun requestDenied() {
        showToast("权限开启失败！")
        postDelayed { finish() }
    }

}
package com.android.project.util

import android.app.Dialog
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.android.base.BaseApplication
import com.android.project.R
import com.android.util.CheckFastClickUtil
import com.android.util.SizeUtil
import com.android.widget.dialog.ConfirmDialog
import com.android.widget.picGetterDialog.OnPicGetterListener
import com.android.widget.picGetterDialog.PicGetterDialog
import com.yalantis.ucrop.UCrop

/**
 * Created by xuzhb on 2020/8/10
 * Desc:对话框管理工具
 */
object DialogUtil {

    //确定/取消对话框
    fun showConfirmDialog(
        fragmentManager: FragmentManager,
        title: String,                               //对话框标题
        content: String,                             //对话框内容
        confirmListener: (dialog: Dialog?) -> Unit,  //点击确定按钮时回调
        cancelistener: (dialog: Dialog?) -> Unit,    //点击取消按钮时回调
        confirmText: String = "确定",                //确定按钮文本
        cancelText: String = "取消",                 //取消按钮文本
        cancelVisible: Boolean = true,               //是否显示取消按钮
        cancelable: Boolean = true                   //点击外部区域是否可取消
    ) {
        if (CheckFastClickUtil.isFastClick()) {  //防止快速点击弹出两个对话框
            return
        }
        ConfirmDialog.newInstance(title, content, confirmText, cancelText, cancelVisible)
            .setOnConfirmListener(confirmListener)
            .setOnCancelListener(cancelistener)
            .setOutsideCancelable(cancelable)
            .setDimAmount(0.3f)
            .setViewParams(SizeUtil.dp2pxInt(245f), ViewGroup.LayoutParams.WRAP_CONTENT)  //设置对话框宽高
            .show(fragmentManager)
    }

    //拍照或从相册选取选择弹窗
    fun showPicChooseDialog(
        fragmentManager: FragmentManager,
        listener: OnPicGetterListener
    ) {
        if (CheckFastClickUtil.isFastClick()) {  //防止快速点击弹出两个对话框
            return
        }
        val options = UCrop.Options().apply {
            setToolbarTitle("裁剪图片")
            val color = BaseApplication.instance.resources.getColor(R.color.colorOnPrimary)
            setToolbarColor(color)       //标题栏颜色
            setStatusBarColor(color)     //状态栏颜色
            setActiveWidgetColor(color)  //底部操作栏字体颜色
        }
        val dialog = PicGetterDialog()
        dialog.setAnimationStyle(R.style.AnimTranslateBottom)
            .setDimAmount(0.3f)
            .setCropOptions(options)
            .setMaxCropSize(1080, 1920)
            .setOnPicGetterListener(listener)
        dialog.show(fragmentManager)
    }

}
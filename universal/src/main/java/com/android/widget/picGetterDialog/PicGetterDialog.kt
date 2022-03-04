package com.android.widget.picGetterDialog

import android.app.Dialog
import com.android.universal.R
import com.android.widget.ViewHolder

/**
 * Created by xuzhb on 2020/1/27
 * Desc:拍照或从相册选取选择框
 */
class PicGetterDialog : BasePicGetterDialog() {

    override fun getLayoutId(): Int = R.layout.dialog_pic_getter

    override fun convertView(holder: ViewHolder, dialog: Dialog?) {
        holder.setOnClickListener(R.id.camera_tv) {
            openCamera()  //拍照
        }
        holder.setOnClickListener(R.id.gallery_tv) {
            openGallery()  //从相册选取照片
        }
        holder.setOnClickListener(R.id.cancel_tv) {
            getDialog()?.let {
                onCancel(it)  //取消弹窗
                dismiss()
            }
        }
    }
}
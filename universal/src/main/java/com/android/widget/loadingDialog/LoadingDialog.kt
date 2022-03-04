package com.android.widget.loadingDialog

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.android.universal.R

/**
 * Created by xuzhb on 2020/7/17
 * Desc:加载框
 */
class LoadingDialog @JvmOverloads constructor(
    context: Context, themeStyle: Int = R.style.LoadingDialogStyle
) : Dialog(context, themeStyle) {

    init {
        setContentView(R.layout.dialog_loading)
    }

    //显示加载框，默认可取消
    fun show(message: CharSequence, cancelable: Boolean = true) {
        with(findViewById<TextView>(R.id.loading_tv)) {
            if (TextUtils.isEmpty(message)) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = message
            }
        }
        //点击对话框外的部分不消失
        setCanceledOnTouchOutside(cancelable)
        //点击或按返回键时是否消失
        setCancelable(cancelable)
        show()
    }

}
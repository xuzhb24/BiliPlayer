package com.android.widget.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import com.android.universal.R
import com.android.widget.ViewHolder

/**
 * Created by xuzhb on 2019/10/22
 * Desc:常见单/双按钮的Dialog
 */
class ConfirmDialog : BaseDialog() {

    companion object {
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_CONTENT = "EXTRA_CONTENT"
        private const val EXTRA_CONFIRM_TEXT = "EXTRA_CONFIRM_TEXT"
        private const val EXTRA_CANCEL_TEXT = "EXTRA_CANCEL_TEXT"
        private const val EXTRA_CANCEL_VISIBLE = "EXTRA_CANCEL_VISIBLE"

        fun newInstance(
            title: String = "",
            content: String = "",
            confirmText: String = "确定",
            cancelText: String = "取消",
            cancelVisible: Boolean = true
        ): ConfirmDialog {
            val dialog = ConfirmDialog()
            if (TextUtils.isEmpty(content) && TextUtils.isEmpty(title)) {
                return dialog  //如果内容和文本都为空，则不传bundle
            }
            val bundle = Bundle()
            with(bundle) {
                putString(EXTRA_TITLE, title)
                putString(EXTRA_CONTENT, content)
                putString(EXTRA_CONFIRM_TEXT, confirmText)
                putString(EXTRA_CANCEL_TEXT, cancelText)
                putBoolean(EXTRA_CANCEL_VISIBLE, cancelVisible)
            }
            dialog.arguments = bundle
            return dialog
        }

    }

    private var mTitle: String = ""
    private var mContent: String = ""
    private var mConfirmText: String = "确定"
    private var mCancelText: String = "取消"
    private var mCancelVisible: Boolean = true  //是否显示两个按钮，默认显示两个按钮
    private var mOnConfirmListener: ((dialog: Dialog?) -> Unit)? = null
    private var mOnCancelListener: ((dialog: Dialog?) -> Unit)? = null

    //设置标题
    fun setTitle(title: String): ConfirmDialog {
        this.mTitle = title
        return this
    }

    //设置内容
    fun setContent(content: String): ConfirmDialog {
        this.mContent = content
        return this
    }

    //设置确定按钮的文本
    fun setConfirmText(confirmText: String): ConfirmDialog {
        this.mConfirmText = confirmText
        return this
    }

    //设置取消按钮的文本
    fun setCancelText(cancelText: String): ConfirmDialog {
        this.mCancelText = cancelText
        return this
    }

    //是否显示取消按钮
    fun setCancelVisible(cancelVisible: Boolean): ConfirmDialog {
        this.mCancelVisible = cancelVisible
        return this
    }

    //点击确定按钮后回调
    fun setOnConfirmListener(listener: (dialog: Dialog?) -> Unit): ConfirmDialog {
        this.mOnConfirmListener = listener
        return this
    }

    //点击取消按钮后回调
    fun setOnCancelListener(listener: (dialog: Dialog?) -> Unit): ConfirmDialog {
        this.mOnCancelListener = listener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        bundle?.let {
            mTitle = it.getString(EXTRA_TITLE) ?: ""
            mContent = it.getString(EXTRA_CONTENT) ?: ""
            mConfirmText = it.getString(EXTRA_CONFIRM_TEXT) ?: ""
            mCancelText = it.getString(EXTRA_CANCEL_TEXT) ?: ""
            mCancelVisible = it.getBoolean(EXTRA_CANCEL_VISIBLE, true)
        }
    }

    override fun getLayoutId(): Int = R.layout.dialog_confirm

    override fun convertView(holder: ViewHolder, dialog: Dialog?) {
        holder.setText(R.id.title_tv, mTitle)
            .setText(R.id.content_tv, mContent)
            .setText(R.id.confirm_tv, mConfirmText)
            .setOnClickListener(R.id.confirm_tv) { mOnConfirmListener?.invoke(dialog) }
        if (mCancelVisible) {
            holder.setText(R.id.cancel_tv, mCancelText)
                .setOnClickListener(R.id.cancel_tv) { mOnCancelListener?.invoke(dialog) }
        } else {
            holder.setViewGone(R.id.cancel_tv).setViewGone(R.id.vertical_view)
        }
    }
}
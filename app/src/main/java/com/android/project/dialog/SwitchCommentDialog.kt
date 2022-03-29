package com.android.project.dialog

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.project.R
import com.android.project.ui.detail.VideoReplyFragment
import com.android.util.formatCountStr
import com.android.util.invisible
import com.android.util.visible
import com.android.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.dialog_video_reply.view.*

/**
 * Created by xuzhb on 2022/3/24
 * Desc:
 */
class SwitchCommentDialog : DialogFragment() {

    companion object {
        private const val EXTRA_ID = "EXTRA_ID"
        private const val EXTRA_REPLY_COUNT = "EXTRA_REPLY_COUNT"
        fun newInstance(id: Int, replyCount: Int) = SwitchCommentDialog().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_ID, id)
                putInt(EXTRA_REPLY_COUNT, replyCount)
            }
        }
    }

    private var mId: Int = 0
    private var mReplyCount: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mId = arguments?.getInt(EXTRA_ID) ?: 0
        mReplyCount = arguments?.getInt(EXTRA_REPLY_COUNT) ?: 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_video_reply, container, false)
        if (mReplyCount > 0) {
            view.count_tv.visible()
            view.count_tv.text = "${formatCountStr(mReplyCount, "0")}条评论"
        } else {
            view.count_tv.invisible()
        }
        childFragmentManager.beginTransaction().add(R.id.content_fl, VideoReplyFragment.newInstance(mId)).commitAllowingStateLoss()
        return view
    }

    override fun onStart() {
        super.onStart()
        initParams()
    }

    //初始化参数
    private fun initParams() {
        dialog?.window?.let {
            val params = it.attributes
            params.dimAmount = 0.5f
            params.gravity = Gravity.BOTTOM
            //设置dialog宽度
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            //设置dialog高度
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //设置dialog动画
            it.setWindowAnimations(R.style.AnimTranslateBottom)
            it.attributes = params
        }
        isCancelable = true
    }

    //在中间显示
    fun show(manager: FragmentManager) {
        super.show(manager, BaseDialog::class.java.name)
    }

}
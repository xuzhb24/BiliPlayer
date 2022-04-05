package com.android.video.util

import android.app.AlertDialog
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.util.LogUtil
import com.android.util.NetworkUtil
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.R
import com.shuyu.gsyvideoplayer.utils.NetworkUtils
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer

/**
 * Created by xuzhb on 2022/3/19
 * Desc:ViewPager列表自动播放监听类
 */
class AutoPlayPageChangeListener(private val viewPager: ViewPager2, private var defaultPosition: Int, private val itemPlayId: Int) :
    ViewPager2.OnPageChangeCallback() {

    companion object {
        const val TAG = "AutoPlayPageChangeListener"
    }

    //防止在当前页面抖动，触发onPageScrollStateChanged回调
    private var isPageSelected = false

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        LogUtil.i(TAG, "onPageSelected position:${position} positionOffsetPixels:${positionOffsetPixels}")
        if (defaultPosition == position && positionOffsetPixels == 0) {
            //进入页面后，调用setCurrentItem函数，手动触发onPageScrollStateChanged函数。
            onPageScrollStateChanged(ViewPager2.SCROLL_STATE_IDLE)
            defaultPosition = -1
        }
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        LogUtil.i(TAG, "onPageSelected position:${position}")
        isPageSelected = true
    }

    override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)
        LogUtil.i(TAG, "onPageScrollStateChanged state:${state}")
        if (state == ViewPager2.SCROLL_STATE_IDLE && isPageSelected) {
            playVideo()
            isPageSelected = false
        }
    }

    private fun playVideo() {
        var gsyBaseVideoPlayer: GSYBaseVideoPlayer? = null
        var needPlay = false
        for (childIndex in 0 until viewPager.childCount) {
            //viewPager2内部使用的RecyclerView作为childView。通过内部源码可以查看。
            val view = viewPager.getChildAt(childIndex)
            if (view != null && view is RecyclerView) {
                val layoutManager = view.layoutManager
                val childCount = layoutManager?.childCount ?: 0  //返回offscreenPageLimit(两侧保留的屏幕页数) * 2 + 1（当前页数）。offscreenPageLimit 设置1，则返回值为3。
                for (index in 0 until childCount) {
                    if (layoutManager!!.getChildAt(index) != null && layoutManager.getChildAt(index)?.findViewById<View?>(itemPlayId) != null) {
                        val player = layoutManager.getChildAt(index)?.findViewById<View>(itemPlayId) as GSYBaseVideoPlayer
                        val rect = Rect()
                        player.getLocalVisibleRect(rect)
                        val height = player.height
                        val isPlayerVisible = rect.top == 0 && rect.bottom == height  //true：当前videoPlayer在屏幕上可见
                        //player.visibility==View.VISIBLE，防止RecyclerView因缓存机制复用ViewHolder，导致第1个判断条件失效。比如，当翻到N页时，正在看图片，同时也在播放原来的视频。
                        if (isPlayerVisible && player.visibility == View.VISIBLE && player.currentPlayer.currentState == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL || player.currentPlayer.currentState == GSYBaseVideoPlayer.CURRENT_STATE_ERROR) {
                            gsyBaseVideoPlayer = player
                            needPlay = true
                        } else {
                            GSYVideoManager.releaseAllVideos()
                        }
                    }
                }
            }
        }
        gsyBaseVideoPlayer?.let {
            if (needPlay) {
//                startPlayLogic(it, it.context)
                it.startPlayLogic()
            }
        }
    }

    private var isNeedShowWifiDialog = true

    private fun startPlayLogic(gsyBaseVideoPlayer: GSYBaseVideoPlayer, context: Context) {
        if (!NetworkUtil.isWifiConnected(context) && isNeedShowWifiDialog) {
            showWifiDialog(gsyBaseVideoPlayer, context)
            return
        }
        gsyBaseVideoPlayer.startPlayLogic()
    }

    private fun showWifiDialog(gsyBaseVideoPlayer: GSYBaseVideoPlayer, context: Context) {
        if (!NetworkUtils.isAvailable(context)) {
            Toast.makeText(context, context.resources.getString(R.string.no_net), Toast.LENGTH_LONG).show()
            return
        }
        AlertDialog.Builder(context).apply {
            setMessage(context.resources.getString(R.string.tips_not_wifi))
            setPositiveButton(context.resources.getString(R.string.tips_not_wifi_confirm)) { dialog, which ->
                dialog.dismiss()
                gsyBaseVideoPlayer.startPlayLogic()
            }
            setNegativeButton(context.resources.getString(R.string.tips_not_wifi_cancel)) { dialog, which ->
                dialog.dismiss()
            }
            create()
        }.show()
    }

}
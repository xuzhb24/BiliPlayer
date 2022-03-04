package com.android.video.util

import android.graphics.Rect
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.android.util.LogUtil
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer

/**
 * Created by xuzhb on 2022/1/27
 * Desc:计算滑动，自动播放的帮助类
 */
class ScrollCalculatorHelper(
    private val playId: Int,
    private val rangeTop: Int,
    private val rangeBottom: Int
) {

    companion object {
        private const val TAG = "ScrollCalculatorHelper"
    }

    //可见个数
    var visibleCount = 0

    private val playHandler = Handler()
    private var runnable: PlayRunnable? = null

    fun onScrollStateChanged(view: RecyclerView?, scrollState: Int) {
        when (scrollState) {
            RecyclerView.SCROLL_STATE_IDLE -> playVideo(view)
        }
    }

    private fun playVideo(view: RecyclerView?) {
        if (view == null) {
            return
        }
        val layoutManager = view.layoutManager
        var gsyBaseVideoPlayer: GSYBaseVideoPlayer? = null
        var needPlay = false
        for (i in 0 until visibleCount) {
            if (layoutManager?.getChildAt(i) != null && layoutManager.getChildAt(i)?.findViewById<View>(playId) != null) {
                val player = layoutManager.getChildAt(i)!!.findViewById<View>(playId) as GSYBaseVideoPlayer
                val rect = Rect()
                player.getLocalVisibleRect(rect)
                val height = player.height
                LogUtil.i(TAG, "top:${rect.top} bottom:${rect.bottom} height:${height}")
                //说明第一个完全可视
                if (rect.top >= 0 && rect.bottom - rect.top == height) {
                    gsyBaseVideoPlayer = player
                    LogUtil.i(TAG, "currentState:${player.currentPlayer.currentState}")
                    if (player.currentPlayer.currentState == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL
                        || player.currentPlayer.currentState == GSYBaseVideoPlayer.CURRENT_STATE_ERROR
                        || player.currentPlayer.currentState == GSYBaseVideoPlayer.CURRENT_STATE_PAUSE  //恢复播放
                    ) {
                        needPlay = true
                    }
                    break
                }
            }
        }
        LogUtil.i(TAG, "hasPlayer:${gsyBaseVideoPlayer != null} needPlay:$needPlay")
        if (gsyBaseVideoPlayer != null && needPlay) {
            if (runnable != null) {
                playHandler.removeCallbacks(runnable)
                runnable = null
            }
            runnable = PlayRunnable(gsyBaseVideoPlayer)
            //降低频率
            playHandler.postDelayed(runnable, 400)
        } else {
            if (gsyBaseVideoPlayer == null) {  //当前可视区域内没有播放器控件
                GSYVideoManager.onPause()  //暂停所有视频播放，考虑是滑出可视区域后的播放器控件
            }
        }
    }

    private inner class PlayRunnable(var gsyBaseVideoPlayer: GSYBaseVideoPlayer?) : Runnable {
        override fun run() {
            //如果未播放，需要播放
            gsyBaseVideoPlayer?.let {
                val screenPosition = IntArray(2)
                it.getLocationOnScreen(screenPosition)
                val halfHeight = it.height / 2
                val rangePosition = screenPosition[1] + halfHeight
                LogUtil.i(TAG, "rangePosition:$rangePosition rangeTop:$rangeTop rangeBottom:$rangeBottom")
                //中心点在播放区域内
                if (rangePosition in rangeTop..rangeBottom) {
                    it.startPlayLogic()
                }
            }
        }
    }

}
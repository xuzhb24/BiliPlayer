package com.android.video.util

import android.app.AlertDialog
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.util.LogUtil
import com.android.util.NetworkUtil
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.R
import com.shuyu.gsyvideoplayer.utils.NetworkUtils
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer

/**
 * Created by xuzhb on 2022/3/31
 * Desc:RecyclerView列表自动播放监听类
 */
open class AutoPlayScrollListener(
    private val playId: Int,
    private val rangeTop: Int,
    private val rangeBottom: Int
) : RecyclerView.OnScrollListener() {

    companion object {
        private const val TAG = "AutoPlayScrollListener"
    }

    private var firstVisible = 0
    private var lastVisible = 0
    private var visibleCount = 0
    private var runnable: PlayRunnable? = null
    private val playHandler = Handler()

    override fun onScrollStateChanged(recyclerView: RecyclerView, scrollState: Int) {
        when (scrollState) {
            RecyclerView.SCROLL_STATE_IDLE -> playVideo(recyclerView)
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        initVisibleCount(recyclerView)
    }

    fun onResume(recyclerView: RecyclerView) {
        initVisibleCount(recyclerView)
        playVideo(recyclerView)
    }

    private fun initVisibleCount(recyclerView: RecyclerView) {
        val firstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val lastVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        firstVisible = firstVisibleItem
        lastVisible = lastVisibleItem
        visibleCount = lastVisibleItem - firstVisibleItem
    }

    private fun playVideo(recyclerView: RecyclerView?) {
        if (recyclerView == null) {
            return
        }
        val layoutManager = recyclerView.layoutManager
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
//                    startPlayLogic(it, it.context)
                }
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
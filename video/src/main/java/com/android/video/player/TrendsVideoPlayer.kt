package com.android.video.player

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import com.android.util.*
import com.android.util.glide.GlideUtil
import com.android.video.R
import com.android.video.databinding.LayoutTrendsVideoPlayerBinding
import com.android.video.util.VideoConfig
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.utils.NetworkUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.android.synthetic.main.dialog_video_brightness.view.*
import kotlinx.android.synthetic.main.dialog_video_progress.view.*
import kotlinx.android.synthetic.main.dialog_video_volume.view.*

/**
 * Created by xuzhb on 2022/3/30
 * Desc:动态播放器
 */
class TrendsVideoPlayer : StandardGSYVideoPlayer {

    companion object {
        private const val TAG = "TrendsVideoPlayer"
    }

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    //音量进度条的左侧图标
    private var mDialogVolumeIcon: ImageView? = null

    //亮度进度条的progress
    private var mDialogBrightnessProgressBar: ProgressBar? = null

    private lateinit var binding: LayoutTrendsVideoPlayerBinding
    private var mCoverOriginUrl = ""  //封面网络图
    private var mCoverOriginId = 0    //封面资源id
    private var mCoverDefaultRes = 0  //封面缺省图

    //加载封面
    fun loadCoverImage(url: String, res: Int) {
        mCoverOriginUrl = url
        mCoverDefaultRes = res
        val radius = SizeUtil.dp2pxInt(5f)
        GlideUtil.showImageFromUrl(
            binding.thumbImage, url,
            RoundedCorners(radius), res, res
        )
    }

    //加载封面
    fun loadCoverImage(resId: Int, res: Int) {
        mCoverOriginId = resId
        mCoverDefaultRes = res
        val radius = SizeUtil.dp2pxInt(5f)
        GlideUtil.showImageFromResource(
            binding.thumbImage, resId,
            RoundedCorners(radius)
        )
    }

    /**
     * 设置数据
     * @param shareCount 分享个数
     * @param replyCount 回复个数
     * @param duration 时长
     */
    fun setData(shareCount: String, replyCount: String, duration: String) {
        binding.shareCountTv.text = "${shareCount}分享"
        binding.commentCountTv.text = "${replyCount}评论"
        binding.durationTv.text = duration
    }

    override fun initInflate(context: Context?) {
        binding = LayoutTrendsVideoPlayerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun init(context: Context?) {
        super.init(context)
        if (mThumbImageViewLayout != null && (mCurrentState == -1
                    || mCurrentState == CURRENT_STATE_NORMAL
                    || mCurrentState == CURRENT_STATE_ERROR)
        ) {
            mThumbImageViewLayout.visible()
        }
        //声音设置
        binding.volumeIv.setOnClickListener {
            VideoConfig.muteInTheList = !VideoConfig.muteInTheList
            GSYVideoManager.instance().isNeedMute = VideoConfig.muteInTheList
            setVolumeState()
        }
        //重播
        binding.replayTv.setOnClickListener {
            clickStartIcon()
        }
    }

    //将自定义的效果也设置到全屏
    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        val player = super.startWindowFullscreen(context, actionBar, statusBar) as TrendsVideoPlayer
        if (mCoverOriginUrl.isNotBlank()) {
            player.loadCoverImage(mCoverOriginUrl, mCoverDefaultRes)
        } else if (mCoverOriginId != 0) {
            player.loadCoverImage(mCoverOriginId, mCoverDefaultRes)
        }
        return player
    }

    //显示小窗口
    override fun showSmallVideo(size: Point?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        return (super.showSmallVideo(size, actionBar, statusBar) as TrendsVideoPlayer).apply {
            mStartButton.visibility = View.GONE
            mStartButton = null
        }
    }

    //克隆切换参数
    override fun cloneParams(from: GSYBaseVideoPlayer?, to: GSYBaseVideoPlayer?) {
        super.cloneParams(from, to)
        (to as TrendsVideoPlayer).mShowFullAnimation = (from as TrendsVideoPlayer).mShowFullAnimation
    }

    //退出window层播放全屏效果
    override fun clearFullscreenLayout() {
        if (!mFullAnimEnd) {
            return
        }
        mIfCurrentIsFullscreen = false
        var delay = 0
        if (mOrientationUtils != null) {
            delay = mOrientationUtils.backToProtVideo()
            mOrientationUtils.isEnable = false
            mOrientationUtils.releaseListener()
            mOrientationUtils = null
        }
        if (!mShowFullAnimation) {
            delay = 0
        }
        val vp: ViewGroup? = CommonUtil.scanForActivity(context).findViewById(Window.ID_ANDROID_CONTENT)
        val oldF: View? = vp?.findViewById(fullId)
        if (oldF != null) {
            (oldF as TrendsVideoPlayer).mIfCurrentIsFullscreen = false
        }
        if (delay == 0) {
            backToNormal()
        } else {
            postDelayed({ backToNormal() }, delay.toLong())
        }
    }

    override fun onSurfaceUpdated(surface: Surface?) {
        super.onSurfaceUpdated(surface)
        if (mThumbImageViewLayout != null && mThumbImageViewLayout.visibility == VISIBLE) {
            mThumbImageViewLayout.visibility = INVISIBLE
        }
    }

    override fun setViewShowState(view: View?, visibility: Int) {
        if (view == mThumbImageViewLayout && visibility != VISIBLE) {
            return
        }
        super.setViewShowState(view, visibility)
    }

    override fun onSurfaceAvailable(surface: Surface?) {
        super.onSurfaceAvailable(surface)
        if (GSYVideoType.getRenderType() != GSYVideoType.TEXTURE) {
            if (mThumbImageViewLayout != null && mThumbImageViewLayout.visibility == VISIBLE) {
                mThumbImageViewLayout.visibility = INVISIBLE
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (!mIfCurrentIsFullscreen && mCurrentState != CURRENT_STATE_AUTO_COMPLETE) {
            binding.volumeIv.visible()
        } else {
            binding.volumeIv.gone()
        }
        return super.onTouch(v, event)
    }

    //点击播放器
    override fun onClick(v: View?) {
        if (mIfCurrentIsFullscreen) {
            super.onClick(v)
        } else {
            mOnNormalClickListener?.invoke()
        }
    }

    //点击播放器
    override fun onClickUiToggle(e: MotionEvent?) {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE)
            return
        }
        LogUtil.i(TAG, "onClickUiToggle")
        super.onClickUiToggle(e)
        hideBottomProgressBarWhenFull()
        showControlLayout()
    }

    //显示
    override fun changeUiToNormal() {
        super.changeUiToNormal()
        LogUtil.i(TAG, "changeUiToNormal")
        mBottomContainer.visible()
        showControlLayout()
        if (!mIfCurrentIsFullscreen) {
            binding.pauseIv.visible()
            binding.volumeIv.invisible()
        } else {
            binding.pauseIv.invisible()
            binding.volumeIv.invisible()
        }
        binding.replayFl.gone()
    }

    //准备
    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        LogUtil.i(TAG, "changeUiToPreparingShow")
        binding.replayFl.gone()
    }

    //缓冲
    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        LogUtil.i(TAG, "changeUiToPlayingBufferingShow")
    }

    //开始播放
    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        LogUtil.i(TAG, "changeUiToPlayingShow")
        if (!mIfCurrentIsFullscreen) {
            binding.pauseIv.gone()
            binding.volumeIv.visible()
        } else {
            binding.pauseIv.invisible()
            binding.volumeIv.invisible()
        }
    }

    //暂停播放
    override fun changeUiToPauseShow() {
        super.changeUiToPauseShow()
        LogUtil.i(TAG, "changeUiToPauseShow")
    }

    //播放出错
    override fun changeUiToError() {
        super.changeUiToError()
        LogUtil.i(TAG, "changeUiToError")
    }

    //播放完成
    override fun changeUiToCompleteShow() {
        super.changeUiToCompleteShow()
        LogUtil.i(TAG, "changeUiToCompleteShow")
        binding.smallControlLl.gone()
        if (!mIfCurrentIsFullscreen) {
            binding.replayFl.visible()
        } else {
            binding.replayFl.gone()
        }
    }

    //隐藏控件
    override fun hideAllWidget() {
        super.hideAllWidget()
        LogUtil.i(TAG, "hideAllWidget")
        hideBottomProgressBarWhenFull()
        binding.volumeIv.gone()
    }

    override fun startAfterPrepared() {
        super.startAfterPrepared()
        LogUtil.i(TAG, "startAfterPrepared")
        GSYVideoManager.instance().isNeedMute = VideoConfig.muteInTheList
    }

    //全屏返回
    override fun resolveNormalVideoShow(oldF: View?, vp: ViewGroup?, gsyVideoPlayer: GSYVideoPlayer?) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
        if (mCurrentState == GSYVideoView.CURRENT_STATE_PAUSE) {
            clickStartIcon()  //全屏返回时如果是暂停状态则恢复播放
        } else if (mCurrentState == GSYVideoView.CURRENT_STATE_AUTO_COMPLETE) {
            binding.replayFl.visible()  //全屏返回时如果是播放完成状态则显示重播按钮
        }
    }

    override fun updateStartImage() {
        if (mStartButton is ImageView) {
            val iv = mStartButton as ImageView
            when (mCurrentState) {
                GSYVideoView.CURRENT_STATE_PLAYING -> iv.setImageResource(R.drawable.ic_video_play)
                GSYVideoView.CURRENT_STATE_ERROR -> iv.setImageResource(R.drawable.ic_video_pause)
                GSYVideoView.CURRENT_STATE_AUTO_COMPLETE -> iv.setImageResource(R.drawable.ic_video_refresh)
                else -> iv.setImageResource(R.drawable.ic_video_pause)
            }
        } else {
            super.updateStartImage()
        }
        setVolumeState()
    }

    override fun getShrinkImageRes() = R.drawable.ic_video_shrink

    override fun getEnlargeImageRes() = R.drawable.ic_video_enlarge

    //自定义音量进度条
    override fun showVolumeDialog(deltaY: Float, volumePercent: Int) {
        if (mVolumeDialog == null) {
            val localView = LayoutInflater.from(activityContext).inflate(R.layout.dialog_video_volume, null)
            mDialogVolumeIcon = localView.volume_icon
            mDialogVolumeProgressBar = localView.volume_progressbar
            mVolumeDialog = Dialog(activityContext, com.shuyu.gsyvideoplayer.R.style.video_style_dialog_progress).apply {
                setContentView(localView)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                window?.attributes = window?.attributes?.apply {
                    gravity = Gravity.TOP or Gravity.START
                    width = getWidth()
                    height = getHeight()
                    val location = IntArray(2)
                    getLocationOnScreen(location)
                    x = location[0]
                    y = location[1]
                }
            }
        }
        if (!mVolumeDialog.isShowing) {
            mVolumeDialog.show()
        }
        if (volumePercent <= 0) {
            mDialogVolumeIcon?.setImageResource(R.drawable.ic_dialog_volume_close)
        } else {
            mDialogVolumeIcon?.setImageResource(R.drawable.ic_dialog_volume_open)
        }
        mDialogVolumeProgressBar.volume_progressbar.progress = volumePercent
    }

    //自定义亮度进度条
    override fun showBrightnessDialog(percent: Float) {
        if (mBrightnessDialog == null) {
            val localView = LayoutInflater.from(activityContext).inflate(R.layout.dialog_video_brightness, null)
            mDialogBrightnessProgressBar = localView.brightness_progressbar
            mBrightnessDialog = Dialog(activityContext, com.shuyu.gsyvideoplayer.R.style.video_style_dialog_progress).apply {
                setContentView(localView)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                window?.attributes = window?.attributes?.apply {
                    gravity = Gravity.TOP or Gravity.START
                    width = getWidth()
                    height = getHeight()
                    val location = IntArray(2)
                    getLocationOnScreen(location)
                    x = location[0]
                    y = location[1]
                }
            }
        }
        if (!mBrightnessDialog.isShowing) {
            mBrightnessDialog.show()
        }
        mDialogBrightnessProgressBar?.progress = (percent * 100).toInt()
    }

    //自定义触摸显示滑动进度条
    override fun showProgressDialog(
        deltaX: Float,
        seekTime: String?,
        seekTimePosition: Int,
        totalTime: String?,
        totalTimeDuration: Int
    ) {
        if (mProgressDialog == null) {
            val localView = LayoutInflater.from(activityContext).inflate(R.layout.dialog_video_progress, null)
            mDialogSeekTime = localView.tv_current
            mDialogTotalTime = localView.tv_duration
            mProgressDialog = Dialog(activityContext, com.shuyu.gsyvideoplayer.R.style.video_style_dialog_progress).apply {
                setContentView(localView)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                window?.attributes = window?.attributes?.apply {
                    gravity = Gravity.TOP or Gravity.START
                    width = getWidth()
                    height = getHeight()
                    val location = IntArray(2)
                    getLocationOnScreen(location)
                    x = location[0]
                    y = location[1]
                }
            }
        }
        if (!mProgressDialog.isShowing) {
            mProgressDialog.show()
        }
        mDialogSeekTime.text = seekTime
        mDialogTotalTime.text = totalTime
    }

    //流量播放时不显示wifi确定框
    override fun isShowNetConfirm() = false

    override fun startPlayLogic() {
        if (!NetworkUtils.isAvailable(context)) {
            ToastUtil.showToast("当前找不到网络", true)
            return
        }
        if (!NetworkUtil.isWifiConnected(context) && !VideoConfig.hasNoticeWithoutWifi) {
            VideoConfig.hasNoticeWithoutWifi = true
            ToastUtil.showToast("移动网络播放中，请注意流量消耗", true)
        }
        super.startPlayLogic()
    }

    private fun setVolumeState() {
        if (VideoConfig.muteInTheList) {
            binding.volumeIv.setImageResource(R.drawable.ic_volume_close)
        } else {
            binding.volumeIv.setImageResource(R.drawable.ic_volume_open)
        }
    }

    //全屏时隐藏底部进度条
    private fun hideBottomProgressBarWhenFull() {
        if (mIfCurrentIsFullscreen) {
            mBottomProgressBar.gone()
        } else {
            mBottomProgressBar.visible()
        }
    }

    //显示全屏和非全屏时各自的控制栏
    private fun showControlLayout() {
        if (isIfCurrentIsFullscreen) {  //全屏
            binding.smallControlLl.gone()
            binding.fullBottomControlLl.visible()
        } else {
            binding.smallControlLl.visible()
            binding.fullBottomControlLl.gone()
        }
    }

    //非全屏时点击整个控件
    private var mOnNormalClickListener: (() -> Unit)? = null

    fun setOnNormalClickListener(listener: (() -> Unit)) {
        this.mOnNormalClickListener = listener
    }

}
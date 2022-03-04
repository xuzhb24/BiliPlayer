package com.android.video.player


import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import com.android.util.*
import com.android.util.glide.GlideUtil
import com.android.util.glide.SectionRoundedCorners
import com.android.video.R
import com.android.video.databinding.LayoutListVideoPlayerBinding
import com.android.video.util.VideoConfig
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
 * Created by xuzhb on 2021/12/29
 * Desc:列表播放器
 */
class ListVideoPlayer : StandardGSYVideoPlayer {

    companion object {
        private const val TAG = "ListVideoPlayer"
    }

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    //音量进度条的左侧图标
    private var mDialogVolumeIcon: ImageView? = null

    //亮度进度条的progress
    private var mDialogBrightnessProgressBar: ProgressBar? = null

    private lateinit var binding: LayoutListVideoPlayerBinding
    private var mCoverOriginId = 0
    private var mCoverDefaultRes = 0
    private var mCoverOriginUrl = ""

    override fun initInflate(context: Context?) {
        binding = LayoutListVideoPlayerBinding.inflate(LayoutInflater.from(context), this, true)
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
    }

    /**
     * 设置数据
     * @param shareCount 分享个数
     * @param replyCount 回复个数
     * @param duration 时长
     */
    fun setData(shareCount: String, replyCount: String, duration: String) {
        binding.shareTv.text = shareCount
        binding.commentTv.text = replyCount
        binding.durationTv.text = duration
    }

    //加载封面
    fun loadCoverImage(url: String, res: Int) {
        mCoverOriginUrl = url
        mCoverDefaultRes = res
        val radius = SizeUtil.dp2px(5f)
        GlideUtil.showImageFromUrl(
            binding.thumbImage, url,
            SectionRoundedCorners(radius, radius), res, res
        )
    }

    //加载封面
    fun loadCoverImage(resId: Int, res: Int) {
        mCoverOriginId = resId
        mCoverDefaultRes = res
        val radius = SizeUtil.dp2px(5f)
        GlideUtil.showImageFromResource(
            binding.thumbImage, resId,
            SectionRoundedCorners(radius, radius)
        )
    }

    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        val player = super.startWindowFullscreen(context, actionBar, statusBar) as ListVideoPlayer
        if (mCoverOriginUrl.isNotBlank()) {
            player.loadCoverImage(mCoverOriginUrl, mCoverDefaultRes)
        } else if (mCoverOriginId != 0) {
            player.loadCoverImage(mCoverOriginId, mCoverDefaultRes)
        }
        return player
    }

    override fun showSmallVideo(size: Point?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        return (super.showSmallVideo(size, actionBar, statusBar) as ListVideoPlayer).apply {
            mStartButton.visibility = View.GONE
            mStartButton = null
        }
    }

    override fun cloneParams(from: GSYBaseVideoPlayer?, to: GSYBaseVideoPlayer?) {
        super.cloneParams(from, to)
        (to as ListVideoPlayer).mShowFullAnimation = (from as ListVideoPlayer).mShowFullAnimation
    }

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
            (oldF as ListVideoPlayer).mIfCurrentIsFullscreen = false
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

    override fun onClick(v: View?) {
        if (mIfCurrentIsFullscreen) {
            super.onClick(v)
        } else {
            mOnNormalClickListener?.invoke()
        }
    }

    override fun onClickUiToggle(e: MotionEvent?) {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE)
            return
        }
        LogUtil.i(TAG, "onClickUiToggle")
        super.onClickUiToggle(e)
        darkSmallControll(false)
        hideBottomProgressBarWhenFull()
    }

    //显示
    override fun changeUiToNormal() {
        super.changeUiToNormal()
        LogUtil.i(TAG, "changeUiToNormal")
        mBottomContainer.visible()
        if (isIfCurrentIsFullscreen) {
            binding.smallDataLl.gone()
            binding.fullBottomControlLl.visible()
            binding.durationTv.gone()
        } else {
            binding.smallDataLl.visible()
            binding.fullBottomControlLl.gone()
            binding.durationTv.visible()
        }
        binding.smallControlLl.invisible()
    }

    //准备
    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        LogUtil.i(TAG, "changeUiToPreparingShow")
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
        darkSmallControll(false)
        if (isIfCurrentIsFullscreen) {  //全屏
            binding.smallDataLl.gone()
            binding.fullBottomControlLl.visible()
            binding.smallControlLl.gone()
        } else {
            binding.smallDataLl.visible()
            binding.fullBottomControlLl.gone()
            binding.smallControlLl.visible()
        }
        binding.durationTv.gone()
    }

    //暂停播放
    override fun changeUiToPauseShow() {
        super.changeUiToPauseShow()
        LogUtil.i(TAG, "changeUiToPauseShow")
        if (isIfCurrentIsFullscreen) {
            binding.smallDataLl.gone()
            binding.fullBottomControlLl.visible()
            binding.smallControlLl.gone()
        } else {
            binding.smallDataLl.visible()
            binding.fullBottomControlLl.gone()
            binding.smallControlLl.visible()
        }
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
    }

    //隐藏控件
    override fun hideAllWidget() {
        super.hideAllWidget()
        LogUtil.i(TAG, "hideAllWidget")
        darkSmallControll(true)
        hideBottomProgressBarWhenFull()
    }

    override fun startAfterPrepared() {
        super.startAfterPrepared()
        LogUtil.i(TAG, "startAfterPrepared")
        GSYVideoManager.instance().isNeedMute = VideoConfig.muteInTheList
    }

    override fun onAutoCompletion() {
        super.onAutoCompletion()
        if (!mIfCurrentIsFullscreen) {
            clickStartIcon()  //非全屏时重新开始播放
        }
    }

    //全屏返回
    override fun resolveNormalVideoShow(oldF: View?, vp: ViewGroup?, gsyVideoPlayer: GSYVideoPlayer?) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
        //全屏返回时如果是暂停状态或者是播放完成状态，则恢复播放
        if (mCurrentState == GSYVideoView.CURRENT_STATE_PAUSE || mCurrentState == GSYVideoView.CURRENT_STATE_AUTO_COMPLETE) {
            clickStartIcon()
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

    private fun darkSmallControll(dark: Boolean) {
        if (dark) {
            binding.fullscreen.imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
            binding.danmaIv.imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
            binding.volumeIv.imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
        } else {
            binding.fullscreen.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            binding.danmaIv.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
            binding.volumeIv.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
        }
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

    //非全屏时点击整个控件
    private var mOnNormalClickListener: (() -> Unit)? = null

    fun setOnNormalClickListener(listener: (() -> Unit)) {
        this.mOnNormalClickListener = listener
    }

}
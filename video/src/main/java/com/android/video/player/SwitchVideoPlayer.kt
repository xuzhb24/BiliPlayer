package com.android.video.player

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.widget.*
import com.android.util.*
import com.android.util.glide.GlideUtil
import com.android.util.glide.SectionRoundedCorners
import com.android.video.R
import com.android.video.databinding.LayoutSwitchVideoPlayerBinding
import com.android.video.entity.VideoBean
import com.android.video.util.VideoConfig
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.utils.Debuger
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
 * Created by xuzhb on 2022/3/16
 * Desc:上下切换播放器
 */
class SwitchVideoPlayer : StandardGSYVideoPlayer {

    companion object {
        private const val TAG = "SwitchVideoPlayer"
    }

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    //音量进度条的左侧图标
    private var mDialogVolumeIcon: ImageView? = null

    //亮度进度条的progress
    private var mDialogBrightnessProgressBar: ProgressBar? = null

    private lateinit var binding: LayoutSwitchVideoPlayerBinding
    private var mCoverOriginUrl = ""  //封面网络图
    private var mCoverOriginId = 0    //封面资源id
    private var mCoverDefaultRes = 0  //封面缺省图
    private var mCurrentVideo: VideoBean? = null  //绑定的视频

    private var mOnSeekBarChangeListener: SeekBar.OnSeekBarChangeListener? = null

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

    //设置当前视频信息
    fun setVideoInfo(video: VideoBean?) {
        mCurrentVideo = video
//        if (fullWindowPlayer != null) {
//            //拷贝至全屏播放器，非全屏和全屏时的播放器是两个不同的控件
//            (fullWindowPlayer as SwitchVideoPlayer).mCurrentVideo = video
//        }
    }

    override fun initInflate(context: Context?) {
        binding = LayoutSwitchVideoPlayerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun init(context: Context?) {
        super.init(context)
        if (mThumbImageViewLayout != null && (mCurrentState == -1
                    || mCurrentState == CURRENT_STATE_NORMAL
                    || mCurrentState == CURRENT_STATE_ERROR)
        ) {
            mThumbImageViewLayout.visible()
        }
        //增加SeekBar拖动区域
        binding.progressLl.setOnTouchListener { v, event ->
            val seekRect = Rect()
            mProgressBar.getHitRect(seekRect)
            if (event.y >= seekRect.top - 500 && event.y <= seekRect.bottom + 500) {
                val y = seekRect.top + seekRect.height() / 2f
                var x = event.x - seekRect.left
                if (x < 0) {
                    x = 0f
                } else if (x > seekRect.width()) {
                    x = seekRect.width().toFloat()
                }
                val ev = MotionEvent.obtain(event.downTime, event.eventTime, event.action, x, y, event.metaState)
                mProgressBar.onTouchEvent(ev)
                mProgressBar?.visible()
            }
            false
        }
        mProgressBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                showDragProgressTextOnSeekBar(fromUser, progress)
                binding.currentTimeTv.text = CommonUtil.stringForTime(progress * duration / 100)
                binding.totalTimeTv.text = CommonUtil.stringForTime(duration)
                mOnSeekBarChangeListener?.onProgressChanged(seekBar, progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mHadSeekTouch = true
                if (mIfCurrentIsFullscreen) {
                    binding.timeProgressLl.gone()
                } else {
                    binding.timeProgressLl.visible()
                }
                mOnSeekBarChangeListener?.onStartTrackingTouch(seekBar)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (mVideoAllCallBack != null && isCurrentMediaListener) {
                    if (isIfCurrentIsFullscreen) {
                        Debuger.printfLog("onClickSeekbarFullscreen")
                        mVideoAllCallBack.onClickSeekbarFullscreen(mOriginUrl, mTitle, this)
                    } else {
                        Debuger.printfLog("onClickSeekbar")
                        mVideoAllCallBack.onClickSeekbar(mOriginUrl, mTitle, this)
                    }
                }
                if (gsyVideoManager != null && mHadPlay) {
                    try {
                        val time = seekBar!!.progress * duration / 100
                        gsyVideoManager.seekTo(time.toLong())
                    } catch (e: Exception) {
                        Debuger.printfWarning(e.toString())
                    }
                }
                mHadSeekTouch = false
                binding.timeProgressLl.gone()
                mOnSeekBarChangeListener?.onStopTrackingTouch(seekBar)
            }
        })
    }

    //将自定义的效果也设置到全屏
    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        val player = super.startWindowFullscreen(context, actionBar, statusBar) as SwitchVideoPlayer
        if (mCoverOriginUrl.isNotBlank()) {
            player.loadCoverImage(mCoverOriginUrl, mCoverDefaultRes)
        } else if (mCoverOriginId != 0) {
            player.loadCoverImage(mCoverOriginId, mCoverDefaultRes)
        }
        return player
    }

    //显示小窗口
    override fun showSmallVideo(size: Point?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        return (super.showSmallVideo(size, actionBar, statusBar) as SwitchVideoPlayer).apply {
            mStartButton.visibility = View.GONE
            mStartButton = null
        }
    }

    //克隆切换参数
    override fun cloneParams(from: GSYBaseVideoPlayer?, to: GSYBaseVideoPlayer?) {
        super.cloneParams(from, to)
        LogUtil.i(TAG, "cloneParams")
        //这里做一些状态的保存，不然旋转屏幕时会丢失数据
        (to as SwitchVideoPlayer).mShowFullAnimation = (from as SwitchVideoPlayer).mShowFullAnimation
        to.mCurrentVideo = from.mCurrentVideo
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
            (oldF as SwitchVideoPlayer).mIfCurrentIsFullscreen = false
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

    //点击播放器
    override fun onClickUiToggle(e: MotionEvent?) {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE)
            return
        }
        LogUtil.i(TAG, "onClickUiToggle")
        super.onClickUiToggle(e)
        hideBottomProgressBarWhenFull()
        if (!mIfCurrentIsFullscreen) {
            mBottomContainer.visible()
            clickStartIcon()
        }
    }

    //显示
    override fun changeUiToNormal() {
        super.changeUiToNormal()
        LogUtil.i(TAG, "changeUiToNormal")
        mBottomContainer.visible()
        setBottomControlLayoutState()
        binding.switchPauseIv.gone()
        if (!mIfCurrentIsFullscreen) {
            mTopContainer.gone()
            mProgressBar.gone()
            mBottomProgressBar.visible()
            mCurrentTimeTextView.gone()
            mTotalTimeTextView.gone()
        } else {
            mCurrentTimeTextView.visible()
            mTotalTimeTextView.visible()
        }
        LayoutParamsUtil.setMargin(
            binding.fullscreen,
            0, if (mIfCurrentIsFullscreen) SizeUtil.dp2pxInt(10f) else 0,
            if (mIfCurrentIsFullscreen) SizeUtil.dp2pxInt(5f) else 0,
            if (mIfCurrentIsFullscreen) SizeUtil.dp2pxInt(2f) else SizeUtil.dp2pxInt(6f)
        )
        if (mIfCurrentIsFullscreen) {
            (binding.surfaceContainer.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ABOVE)
            binding.surfaceContainer.translationY = 0f
        } else {
            (binding.surfaceContainer.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.ABOVE, R.id.layout_bottom)
            binding.surfaceContainer.translationY = SizeUtil.dp2px(10f)
        }
    }

    //准备
    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        LogUtil.i(TAG, "changeUiToPreparingShow")
        mStartButton.visible()
        if (!mIfCurrentIsFullscreen) {
            mTopContainer.gone()
            mProgressBar.gone()
            mBottomProgressBar.visible()
        }
    }

    //缓冲
    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        LogUtil.i(TAG, "changeUiToPlayingBufferingShow")
        mStartButton.visible()
        if (!mIfCurrentIsFullscreen) {
            mTopContainer.gone()
            mProgressBar.gone()
            mBottomProgressBar.visible()
        }
    }

    //开始播放
    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        LogUtil.i(TAG, "changeUiToPlayingShow")
        setBottomControlLayoutState()
        binding.switchPauseIv.gone()
        if (!mIfCurrentIsFullscreen) {
            mTopContainer.gone()
            mProgressBar.gone()
            mBottomProgressBar.visible()
        }
        //设置视频播放尺寸
        val ratio = (mCurrentVideo?.width ?: 1920) / (mCurrentVideo?.height ?: 1080)
        if (ratio <= 1080 / 1920) {
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL)  //全屏
        } else {
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)  //默认比例
        }
    }

    //暂停播放
    override fun changeUiToPauseShow() {
        super.changeUiToPauseShow()
        LogUtil.i(TAG, "changeUiToPauseShow")
        setBottomControlLayoutState()
        if (mIfCurrentIsFullscreen) {
            binding.switchPauseIv.gone()
        } else {
            mTopContainer.gone()
            binding.switchPauseIv.visible()
        }
        if (!mIfCurrentIsFullscreen) {
            mProgressBar.visible()
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
        if (!mIfCurrentIsFullscreen) {
            mTopContainer.gone()
        }
        LogUtil.i(TAG, "changeUiToCompleteShow")
    }

    //隐藏控件
    override fun hideAllWidget() {
        super.hideAllWidget()
        LogUtil.i(TAG, "hideAllWidget")
        hideBottomProgressBarWhenFull()
        if (!mIfCurrentIsFullscreen) {
            mBottomContainer.visible()
            mProgressBar.gone()
        }
    }

    //播放完成
    override fun onAutoCompletion() {
        super.onAutoCompletion()
        clickStartIcon()  //重新开始播放
    }

    //全屏
    override fun resolveFullVideoShow(context: Context?, gsyVideoPlayer: GSYBaseVideoPlayer?, frameLayout: FrameLayout?) {
        super.resolveFullVideoShow(context, gsyVideoPlayer, frameLayout)
        LogUtil.i(TAG, "resolveFullVideoShow")
        mBottomContainer.visible()
    }

    //全屏返回
    override fun resolveNormalVideoShow(oldF: View?, vp: ViewGroup?, gsyVideoPlayer: GSYVideoPlayer?) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
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
    }

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
            ToastUtil.showToast("加载失败", true)
            return
        }
        if (!NetworkUtil.isWifiConnected(context) && !VideoConfig.hasNoticeWithoutWifi) {
            VideoConfig.hasNoticeWithoutWifi = true
            ToastUtil.showToast("正在使用非WiFi网络，请注意流量消耗", true)
        }
        super.startPlayLogic()
    }

    //播放结束
    override fun release() {
        LogUtil.i(TAG, "release")
        cancelProgressTimer()
        cancelDismissControlViewTimer()
        super.release()
    }

    private fun setBottomControlLayoutState() {
        if (isIfCurrentIsFullscreen) {  //全屏
            mBottomContainer.setBackgroundResource(R.drawable.shape_video_mask)
            binding.smallBottomControlLl.gone()
            binding.fullBottomControlCl.visible()
            binding.fullPraiseCountTv.text = formatCountStr(mCurrentVideo?.collectionCount, "")
            binding.fullCommentCountTv.text = formatCountStr(mCurrentVideo?.replyCount, "")
        } else {
            mBottomContainer.setBackgroundColor(Color.TRANSPARENT)
            binding.smallBottomControlLl.visible()
            binding.fullBottomControlCl.gone()
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

    fun setOnSeekBarChangeListener(listener: SeekBar.OnSeekBarChangeListener) {
        mOnSeekBarChangeListener = listener
    }

}
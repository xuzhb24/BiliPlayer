package com.android.video.player

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.util.*
import com.android.util.glide.GlideUtil
import com.android.video.R
import com.android.video.adapter.FullVideoReleatedAdapter
import com.android.video.databinding.LayoutDetailVideoPlayerBinding
import com.android.video.entity.VideoBean
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
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
 * Created by xuzhb on 2021/12/27
 * Desc:详情页播放器
 */
class DetailVideoPlayer : StandardGSYVideoPlayer {

    companion object {
        private const val TAG = "DetailVideoPlayer"
    }

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    //音量进度条的左侧图标
    private var mDialogVolumeIcon: ImageView? = null

    //亮度进度条的progress
    private var mDialogBrightnessProgressBar: ProgressBar? = null

    private lateinit var binding: LayoutDetailVideoPlayerBinding
    private var mCoverOriginUrl = ""  //封面网络图
    private var mCoverOriginId = 0    //封面资源id
    private var mCoverDefaultRes = 0  //封面缺省图
    private var mCurrentVideo: VideoBean? = null  //绑定的视频
    private var mReleatedVideoList: MutableList<VideoBean> = mutableListOf()  //推荐视频列表

    //用户按下返回键退出（不包括全屏时按下返回键），此时不显示底部控制栏
    var isBackPressed = false

    //加载封面
    fun loadCoverImage(url: String, res: Int) {
        mCoverOriginUrl = url
        mCoverDefaultRes = res
        Glide.with(context)
            .setDefaultRequestOptions(RequestOptions().frame(1000000).centerCrop().error(res).placeholder(res))
            .load(url)
            .into(binding.thumbImage)
    }

    //加载封面
    fun loadCoverImage(id: Int, res: Int) {
        mCoverOriginId = id
        mCoverDefaultRes = res
        binding.thumbImage.setImageResource(id)
    }

    //设置当前视频信息以及相关推荐视频列表
    fun setVideoInfo(video: VideoBean?, list: MutableList<VideoBean>?) {
        mCurrentVideo = video
        mReleatedVideoList.clear()
        list?.let { mReleatedVideoList.addAll(it) }
        if (fullWindowPlayer != null) {
            //拷贝至全屏播放器，非全屏和全屏时的播放器是两个不同的控件
            (fullWindowPlayer as DetailVideoPlayer).mCurrentVideo = video
            (fullWindowPlayer as DetailVideoPlayer).mReleatedVideoList.let {
                it.clear()
                if (list != null) {
                    it.addAll(list)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initInflate(context: Context?) {
        binding = LayoutDetailVideoPlayerBinding.inflate(LayoutInflater.from(context), this, true)
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
            }
            false
        }
    }

    override fun init(context: Context?) {
        super.init(context)
        if (mThumbImageViewLayout != null && (mCurrentState == -1
                    || mCurrentState == CURRENT_STATE_NORMAL
                    || mCurrentState == CURRENT_STATE_ERROR)
        ) {
            mThumbImageViewLayout.visible()
        }
    }

    //将自定义的效果也设置到全屏
    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        val player = super.startWindowFullscreen(context, actionBar, statusBar) as DetailVideoPlayer
        if (mCoverOriginUrl.isNotBlank()) {
            player.loadCoverImage(mCoverOriginUrl, mCoverDefaultRes)
        } else if (mCoverOriginId != 0) {
            player.loadCoverImage(mCoverOriginId, mCoverDefaultRes)
        }
        return player
    }

    //显示小窗口
    override fun showSmallVideo(size: Point?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        return (super.showSmallVideo(size, actionBar, statusBar) as DetailVideoPlayer).apply {
            mStartButton.visibility = View.GONE
            mStartButton = null
        }
    }

    //克隆切换参数
    override fun cloneParams(from: GSYBaseVideoPlayer?, to: GSYBaseVideoPlayer?) {
        super.cloneParams(from, to)
        //这里做一些状态的保存，不然旋转屏幕时会丢失数据
        (to as DetailVideoPlayer).mShowFullAnimation = (from as DetailVideoPlayer).mShowFullAnimation
        to.mCurrentVideo = from.mCurrentVideo
        val list: MutableList<VideoBean> = mutableListOf()
        list.addAll(from.mReleatedVideoList)
        to.mReleatedVideoList = list
        to.mOnVideoChangeListener = from.mOnVideoChangeListener
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
            (oldF as DetailVideoPlayer).mIfCurrentIsFullscreen = false
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
    }

    //显示
    override fun changeUiToNormal() {
        super.changeUiToNormal()
        LogUtil.i(TAG, "changeUiToNormal")
        showBottomContainer()
        //提示正在使用流量播放
        if (!mHadPlay && !NetworkUtil.isWifiConnected(context)) {
            binding.flowTv.visible()
        } else {
            binding.flowTv.gone()
        }
        binding.reloadLl.gone()
        binding.completeSmallLayout.smallRootLl.gone()
        binding.completeFullLayout.fullRootLl.gone()
    }

    //准备
    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        LogUtil.i(TAG, "changeUiToPreparingShow")
        mStartButton.visible()
        showBottomContainer()
    }

    //缓冲
    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        LogUtil.i(TAG, "changeUiToPlayingBufferingShow")
        mStartButton.visible()
        showBottomContainer()
    }

    //开始播放
    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        LogUtil.i(TAG, "changeUiToPlayingShow")
        showBottomContainer()
    }

    //暂停播放
    override fun changeUiToPauseShow() {
        super.changeUiToPauseShow()
        LogUtil.i(TAG, "changeUiToPauseShow")
        showBottomContainer()
    }

    //播放出错
    override fun changeUiToError() {
        super.changeUiToError()
        LogUtil.i(TAG, "changeUiToError")
        showBottomContainer()
    }

    //播放完成
    override fun changeUiToCompleteShow() {
        super.changeUiToCompleteShow()
        LogUtil.i(TAG, "changeUiToCompleteShow")
        binding.flowTv.gone()
        if (mReleatedVideoList.isNullOrEmpty()) {
            mBottomContainer.visible()
            binding.completeFullLayout.fullRootLl.gone()
            binding.completeSmallLayout.smallRootLl.gone()
        } else {
            mBottomContainer.gone()
            mCurrentTimeTextView
            if (mIfCurrentIsFullscreen) {  //全屏
                binding.completeFullLayout.fullRootLl.visible()
                binding.completeSmallLayout.smallRootLl.gone()
                binding.completeFullLayout.let {
                    GlideUtil.showCircleImageFromUrl(
                        it.fullHeadIv, mCurrentVideo?.upIcon ?: "",
                        R.drawable.ic_video_place_holder, R.drawable.ic_video_place_holder
                    )
                    it.fullNameTv.text = mCurrentVideo?.upName ?: ""
                    //重播
                    it.fullRepeatTv.setOnClickListener {
                        playNextVideo(mCurrentVideo)
                    }
                    it.fullRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    it.fullRv.adapter = FullVideoReleatedAdapter().apply {
                        setNewInstance(mReleatedVideoList)
                        //推荐视频列表Item
                        setOnItemClickListener { adapter, view, position ->
                            val nextVideo = getItem(position)
                            playNextVideo(nextVideo)
                            mOnVideoChangeListener?.invoke(nextVideo.id)
                        }
                    }
                }
            } else {  //非全屏
                binding.completeFullLayout.fullRootLl.gone()
                binding.completeSmallLayout.smallRootLl.visible()
                binding.completeSmallLayout.let {
                    val nextVideo = mReleatedVideoList[0]
                    LogUtil.i(TAG, Gson().toJson(nextVideo))
                    GlideUtil.showImageFromUrl(it.smallCoverIv, nextVideo.cover ?: "", RoundedCorners(SizeUtil.dp2pxInt(3.5f)))
                    it.smallDurationTv.text = DateUtil.getDuration(nextVideo.duration ?: 0)
                    it.smallTitleTv.text = nextVideo.title
                    it.smallUpTv.text = nextVideo.upName
                    it.smallShareTv.text = formatCountStr(nextVideo.shareCount)
                    it.smallCommentTv.text = formatCountStr(nextVideo.replyCount)
                    //重播
                    it.smallRepeatTv.setOnClickListener {
                        playNextVideo(mCurrentVideo)
                    }
                    //立即播放
                    it.smallPlayTv.setOnClickListener {
                        playNextVideo(nextVideo)
                        mOnVideoChangeListener?.invoke(nextVideo.id)
                    }
                    //封面
                    it.smallCoverIv.setOnClickListener {
                        playNextVideo(nextVideo)
                        mOnVideoChangeListener?.invoke(nextVideo.id)
                    }
                }
            }
        }
    }

    //隐藏控件
    override fun hideAllWidget() {
        super.hideAllWidget()
        LogUtil.i(TAG, "hideAllWidget")
        hideBottomProgressBarWhenFull()
        binding.flowTv.gone()
        mOnHideAllWidgetListener?.invoke()
    }

    //全屏
    override fun resolveFullVideoShow(context: Context?, gsyVideoPlayer: GSYBaseVideoPlayer?, frameLayout: FrameLayout?) {
        super.resolveFullVideoShow(context, gsyVideoPlayer, frameLayout)
        LogUtil.i(TAG, "resolveFullVideoShow")
        mBottomContainer.visible()
    }

    //恢复
    override fun resolveNormalVideoShow(oldF: View?, vp: ViewGroup?, gsyVideoPlayer: GSYVideoPlayer?) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
        loadCoverImage(mCurrentVideo?.cover ?: "", -1)
    }

    //定义开始按键显示
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

    //全屏图标
    override fun getEnlargeImageRes() = R.drawable.ic_video_enlarge

    //退出全屏图标
    override fun getShrinkImageRes() = R.drawable.ic_video_shrink

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
                    val adjustSize = if (mIfCurrentIsFullscreen) 0 else SizeUtil.dp2pxInt(50f)
                    y = location[1] - adjustSize
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
                    val adjustSize = if (mIfCurrentIsFullscreen) 0 else SizeUtil.dp2pxInt(50f)
                    y = location[1] - adjustSize
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
                    val adjustSize = if (mIfCurrentIsFullscreen) 0 else SizeUtil.dp2pxInt(50f)
                    y = location[1] - adjustSize
                }
            }
        }
        if (!mProgressDialog.isShowing) {
            mProgressDialog.show()
        }
        mDialogSeekTime.text = seekTime
        mDialogTotalTime.text = totalTime
    }

    override fun setProgressAndTime(progress: Int, secProgress: Int, currentTime: Int, totalTime: Int, forceChange: Boolean) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange)
        val totalTimeStr = CommonUtil.stringForTime(totalTime)
        val currentTimeStr = if (currentTime > 0) CommonUtil.stringForTime(currentTime) else ""
        if (mIfCurrentIsFullscreen) {
            mTotalTimeTextView.text = totalTimeStr
            mCurrentTimeTextView.visible()
            mCurrentTimeTextView.text = currentTimeStr
            mTotalTimeTextView.textSize = 12f
            mCurrentTimeTextView.textSize = 12f
        } else {
            mTotalTimeTextView.text = "$currentTimeStr/$totalTimeStr"
            mCurrentTimeTextView.gone()
            mCurrentTimeTextView.text = currentTimeStr
            mTotalTimeTextView.textSize = 10.5f
            mCurrentTimeTextView.textSize = 10.5f
        }
    }

    //流量播放时不显示wifi确定框
    override fun isShowNetConfirm() = false

    override fun startPlayLogic() {
        if (!NetworkUtils.isAvailable(context)) {
            ToastUtil.showToast("当前找不到网络", true)
            binding.reloadLl.visible()
            return
        }
        super.startPlayLogic()
    }

    //播放结束
    override fun release() {
        LogUtil.i(TAG, "release")
        mBottomContainer.gone()
        mOnVideoChangeListener = null
        cancelProgressTimer()
        cancelDismissControlViewTimer()
        super.release()
    }

    //第一次播放时隐藏底部控制栏
    private fun showBottomContainer() {
        LogUtil.i(TAG, "mHadPlay:$mHadPlay isBackPressed:$isBackPressed")
        if (mHadPlay && !isBackPressed) {
            mBottomContainer.visible()
        } else {
            //前2秒或退出时不显示底部控制栏
            mBottomContainer.gone()
            if (isBackPressed) {  //退出时不显示加载动画
                mLoadingProgressBar.gone()
            }
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

    //播放下一个视频
    private fun playNextVideo(video: VideoBean?) {
        video?.let {
            loadCoverImage(it.cover ?: "", -1)
            setUp(it.playUrl, false, it.title)
            startPlayLogic()
        }
    }

    fun clickStart() {
        clickStartIcon()
    }

    //设置子控件点击事件
    fun setOnChildClickListener(id: Int, listener: OnClickListener) {
        findViewById<View>(id)?.setOnClickListener(listener)
    }

    //播放下一个视频时回调，id：视频id
    private var mOnVideoChangeListener: ((id: Int?) -> Unit)? = null

    fun setOnVideoChangeListener(listener: (id: Int?) -> Unit) {
        mOnVideoChangeListener = listener
    }

    //隐藏控件时回调
    private var mOnHideAllWidgetListener: (() -> Unit)? = null

    fun setOnHideAllWidgetListener(listener: (() -> Unit)) {
        this.mOnHideAllWidgetListener = listener
    }

}
package com.android.project.ui.detail

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.android.base.CommonBaseActivity
import com.android.base.DontSwipeBack
import com.android.project.R
import com.android.project.adapter.PhotoViewAdapter
import com.android.project.databinding.ActivityPhotoViewBinding
import com.android.project.entity.ItemBean
import com.android.project.util.*
import com.android.util.*
import com.android.widget.ExpandTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import java.io.File

/**
 * Created by xuzhb on 2022/4/13
 * Desc:图片查看器Activity
 */
class PhotoViewActivity : CommonBaseActivity<ActivityPhotoViewBinding>(), DontSwipeBack {

    companion object {
        private const val TAG = "PhotoViewActivity"
        private const val EXTRA_ITEM_BEAN = "EXTRA_ITEM_BEAN"
        private const val EXTRA_POSITION = "EXTRA_POSITION"
        private val REQUEST_PERMISSION: Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val REQUEST_PERMISSION_CODE = 1

        fun start(context: Context, item: ItemBean, position: Int) {
            if (CheckFastClickUtil.isFastClick()) {
                return
            }
            val intent = Intent(context, PhotoViewActivity::class.java)
            intent.putExtra(EXTRA_ITEM_BEAN, Gson().toJson(item))
            intent.putExtra(EXTRA_POSITION, position)
            context.startActivity(intent)
        }

    }

    private var mId: Int? = null             //当前稿件id
    private var mItemBean: ItemBean? = null  //当前稿件
    private var mImageUrlList: MutableList<String>? = null  //图片列表
    private var mCurrentPosition = -1

    override fun initBar() {
        StatusBarUtil.darkModeAndPadding(this, mTitleBar!!, Color.BLACK, 0f, false)
        window.navigationBarColor = Color.BLACK
    }

    override fun handleView(savedInstanceState: Bundle?) {
        mItemBean = Gson().fromJson(intent.getStringExtra(EXTRA_ITEM_BEAN) ?: "{}", ItemBean::class.java)
        mCurrentPosition = intent.getIntExtra(EXTRA_POSITION, 0)
        mId = getVideoId(mItemBean)
        mImageUrlList = mItemBean?.data?.content?.data?.urls ?: mutableListOf()
        initData(mItemBean, mImageUrlList)
        //关闭
        mTitleBar?.setOnLeftIconClickListener {
            finish()
        }
        //下载
//        binding.downloadIv.setOnClickListener {
//            if (PermissionUtil.requestPermissions(this, REQUEST_PERMISSION_CODE, *REQUEST_PERMISSION)) {
//                if (mCurrentPosition != -1 && !mImageUrlList.isNullOrEmpty()) {
//                    downloadPicture(mImageUrlList!![mCurrentPosition])
//                }
//            }
//        }
    }

    override fun initListener() {
    }

    private fun initData(item: ItemBean?, imageUrlList: MutableList<String>?) {
        if (item == null || imageUrlList.isNullOrEmpty()) {
            return
        }
        mTitleBar?.titleText = "${mCurrentPosition + 1}/${imageUrlList.size}"
        binding.photoVp.adapter = PhotoViewAdapter(this, imageUrlList)
        binding.photoVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                mCurrentPosition = position
                mTitleBar?.titleText = "${position + 1}/${imageUrlList.size}"
            }
        })
        binding.photoVp.offscreenPageLimit = imageUrlList.size
        binding.photoVp.currentItem = mCurrentPosition
        val owner = item.data.content?.data?.owner
        showCircleImageByCenterCrop(binding.headIv, owner?.avatar ?: "")
        binding.nameTv.text = owner?.nickname ?: ""
        binding.descTv.text = if (owner?.description.isNullOrBlank()) "ta暂时没有简介！" else owner?.description
        binding.titleEtv.contentText = getVideoDescription(item)
        binding.titleEtv.setOnTextClickListener(object : ExpandTextView.OnTextClickListener {
            override fun onContentTextClick(isExpand: Boolean) {
                PhotoDetailActivity.start(this@PhotoViewActivity, item)
            }

            override fun onLabelTextClick(isExpand: Boolean) {
                PhotoDetailActivity.start(this@PhotoViewActivity, item)
            }
        })
        binding.shareTv.text = formatCountStr(getVideoShareCount(item), "转发")
        binding.commentTv.text = formatCountStr(getVideoReplyCount(item), "评论")
        binding.praiseTv.text = formatCountStr(getVideoCollectionCount(item), "点赞")
        binding.downIv.setOnClickListener {
            mTitleBar?.leftIcon = null
            mTitleBar?.rightIcon = null
            binding.bottomCl.gone()
            binding.upIv.visible()
        }
        binding.upIv.setOnClickListener {
            binding.upIv.gone()
            mTitleBar?.leftIcon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_photo_close)
            mTitleBar?.rightIcon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_dot_while)
            binding.bottomCl.visible()
        }
    }

    //下载并保存图片
    private fun downloadPicture(url: String) {
        Glide.with(this).downloadOnly().load(url).into(object : CustomTarget<File>() {

            override fun onLoadStarted(placeholder: Drawable?) {
                super.onLoadStarted(placeholder)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                ToastUtil.showToast("下载失败")
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                val isSuccess = BitmapUtil.saveImageFileToGallery(applicationContext, resource, "${System.currentTimeMillis()}")
                ToastUtil.showToast(if (isSuccess) "保存成功" else "保存失败")
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(
            this, requestCode, permissions, grantResults,
            object : PermissionUtil.OnPermissionListener {
                override fun onPermissionGranted() {
                    if (mCurrentPosition != -1 && !mImageUrlList.isNullOrEmpty()) {
                        downloadPicture(mImageUrlList!![mCurrentPosition])
                    }
                }

                override fun onPermissionDenied(deniedPermissions: Array<String>) {
                    showToast("请先允许权限")
                }

                override fun onPermissionDeniedForever(deniedForeverPermissions: Array<String>) {
                }

            })
    }

    override fun finish() {
        super.finish()
    }

}
package com.android.project.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.android.project.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView

/**
 * Created by xuzhb on 2021/3/30
 * Desc:查看多张图片对应的Adapter
 */
class PhotoViewAdapter constructor(
    private val mActivity: Activity,
    private val mImageUrlList: MutableList<String>
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mActivity.applicationContext).inflate(R.layout.item_photo_view_pager, container, false)
        val progressBar: ProgressBar = layout.findViewById(R.id.progress_bar)
        val photoView: PhotoView = layout.findViewById(R.id.photo_view)
        progressBar.visibility = View.VISIBLE
        Glide.with(mActivity).asBitmap().load(mImageUrlList.get(position))
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    photoView.setImageBitmap(resource)
                    photoView.setZoomable(true)
                    progressBar.visibility = View.GONE
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    photoView.setImageResource(R.drawable.ic_photo_load_fail)
                    photoView.setZoomable(false)
                    progressBar.visibility = View.GONE
                }
            })
        photoView.setOnClickListener { mActivity.finish() }
        container.addView(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int = mImageUrlList.size
}
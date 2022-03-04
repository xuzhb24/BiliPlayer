package com.android.project.adapter.banner

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.youth.banner.adapter.BannerAdapter

/**
 * Created by xuzhb on 2020/8/3
 * Desc:youth.banner对应的图片适配器，加载本地图片
 */
class BannerImageResAdapter(
    imageRes: MutableList<Int>,
    private val mTransformation: BitmapTransformation? = null
) : BannerAdapter<Int, BannerImageResAdapter.ImageHolder>(imageRes) {

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ImageHolder {
        val imageView = ImageView(parent!!.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return ImageHolder(imageView)
    }

    override fun onBindView(holder: ImageHolder?, data: Int?, position: Int, size: Int) {
        holder?.let {
            Glide.with(it.imageView).load(data).apply {
                if (mTransformation != null) {
                    transform(CenterCrop(), mTransformation)  //解决Glide和ImageView自身的CENTER_CROP冲突
                } else {
                    transform(CenterCrop())
                }
            }.into(it.imageView)
        }
    }

    class ImageHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view as ImageView
    }

}
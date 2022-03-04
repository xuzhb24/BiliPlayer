package com.android.util.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.android.util.LogUtil
import com.android.util.SizeUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

/**
 * Created by xuzhb on 2020/10/21
 * Desc:Glide工具类
 */
object GlideUtil {

    //从网络加载并显示图片
    fun showImageFromUrl(
        iv: ImageView,
        url: String,
        transformation: BitmapTransformation? = null,
        @DrawableRes placeResId: Int = -1,
        @DrawableRes errorResId: Int = -1
    ) {
        loadUrl(iv, url, transformation, placeResId, errorResId).into(iv)
    }

    //从网络加载并显示圆形图片
    fun showCircleImageFromUrl(
        iv: ImageView,
        url: String,
        @DrawableRes placeResId: Int = -1,
        @DrawableRes errorResId: Int = -1
    ) {
        showImageFromUrl(iv, url, CircleCrop(), placeResId, errorResId)
    }

    //从网络加载并显示圆角图片，radius：以dp为单位
    fun showRoundedImageFromUrl(
        iv: ImageView,
        url: String,
        radius: Float,
        @DrawableRes placeResId: Int = -1,
        @DrawableRes errorResId: Int = -1
    ) {
        showImageFromUrl(iv, url, RoundedCorners(SizeUtil.dp2pxInt(radius)), placeResId, errorResId)
    }

    //从网络加载并显示部分圆角图片
    fun showSectionRoundedImageFromUrl(
        iv: ImageView,
        url: String,
        topLeftRadius: Float = 0f,
        topRightRadius: Float = 0f,
        bottomLeftRadius: Float = 0f,
        bottomRightRadius: Float = 0f,
        @DrawableRes placeResId: Int = -1,
        @DrawableRes errorResId: Int = -1
    ) {
        showImageFromUrl(
            iv, url,
            SectionRoundedCorners(
                SizeUtil.dp2px(topLeftRadius),
                SizeUtil.dp2px(topRightRadius),
                SizeUtil.dp2px(bottomLeftRadius),
                SizeUtil.dp2px(bottomRightRadius)
            ),
            placeResId, errorResId
        )
    }

    //从本地加载并显示图片
    fun showImageFromResource(
        iv: ImageView,
        @DrawableRes resId: Int,
        transformation: BitmapTransformation?
    ) {
        loadResource(iv, resId, transformation).into(iv)
    }

    //从本地加载并显示圆形图片
    fun showCircleImageFromResource(
        iv: ImageView,
        @DrawableRes resId: Int
    ) {
        showImageFromResource(iv, resId, CircleCrop())
    }

    //从本地加载并显示圆角图片，radius：以dp为单位
    fun showRoundedImageFromResource(
        iv: ImageView,
        @DrawableRes resId: Int,
        radius: Float
    ) {
        showImageFromResource(iv, resId, RoundedCorners(SizeUtil.dp2pxInt(radius)))
    }

    //从本地加载并显示部分圆角图片
    fun showSectionRoundedImageFromResource(
        iv: ImageView,
        @DrawableRes resId: Int,
        topLeftRadius: Float = 0f,
        topRightRadius: Float = 0f,
        bottomLeftRadius: Float = 0f,
        bottomRightRadius: Float = 0f
    ) {
        showImageFromResource(
            iv, resId,
            SectionRoundedCorners(
                SizeUtil.dp2px(topLeftRadius),
                SizeUtil.dp2px(topRightRadius),
                SizeUtil.dp2px(bottomLeftRadius),
                SizeUtil.dp2px(bottomRightRadius)
            )
        )
    }

    /**
     * 加载网络图片
     * @param url 图片链接
     * @param transformation 图片转换，CircleCrop：圆形图片，RoundedCorners：圆角图片，SectionRoundedCorners：指定部分圆角图片
     * @param placeResId 占位图
     * @param errorResId 错误图
     */
    fun loadUrl(
        iv: ImageView,
        url: String,
        transformation: BitmapTransformation? = null,
        @DrawableRes placeResId: Int = -1,
        @DrawableRes errorResId: Int = -1
    ): RequestBuilder<Drawable> {
        return Glide.with(iv).load(url).apply {
            if (iv.scaleType == ImageView.ScaleType.CENTER_CROP) {
                if (transformation != null) {
                    transform(CenterCrop(), transformation)
                } else {
                    transform(CenterCrop())
                }
            } else {
                if (transformation != null) {
                    transform(transformation)
                }
            }
            if (placeResId != -1) {
                if (transformation != null) {
                    thumbnail(loadResource(iv, placeResId, transformation))  //占位图图片转换
                } else {
                    placeholder(placeResId)
                }
            }
            if (errorResId != -1) {
                if (transformation != null) {
                    thumbnail(loadResource(iv, errorResId, transformation))  //错误图图片转换
                } else {
                    error(errorResId)
                }
            }
        }
    }

    /**
     * 加载本地图片
     * @param resId 本地图片
     * @param transformation 图片转换，CircleCrop：圆形图片，RoundedCorners：圆角图片，SectionRoundedCorners：指定部分圆角图片
     */
    fun loadResource(
        iv: ImageView,
        @DrawableRes resId: Int,
        transformation: BitmapTransformation?
    ): RequestBuilder<Drawable> {
        return if (iv.scaleType == ImageView.ScaleType.CENTER_CROP) {  //解决Glide和ImageView自身的CENTER_CROP冲突
            if (transformation != null) {
                Glide.with(iv).load(resId).transform(CenterCrop(), transformation)
            } else {
                Glide.with(iv).load(resId).transform(CenterCrop())
            }
        } else {
            if (transformation != null) {
                Glide.with(iv).load(resId).transform(transformation)
            } else {
                Glide.with(iv).load(resId)
            }
        }
    }

    //加载网络图片并转换成Bitmap
    fun loadUrlAsBitmap(
        context: Context,
        url: String,
        method: (resource: Bitmap) -> Unit
    ) {
        Glide.with(context).asBitmap().load(url).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                LogUtil.i("Bitmap Size", "${resource.width} x ${resource.height}")
                method(resource)
            }
        })
    }

    //加载网络图片并转换成drawable
    fun loadUrlAsDrawable(
        context: Context,
        url: String,
        method: (resource: Drawable) -> Unit
    ) {
        Glide.with(context).load(url).into(object : SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                method(resource)
            }
        })
    }

}
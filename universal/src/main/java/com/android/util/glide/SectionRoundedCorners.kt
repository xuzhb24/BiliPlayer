package com.android.util.glide

import android.graphics.*
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

/**
 * Created by xuzhb on 2020/8/13
 * Desc:Glide加载部分圆角
 */
class SectionRoundedCorners constructor(
    private val topLeftRadius: Float = 0f,
    private val topRightRadius: Float = 0f,
    private val bottomLeftRadius: Float = 0f,
    private val bottomRightRadius: Float = 0f
) : BitmapTransformation() {

    private val ID_BYTES =
        "com.android.util.glide.RoundCorner$topLeftRadius$topRightRadius$bottomLeftRadius$bottomRightRadius".toByteArray(Key.CHARSET)

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val bitmap = pool.get(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setHasAlpha(true)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val radii = floatArrayOf(
            topLeftRadius, topLeftRadius, topRightRadius, topRightRadius,
            bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius
        )
        val path = Path()
        path.addRoundRect(rect, radii, Path.Direction.CW)
        canvas.drawPath(path, paint)
        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is SectionRoundedCorners) {
            return topLeftRadius == other.topLeftRadius &&
                    topRightRadius == other.topRightRadius &&
                    bottomLeftRadius == other.bottomLeftRadius &&
                    bottomRightRadius == other.bottomRightRadius
        }
        return false
    }

    override fun hashCode(): Int {
        return ID_BYTES.hashCode() + topLeftRadius.hashCode() + topRightRadius.hashCode() +
                bottomLeftRadius.hashCode() + bottomRightRadius.hashCode()
    }

}
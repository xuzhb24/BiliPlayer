package com.android.util

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.RenderScript.RSMessageHandler
import android.renderscript.ScriptIntrinsicBlur
import android.text.TextUtils
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.android.base.BaseApplication
import com.android.util.FileUtil
import com.android.util.IOUtil
import com.android.util.LogUtil
import java.io.*

/**
 * Create by xuzhb on 2019/10/11
 * Desc:图片工具
 */
object BitmapUtil {

    //Bitmap转化byte数组
    fun bitmap2Bytes(
        bitmap: Bitmap?,
        format: CompressFormat = CompressFormat.PNG,
        @IntRange(from = 0, to = 100) quality: Int = 100
    ): ByteArray? {
        if (bitmap == null) {
            return null
        }
        val baos = ByteArrayOutputStream()
        bitmap.compress(format, quality, baos)
        return baos.toByteArray()
    }

    //byte数组转化Bitmap
    fun bytes2Bitmap(bytes: ByteArray?): Bitmap? {
        return if (bytes == null || bytes.isEmpty()) null
        else BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    //Bitmap转化Drawable
    fun bitmap2Drawable(bitmap: Bitmap?): Drawable? = bitmap?.let { BitmapDrawable(it) }

    //Drawable转化Bitmap
    fun drawable2Bitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        //取Drawable的长宽
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        //取Drawable的颜色格式
        val config = if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        //建立对应Bitmap
        val bitmap = Bitmap.createBitmap(width, height, config)
        //建立对应Bitmap的画布
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        //把Dawable内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }

    //Drawable转化Bytes
    fun drawable2Bytes(drawable: Drawable?): ByteArray? {
        return if (drawable == null) null else bitmap2Bytes(drawable2Bitmap(drawable))
    }

    //Bytes转化Drawable
    fun bytes2Drawable(bytes: ByteArray?): Drawable? {
        return if (bytes == null) null else bitmap2Drawable(bytes2Bitmap(bytes))
    }

    //view转Bitmap
    fun view2Bitmap(view: View?): Bitmap? {
        if (view == null) {
            return null
        }
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val drawable = view.background
        if (drawable != null) {
            drawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    /**
     * 保存Bitmap到文件中
     *
     * @param bitmap   保存的图片
     * @param filePath 保存的文件路径，如sdcard/AAAA/test.png
     * @param quality  图片压缩质量，取值范围0-100
     */
    fun saveBitmapToFile(
        bitmap: Bitmap,
        filePath: String,
        format: CompressFormat = CompressFormat.PNG,
        @IntRange(from = 0, to = 100) quality: Int = 100
    ): Boolean {
        if (isEmptyBitmap(bitmap)) {
            return false
        }
        var flag = false
        val dirPath = filePath.substring(0, filePath.lastIndexOf(File.separator))
        try {
            if (FileUtil.createOrExistsDirectory(dirPath)) {
                val bos = BufferedOutputStream(FileOutputStream(filePath))
                flag = bitmap.compress(format, quality, bos)
                bos.flush()
                bos.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return flag
    }

    //保存图片到系统相册，返回true表示保存成功，false表示保存失败
    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap?,
        bitmapName: String = System.currentTimeMillis().toString()  //存储的图片名称
    ): Boolean {
        if (bitmap == null) {
            return false
        }
        // 首先保存图片
        val appDir = File(Environment.getExternalStorageDirectory(), "extra_picture")  //图片存储路径
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val fileName = "${bitmapName}.jpg"
        val file = File(appDir, fileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            context.applicationContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            // 最后通知图库更新
            context.applicationContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            IOUtil.closeIO(fos)
        }
    }

    //保存图片文件到系统相册，返回true表示保存成功，false表示保存失败
    fun saveImageFileToGallery(
        context: Context,
        srcFile: File?,
        imageName: String = System.currentTimeMillis().toString()  //存储的图片名称
    ): Boolean {
        if (!FileUtil.isFile(srcFile)) {
            return false
        }
        // 首先保存图片
        val destDir = File(Environment.getExternalStorageDirectory(), "extra_picture") //图片存储路径
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        val mimeType: String = getMimeType(srcFile!!.absolutePath)
        val fileName = "$imageName.$mimeType"
        val destFile = File(destDir, fileName)
        var fis: FileInputStream? = null
        var os: OutputStream? = null
        try {
            fis = FileInputStream(srcFile)
            os = BufferedOutputStream(FileOutputStream(destFile, false))
            val data = ByteArray(1024)
            var len: Int
            while (fis.read(data, 0, 1024).also { len = it } != -1) {
                os.write(data, 0, len)
            }
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, destFile.absolutePath)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/$mimeType")
            context.applicationContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            //最后通知图库更新
            context.applicationContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            IOUtil.closeIO(fis, os)
        }
    }

    //通过BitmapFactory.decodeFile从文件中获取Bitmap
    fun getBitmapFromFile(filePath: String?): Bitmap? {
        if (!FileUtil.isFileExists(filePath)) {
            return null
        }
        return BitmapFactory.decodeFile(filePath)
    }

    //通过BitmapFactory.decodeFile从文件中获取指定宽高的Bitmap
    fun getBitmapFromFile(filePath: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (!FileUtil.isFileExists(filePath)) {
            return null
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }

    //通过BitmapFactory.decodeResource从资源文件中获取Bitmap
    fun getBitmapFromResource(res: Resources?, resId: Int): Bitmap? {
        if (res == null) {
            return null
        }
        return BitmapFactory.decodeResource(res, resId)
    }

    //通过BitmapFactory.decodeResource从资源文件中获取指定宽高的Bitmap
    fun getBitmapFromResource(res: Resources?, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (res == null) {
            return null
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    //通过BitmapFactory.decodeStream从文件中获取Bitmap
    fun getBitmapFromStream(filePath: String?): Bitmap? {
        if (!FileUtil.isFileExists(filePath)) {
            return null
        }
        var inputStream: InputStream? = null
        return try {
            inputStream = BufferedInputStream(FileInputStream(filePath))
            BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } finally {
            IOUtil.closeIO(inputStream)
        }
    }

    //通过BitmapFactory.decodeStream从文件中获取指定宽高的Bitmap
    fun getBitmapFromStream(filePath: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (!FileUtil.isFileExists(filePath)) {
            return null
        }
        var inputStream: InputStream? = null
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            inputStream = BufferedInputStream(FileInputStream(filePath))
            BitmapFactory.decodeStream(inputStream, null, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } finally {
            IOUtil.closeIO(inputStream)
        }
    }

    //通过BitmapFactory.decodeStream从输入流中获取Bitmap
    fun getBitmapFromStream(inputStream: InputStream?): Bitmap? {
        return if (inputStream == null) null else BitmapFactory.decodeStream(inputStream)
    }

    //通过BitmapFactory.decodeStream从输入流中获取指定宽高的Bitmap
    fun getBitmapFromStream(inputStream: InputStream?, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (inputStream == null) {
            return null
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeStream(inputStream, null, options)
    }

    //通过BitmapFactory.decodeByteArray从字节数组中获取Bitmap
    fun getBitmapFromByteArray(data: ByteArray?, offset: Int): Bitmap? {
        return if (data == null || data.isEmpty()) null
        else BitmapFactory.decodeByteArray(data, offset, data.size)
    }

    //通过BitmapFactory.decodeByteArray从字节数组中获取指定宽高的Bitmap
    fun getBitmapFromByteArray(data: ByteArray?, offset: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (data == null || data.isEmpty()) {
            return null
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, offset, data.size, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(data, offset, data.size, options)
    }

    /**
     * 缩放图片
     *
     * @param src       源图片
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @param recycle   是否回收
     * @return 缩放后的图片
     */
    fun scale(src: Bitmap?, newWidth: Int, newHeight: Int, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val ret = Bitmap.createScaledBitmap(src!!, newWidth, newHeight, true)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    /**
     * 缩放图片
     *
     * @param src         源图片
     * @param scaleWidth  缩放宽度倍数
     * @param scaleHeight 缩放高度倍数
     * @param recycle     是否回收
     * @return 缩放后的图片
     */
    fun scale(src: Bitmap?, scaleWidth: Float, scaleHeight: Float, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val matrix = Matrix()
        matrix.setScale(scaleWidth, scaleHeight)
        val ret = Bitmap.createBitmap(src!!, 0, 0, src.width, src.height, matrix, true)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    /**
     * 裁剪图片
     *
     * @param src     源图片
     * @param x       开始坐标x
     * @param y       开始坐标y
     * @param width   裁剪宽度
     * @param height  裁剪高度
     * @param recycle 是否回收
     * @return 裁剪后的图片
     */
    fun clip(src: Bitmap?, x: Int, y: Int, width: Int, height: Int, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val ret = Bitmap.createBitmap(src!!, x, y, width, height)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    /**
     * 倾斜图片
     *
     * @param src     源图片
     * @param kx      x轴方向上的倾斜比例
     * @param ky      y轴方向上的倾斜比例
     * @param px      倾斜的轴点的x坐标
     * @param py      倾斜的轴点的y坐标
     * @param recycle 是否回收
     * @return 倾斜后的图片
     */
    fun skew(src: Bitmap?, kx: Float, ky: Float, px: Float, py: Float, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val matrix = Matrix()
        matrix.setSkew(kx, ky, px, py)
        val ret = Bitmap.createBitmap(src!!, 0, 0, src.width, src.height, matrix, true)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    /**
     * 旋转图片
     *
     * @param src     源图片
     * @param degrees 旋转角度
     * @param px      旋转点横坐标
     * @param py      旋转点纵坐标
     * @param recycle 是否回收
     * @return 旋转后的图片
     */
    fun rotate(src: Bitmap?, degrees: Int, px: Float, py: Float, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        if (degrees == 0) {
            return src
        }
        val matrix = Matrix()
        matrix.setRotate(degrees.toFloat(), px, py)
        val ret = Bitmap.createBitmap(src!!, 0, 0, src.width, src.height, matrix, true)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    //获取图片旋转角度
    fun getRotateDegree(filePath: String?): Int {
        if (!FileUtil.isFileExists(filePath)) {
            return 0
        }
        var degree = 0
        try {
            val exifInterface = ExifInterface(filePath!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    //转为圆形图片
    fun toCircle(src: Bitmap?, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val width = src!!.width
        val height = src.height
        val radius = Math.min(width, height) / 2
        val ret = Bitmap.createBitmap(width, height, src.config)
        val paint = Paint()
        val canvas = Canvas(ret)
        val rect = Rect(0, 0, width, height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(width / 2f, height / 2f, radius.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, rect, rect, paint)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    //转为圆角图片
    fun toRoundCorner(src: Bitmap?, radius: Float, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val width = src!!.width
        val height = src.height
        val ret = Bitmap.createBitmap(width, height, src.config)
        val paint = Paint()
        val canvas = Canvas(ret)
        val rect = Rect(0, 0, width, height)
        paint.isAntiAlias = true
        canvas.drawRoundRect(RectF(rect), radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, rect, rect, paint)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    /**
     * 快速模糊图片，先缩小原图，对小图进行模糊，再放大回原先尺寸
     *
     * @param src     源图片
     * @param scale   缩放比例(0...1)
     * @param radius  模糊半径(0...25)
     * @param recycle 是否回收
     * @return 模糊后的图片
     */
    fun fastBlur(
        src: Bitmap?,
        @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) scale: Float,
        @FloatRange(from = 0.0, to = 25.0, fromInclusive = false) radius: Float,
        recycle: Boolean = true
    ): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val width = src!!.width
        val height = src.height
        val scaleWidth = (width * scale + 0.5f).toInt()
        val scaleHeight = (height * scale + 0.5f).toInt()
        if (scaleWidth == 0 || scaleHeight == 0) {
            return null
        }
        var scaleBitmap = Bitmap.createScaledBitmap(src, scaleWidth, scaleHeight, true)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        val canvas = Canvas()
        val filter = PorterDuffColorFilter(
            Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP
        )
        paint.colorFilter = filter
        canvas.scale(scale, scale)
        canvas.drawBitmap(scaleBitmap, 0f, 0f, paint)
        scaleBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            renderScriptBlur(scaleBitmap, radius)
        } else {
            stackBlur(scaleBitmap, radius.toInt(), recycle)
        }
        if (scale == 1f) return scaleBitmap
        val ret = Bitmap.createScaledBitmap(scaleBitmap, width, height, true)
        if (scaleBitmap != null && !scaleBitmap.isRecycled) scaleBitmap.recycle()
        if (recycle && !src.isRecycled) src.recycle()
        return ret
    }

    //renderScript模糊图片
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun renderScriptBlur(src: Bitmap?, @FloatRange(from = 0.0, to = 25.0, fromInclusive = false) radius: Float): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        var rs: RenderScript? = null
        try {
            rs = RenderScript.create(BaseApplication.instance)
            rs.messageHandler = RSMessageHandler()
            val input = Allocation.createFromBitmap(rs, src, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
            val output = Allocation.createTyped(rs, input.type)
            val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            blurScript.setInput(input)
            blurScript.setRadius(radius)
            blurScript.forEach(output)
            output.copyTo(src)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            rs?.destroy()
        }
        return src
    }

    //stack模糊图片
    fun stackBlur(src: Bitmap?, radius: Int, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val ret: Bitmap = if (recycle) {
            src!!
        } else {
            src!!.copy(src.config, true)
        }
        if (radius < 1) {
            return null
        }
        val w = ret.width
        val h = ret.height
        val pix = IntArray(w * h)
        ret.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {

                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++
            }
            x++
        }
        ret.setPixels(pix, 0, w, 0, 0, w, h)
        return ret
    }

    /**
     * 添加颜色边框
     *
     * @param src         源图片
     * @param borderWidth 边框宽度
     * @param color       边框的颜色值
     * @param recycle     是否回收
     * @return 带颜色边框图
     */
    fun addFrame(src: Bitmap?, borderWidth: Int, color: Int, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val doubleBorder = borderWidth shl 1
        val newWidth = src!!.width + doubleBorder
        val newHeight = src.height + doubleBorder
        val ret = Bitmap.createBitmap(newWidth, newHeight, src.config)
        val canvas = Canvas(ret)
        val rect = Rect(0, 0, newWidth, newHeight)
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.STROKE
        //setStrokeWidth是居中画的，所以要两倍的宽度才能画，否则有一半的宽度是空的
        paint.strokeWidth = doubleBorder.toFloat()
        canvas.drawRect(rect, paint)
        //noinspection SuspiciousNameCombination
        canvas.drawBitmap(src, borderWidth.toFloat(), borderWidth.toFloat(), null)
        if (recycle && !src.isRecycled) src.recycle()
        return ret
    }

    /**
     * 添加倒影
     *
     * @param src              源图片的
     * @param reflectionHeight 倒影高度
     * @param recycle          是否回收
     * @return 带倒影图片
     */
    fun addReflection(src: Bitmap?, reflectionHeight: Int, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        // 原图与倒影之间的间距
        val REFLECTION_GAP = 0
        val srcWidth = src!!.width
        val srcHeight = src.height
        val matrix = Matrix()
        matrix.preScale(1f, -1f)
        val reflectionBitmap = Bitmap.createBitmap(
            src, 0, srcHeight - reflectionHeight,
            srcWidth, reflectionHeight, matrix, false
        )
        val ret = Bitmap.createBitmap(srcWidth, srcHeight + reflectionHeight, src.config)
        val canvas = Canvas(ret)
        canvas.drawBitmap(src, 0f, 0f, null)
        canvas.drawBitmap(reflectionBitmap, 0f, srcHeight + REFLECTION_GAP.toFloat(), null)
        val paint = Paint()
        paint.isAntiAlias = true
        val shader = LinearGradient(
            0f, srcHeight.toFloat(),
            0f, ret.height + REFLECTION_GAP.toFloat(),
            0x70FFFFFF, 0x00FFFFFF, Shader.TileMode.MIRROR
        )
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(
            0f, srcHeight + REFLECTION_GAP.toFloat(),
            srcWidth.toFloat(), ret.height.toFloat(), paint
        )
        if (!reflectionBitmap.isRecycled) reflectionBitmap.recycle()
        if (recycle && !src.isRecycled) src.recycle()
        return ret
    }

    /**
     * 添加文字水印
     *
     * @param src      源图片
     * @param content  水印文本
     * @param textSize 水印字体大小
     * @param color    水印字体颜色
     * @param x        起始坐标x
     * @param y        起始坐标y
     * @param recycle  是否回收
     * @return 带有文字水印的图片
     */
    fun addTextWatermark(
        src: Bitmap?,
        content: String?,
        textSize: Float,
        color: Int,
        x: Float,
        y: Float,
        recycle: Boolean = true
    ): Bitmap? {
        if (isEmptyBitmap(src) || content.isNullOrBlank()) {
            return null
        }
        val ret = src!!.copy(src.config, true)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val canvas = Canvas(ret)
        paint.color = color
        paint.textSize = textSize
        val bounds = Rect()
        paint.getTextBounds(content, 0, content.length, bounds)
        canvas.drawText(content, x, y + textSize, paint)
        if (recycle && !src.isRecycled) src.recycle()
        return ret
    }

    /**
     * 添加图片水印
     *
     * @param src       源图片
     * @param watermark 图片水印
     * @param x         起始坐标x
     * @param y         起始坐标y
     * @param alpha     透明度，范围0到255
     * @param recycle   是否回收
     * @return 带有图片水印的图片
     */
    fun addImageWatermark(src: Bitmap?, watermark: Bitmap?, x: Int, y: Int, alpha: Int, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val ret = src!!.copy(src.config, true)
        if (!isEmptyBitmap(watermark)) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val canvas = Canvas(ret)
            paint.alpha = alpha
            canvas.drawBitmap(watermark!!, x.toFloat(), y.toFloat(), paint)
        }
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }

    //转为alpha位图
    fun toAlpha(src: Bitmap?, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val ret = src!!.extractAlpha()
        if (recycle && !src.isRecycled) src.recycle()
        return ret
    }

    //转为灰度图片
    fun toGray(src: Bitmap, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val grayBitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorMatrixColorFilter
        canvas.drawBitmap(src, 0f, 0f, paint)
        if (recycle && !src.isRecycled) src.recycle()
        return grayBitmap
    }

    //根据文件名判断文件是否为图片
    fun isImage(filePath: String?): Boolean {
        val path = filePath?.toUpperCase() ?: ""
        return (path.endsWith(".PNG") || path.endsWith(".JPG")
                || path.endsWith(".JPEG") || path.endsWith(".BMP")
                || path.endsWith(".GIF"))
    }

    //获取图片类型
    fun getMimeType(filePath: String?): String {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        var mimeType = options.outMimeType
        mimeType = if (TextUtils.isEmpty(mimeType)) "" else mimeType.substring(6)
        LogUtil.i("MimeType", mimeType)
        return mimeType
    }

    //获取图片类型
    fun getImageType(filePath: String?): String? = getImageType(FileUtil.getFileByPath(filePath))

    //获取图片类型
    fun getImageType(file: File?): String? {
        if (file == null) {
            return null
        }
        var inputStream: InputStream? = null
        return try {
            inputStream = FileInputStream(file)
            getImageType(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            IOUtil.closeIO(inputStream)
        }
    }

    //流获取图片类型
    fun getImageType(inputStream: InputStream?): String? {
        return if (inputStream == null) null
        else try {
            val bytes = ByteArray(8)
            if (inputStream.read(bytes, 0, 8) != -1) getImageType(bytes)
            else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取图片类型
     *
     * @param bytes bitmap的前8字节
     * @return 图片类型
     */
    fun getImageType(bytes: ByteArray?): String? {
        if (isJPEG(bytes)) return "JPEG"
        if (isGIF(bytes)) return "GIF"
        if (isPNG(bytes)) return "PNG"
        return if (isBMP(bytes)) "BMP" else null
    }

    private fun isJPEG(b: ByteArray?): Boolean {
        return b != null && b.size >= 2 && b[0] == 0xFF.toByte() && b[1] == 0xD8.toByte()
    }

    private fun isGIF(b: ByteArray?): Boolean {
        return b != null && b.size >= 6 &&
                b[0] == 'G'.toByte() && b[1] == 'I'.toByte() &&
                b[2] == 'F'.toByte() && b[3] == '8'.toByte() &&
                (b[4] == '7'.toByte() || b[4] == '9'.toByte()) && b[5] == 'a'.toByte()
    }

    private fun isPNG(b: ByteArray?): Boolean {
        return b != null && b.size >= 8
                && b[0] == 137.toByte() && b[1] == 80.toByte()
                && b[2] == 78.toByte() && b[3] == 71.toByte()
                && b[4] == 13.toByte() && b[5] == 10.toByte()
                && b[6] == 26.toByte() && b[7] == 10.toByte()
    }

    private fun isBMP(b: ByteArray?): Boolean {
        return b != null && b.size >= 2 && b[0] == 0x42.toByte() && b[1] == 0x4d.toByte()
    }

    /**
     * 按质量压缩
     *
     * @param src     源图片
     * @param limitK  允许最大kb
     * @param recycle 是否回收
     * @return 质量压缩压缩过的图片
     */
    fun compressByQuality(src: Bitmap?, limitK: Int, recycle: Boolean = true): ByteArray? {
        if (isEmptyBitmap(src) && limitK <= 0) {
            return null
        }
        val baos = ByteArrayOutputStream()
        src!!.compress(CompressFormat.JPEG, 100, baos) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 90
        while (baos.toByteArray().size / 1024 > limitK) {  //循环判断如果压缩后图片是否大于指定大小,大于继续压缩
            baos.reset() //重置baos即清空baos
            src.compress(CompressFormat.JPEG, options, baos) //这里压缩options%，把压缩后的数据存放到baos中
            options -= 10 //每次都减少10
        }
        val bytes = baos.toByteArray()
        if (recycle && !src.isRecycled) src.recycle()
        return bytes
    }

    /**
     * 按采样大小压缩
     *
     * @param src        源图片
     * @param sampleSize 采样率大小
     * @param recycle    是否回收
     * @return 按采样率压缩后的图片
     */
    fun compressBySampleSize(src: Bitmap, sampleSize: Int, recycle: Boolean = true): Bitmap? {
        if (isEmptyBitmap(src)) {
            return null
        }
        val options = BitmapFactory.Options()
        options.inSampleSize = sampleSize
        val baos = ByteArrayOutputStream()
        src.compress(CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        if (recycle && !src.isRecycled) src.recycle()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    //计算采样率
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (width > reqWidth || height > reqHeight) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            while (halfWidth / inSampleSize >= reqWidth &&
                halfHeight / inSampleSize >= reqHeight
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    //判断bitmap对象是否为空
    private fun isEmptyBitmap(bitmap: Bitmap?) = bitmap == null || bitmap.width == 0 || bitmap.height == 0

    //回收Bitmap
    fun recycleBitmap(vararg bitmaps: Bitmap?) {
        for (bm in bitmaps) {
            if (bm != null && !bm.isRecycled) {
                bm.recycle()
            }
        }
    }

}
package com.android.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.android.util.BitmapUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

/**
 * Created by xuzhb on 2020/8/23
 * Desc:磁盘缓存工具类
 */
class CacheUtil constructor(
    cacheDir: File,                     //缓存文件的存储路径
    maxSize: Long = 50 * 1024 * 1024L,  //缓存的总大小，默认50MB
    maxCount: Int = Int.MAX_VALUE       //缓存数据的最大数量，默认不限制存放数据的数量
) {

    constructor(
        context: Context,
        cacheName: String = "CacheUtil",    //缓存文件名称
        maxSize: Long = 50 * 1024 * 1024L,  //缓存的总大小，默认50MB
        maxCount: Int = Int.MAX_VALUE       //缓存数据的最大数量，默认不限制存放数据的数量
    ) : this(File(context.cacheDir, cacheName), maxSize, maxCount)

    private val mCacheManager: CacheManager

    init {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw RuntimeException("can't make dirs in ${cacheDir.absolutePath}")
        }
        mCacheManager = CacheManager(cacheDir, maxSize, maxCount)
    }

    //保存字符串数据到缓存中
    fun putString(key: String, value: String) {
        val file = mCacheManager.newFile(key)
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(FileWriter(file), 1024)
            out.write(value)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            mCacheManager.put(file)
        }
    }

    //保存字符串数据到缓存中，saveTime：保存的时间，以秒为单位
    fun putString(key: String, value: String, saveTime: Int) {
        putString(key, DataDueUtil.newStringWithTimeInfo(value, saveTime))
    }

    //读取字符串数据
    fun getString(key: String): String? {
        val file = mCacheManager.get(key)
        if (!file.exists()) {
            return null
        }
        var removeFile = false  //是否在读取后移除文件
        var reader: BufferedReader? = null
        return try {
            reader = BufferedReader(FileReader(file))
            val sb = StringBuilder()
            var currentLine: String?
            while (reader.readLine().also { currentLine = it } != null) {
                sb.append(currentLine)
            }
            if (!DataDueUtil.isDue(sb.toString())) {  //对于无时效性的数据和尚未过期的数据，isDue返回false
                DataDueUtil.clearTimeInfo(sb.toString())
            } else {  //数据已经过期
                removeFile = true
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (removeFile) {
                mCacheManager.remove(key) //删除已经过期的数据
            }
        }
    }

    //保存JSONObject数据到缓存中
    fun putJSONObject(key: String, value: JSONObject) {
        putString(key, value.toString())
    }

    //保存JSONObject数据到缓存中，saveTime：保存的时间，以秒为单位
    fun putJSONObject(key: String, value: JSONObject, saveTime: Int) {
        putString(key, value.toString(), saveTime)
    }

    //读取JSONObject数据
    fun getJSONObject(key: String): JSONObject? {
        try {
            return JSONObject(getString(key))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    //保存JSONArray数据到缓存中
    fun putJSONArray(key: String, value: JSONArray) {
        putString(key, value.toString())
    }

    //保存JSONArray数据到缓存中，saveTime：保存的时间，以秒为单位
    fun putJSONArray(key: String, value: JSONArray, saveTime: Int) {
        putString(key, value.toString(), saveTime)
    }

    //读取JSONArray数据
    fun getJSONArray(key: String): JSONArray? {
        try {
            return JSONArray(getString(key))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    //保存byte数组到缓存中
    fun putBytes(key: String, value: ByteArray) {
        val file = mCacheManager.newFile(key)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            out.write(value)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            mCacheManager.put(file)
        }
    }

    //保存byte数组到缓存中，saveTime：保存的时间，以秒为单位
    fun putBytes(key: String, value: ByteArray, saveTime: Int) {
        putBytes(key, DataDueUtil.newBytesWithTimeInfo(value, saveTime))
    }

    //获取byte数组
    fun getBytes(key: String): ByteArray? {
        var randomAccessFile: RandomAccessFile? = null
        var removeFile = false //是否在读取后移除文件
        return try {
            val file = mCacheManager.get(key)
            if (!file.exists()) {
                return null
            }
            randomAccessFile = RandomAccessFile(file, "r")
            val bytes = ByteArray(randomAccessFile.length().toInt())
            randomAccessFile.read(bytes)
            if (!DataDueUtil.isDue(bytes)) {  //对于无时效性的数据和尚未过期的数据，isDue返回false
                DataDueUtil.clearTimeInfo(bytes)
            } else {  //数据已经过期
                removeFile = true
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (removeFile) {
                mCacheManager.remove(key) //删除已经过期的数据
            }
        }
    }

    //保存Serializable数据到缓存中
    fun putObject(key: String, value: Serializable) {
        putObject(key, value, -1)
    }

    //保存Serializable数据到缓存中，saveTime：保存的时间，以秒为单位
    fun putObject(key: String, value: Any, saveTime: Int) {
        var baos: ByteArrayOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            oos = ObjectOutputStream(baos)
            oos.writeObject(value)
            val data = baos.toByteArray()
            if (saveTime != -1) {  //保存有时效性的数据
                putBytes(key, data, saveTime)
            } else {
                putBytes(key, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                oos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //读取Serializable数据
    fun getObject(key: String): Any? {
        val data = getBytes(key)
        if (data != null) {
            var bais: ByteArrayInputStream? = null
            var ois: ObjectInputStream? = null
            return try {
                bais = ByteArrayInputStream(data)
                ois = ObjectInputStream(bais)
                ois.readObject()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                try {
                    bais?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    ois?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    //保存Bitmap到缓存中
    fun putBitmap(key: String, value: Bitmap) {
        BitmapUtil.bitmap2Bytes(value)?.let {
            putBytes(key, it)
        }
    }

    //保存Bitmap到缓存中，saveTime：保存的时间，以秒为单位
    fun putBitmap(key: String, value: Bitmap, saveTime: Int) {
        BitmapUtil.bitmap2Bytes(value)?.let {
            putBytes(key, it, saveTime)
        }
    }

    //读取Bitmap数据
    fun getBitmap(key: String): Bitmap? {
        return BitmapUtil.bytes2Bitmap(getBytes(key))
    }

    //保存Drawable到缓存中
    fun putDrawable(key: String, value: Drawable) {
        BitmapUtil.drawable2Bitmap(value)?.let {
            putBitmap(key, it)
        }
    }

    //保存Drawable到缓存中，saveTime：保存的时间，以秒为单位
    fun putDrawable(key: String, value: Drawable, saveTime: Int) {
        BitmapUtil.drawable2Bitmap(value)?.let {
            putBitmap(key, it, saveTime)
        }
    }

    //读取Drawable数据
    fun getDrawable(key: String): Drawable? {
        return BitmapUtil.bitmap2Drawable(BitmapUtil.bytes2Bitmap(getBytes(key)))
    }

    //读取缓存文件
    fun getFile(key: String): File? {
        val file = mCacheManager.newFile(key)
        return if (file.exists()) file else null
    }

    //移除指定key对应的缓存内容
    fun remove(key: String): Boolean {
        return mCacheManager.remove(key)
    }

    //清除所有缓存内容
    fun delete() {
        mCacheManager.delete()
    }

    private class CacheManager constructor(
        private val mCacheDir: File,
        private val mSizeLimit: Long,
        private val mCountLimit: Int
    ) {
        private val mCacheSize: AtomicLong = AtomicLong()
        private val mCacheCount: AtomicInteger = AtomicInteger()
        private val mLastUsageDates = Collections.synchronizedMap(HashMap<File, Long>())

        init {
            calculateCacheSizeAndCacheCount()
        }

        //计算cacheSize和cacheCount
        private fun calculateCacheSizeAndCacheCount() {
            thread(start = true) {
                var size = 0
                var count = 0
                val cachedFiles = mCacheDir.listFiles()
                if (cachedFiles != null) {
                    for (cachedFile in cachedFiles) {
                        size += cachedFile.length().toInt()
                        count += 1
                        mLastUsageDates[cachedFile] = cachedFile.lastModified()
                    }
                    mCacheSize.set(size.toLong())
                    mCacheCount.set(count)
                }
            }
        }

        fun put(file: File) {
            var curCacheCount = mCacheCount.get()
            while (curCacheCount + 1 > mCountLimit) {
                val freedSize = removeNext()
                mCacheSize.addAndGet(-freedSize)
                curCacheCount = mCacheCount.addAndGet(-1)
            }
            mCacheCount.addAndGet(1)

            val valueSize = file.length()
            var curCacheSize = mCacheSize.get()
            while (curCacheSize + valueSize > mSizeLimit) {
                val freedSize = removeNext()
                curCacheSize = mCacheSize.addAndGet(-freedSize)
            }
            mCacheSize.addAndGet(valueSize)

            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            mLastUsageDates[file] = currentTime
        }

        fun get(key: String): File {
            val file = newFile(key)
            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            mLastUsageDates[file] = currentTime
            return file
        }

        fun newFile(key: String): File {
            return File(mCacheDir, key.hashCode().toString() + "")
        }

        fun remove(key: String): Boolean {
            val file = get(key)
            return file.delete()
        }

        fun delete() {
            mLastUsageDates.clear()
            mCacheSize.set(0)
            val files = mCacheDir.listFiles()
            if (files != null) {
                for (f in files) {
                    f.delete()
                }
            }
        }

        //移除旧的文件
        private fun removeNext(): Long {
            if (mLastUsageDates.isEmpty()) {
                return 0
            }
            var oldestUsage: Long? = null
            var mostLongUsedFile: File? = null
            val entries: Set<Map.Entry<File, Long>> = mLastUsageDates.entries
            synchronized(mLastUsageDates) {
                for ((key, lastValueUsage) in entries) {
                    if (mostLongUsedFile == null) {
                        mostLongUsedFile = key
                        oldestUsage = lastValueUsage
                    } else {
                        if (lastValueUsage < oldestUsage!!) {
                            oldestUsage = lastValueUsage
                            mostLongUsedFile = key
                        }
                    }
                }
            }
            val fileSize = mostLongUsedFile!!.length()
            if (mostLongUsedFile!!.delete()) {
                mLastUsageDates.remove(mostLongUsedFile)
            }
            return fileSize
        }

    }

}
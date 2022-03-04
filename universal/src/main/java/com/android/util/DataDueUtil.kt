package com.android.util

import kotlin.math.min

/**
 * Created by xuzhb on 2020/8/23
 * Desc:数据缓存是否过期
 */
object DataDueUtil {

    private const val SEPARATOR = ' '

    //判断缓存的字符串数据是否到期，true：到期，false：还处于有效期内
    fun isDue(content: String): Boolean {
        return isDue(content.toByteArray())
    }

    //判断缓存的byte数据是否到期，true：到期，false：还处于有效期内
    fun isDue(data: ByteArray): Boolean {
        val strs = getTimeInfoFromData(data)
        if (strs != null && strs.size == 2) {
            var saveTimeStr = strs[0]
            while (saveTimeStr.startsWith("0")) {
                saveTimeStr = saveTimeStr.substring(1)
            }
            val saveTime = saveTimeStr.toLong()
            val saveDuration = strs[1].toLong()
            if (System.currentTimeMillis() > saveTime + saveDuration * 1000) {
                return true
            }
        }
        return false
    }

    /**
     * 在byte数组开头加上缓存时间标志，使数据具有时效性
     *
     * @param content 缓存的数据
     * @param second  缓存的时长
     */
    fun newStringWithTimeInfo(content: String, second: Int): String {
        return createTimeInfo(second) + content
    }

    /**
     * 在byte数组开头加上缓存时间标志，使数据具有时效性
     *
     * @param data   缓存的数据
     * @param second 缓存的时长
     */
    fun newBytesWithTimeInfo(data: ByteArray, second: Int): ByteArray {
        val timeInfo = createTimeInfo(second).toByteArray()
        val result = ByteArray(timeInfo.size + data.size)
        System.arraycopy(timeInfo, 0, result, 0, timeInfo.size)
        System.arraycopy(data, 0, result, timeInfo.size, data.size)
        return result
    }

    //创建缓存时间标志，格式"System.currentTimeMillis()(13位)-缓存时长"，如"1584629349290-120 "，表示缓存120秒
    private fun createTimeInfo(second: Int): String {
        val currentTime = StringBuilder(System.currentTimeMillis().toString() + "")
        while (currentTime.length < 13) {
            currentTime.insert(0, "0")
        }
        return "$currentTime-$second$SEPARATOR"
    }

    //清除缓存时间标志并获取值
    fun clearTimeInfo(content: String?): String? {
        var content = content
        if (content != null && hasTimeInfo(content.toByteArray())) {
            content = content.substring(content.indexOf(SEPARATOR) + 1)
        }
        return content
    }

    //清除缓存时间标志并获取值
    fun clearTimeInfo(data: ByteArray): ByteArray {
        return if (hasTimeInfo(data)) {
            copyOfRange(data, indexOf(data, SEPARATOR) + 1, data.size)
        } else data
    }

    //获取缓存时间标志，返回长度为2的字符串数组，分别是数据缓存的开始时间和缓存的时长
    private fun getTimeInfoFromData(data: ByteArray): Array<String>? {
        if (hasTimeInfo(data)) {
            val saveTime = String(copyOfRange(data, 0, 13))
            val saveDuration = String(copyOfRange(data, 14, indexOf(data, SEPARATOR)))
            return arrayOf(saveTime, saveDuration)
        }
        return null
    }

    /*
     * byte数组中是否有缓存时间标志
     * 对于具有时效性的数据，会在对应的byte数组开头加上缓存的开始时间和缓存时长
     * 即"System.currentTimeMillis()(13位)-缓存时长 缓存数据"，如"1584629349290-120 xxxxx"，表示缓存120秒
     */
    private fun hasTimeInfo(data: ByteArray?): Boolean {
        return data != null && data.size > 15 && data[13].toChar() == '-' && indexOf(data, SEPARATOR) > 14
    }

    private fun indexOf(data: ByteArray, c: Char): Int {
        for (i in data.indices) {
            if (data[i].toChar() == c) {
                return i
            }
        }
        return -1
    }

    //复制byte数组中指定位置的数据，不包括to下标对应的数据
    private fun copyOfRange(original: ByteArray, from: Int, to: Int): ByteArray {
        val newLength = to - from
        require(newLength >= 0) { "$from > $to" }
        val copy = ByteArray(newLength)
        System.arraycopy(original, from, copy, 0, min(original.size - from, newLength))
        return copy
    }

}
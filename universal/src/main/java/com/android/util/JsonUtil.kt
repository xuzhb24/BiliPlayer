package com.android.util

import com.google.gson.Gson
import java.util.*

/**
 * Created by xuzhb on 2019/10/27
 * Desc:Json工具类
 */
object JsonUtil {

    //格式化Json字符串
    fun formatJson(strJson: String): String {
        // 计数tab的个数
        var tabNum = 0
        val jsonFormat = StringBuilder()
        val length = strJson.length

        var last: Char = 0.toChar()
        for (i in 0 until length) {
            val c = strJson[i]
            if (c == '{') {
                tabNum++
                jsonFormat.append(c + "\n")
                jsonFormat.append(getSpaceOrTab(tabNum))
            } else if (c == '}') {
                tabNum--
                jsonFormat.append("\n")
                jsonFormat.append(getSpaceOrTab(tabNum))
                jsonFormat.append(c)
            } else if (c == ',') {
                jsonFormat.append(c + "\n")
                jsonFormat.append(getSpaceOrTab(tabNum))
            } else if (c == ':') {
                jsonFormat.append("$c ")
            } else if (c == '[') {
                tabNum++
                val next = strJson[i + 1]
                if (next == ']') {
                    jsonFormat.append(c)
                } else {
                    jsonFormat.append(c + "\n")
                    jsonFormat.append(getSpaceOrTab(tabNum))
                }
            } else if (c == ']') {
                tabNum--
                if (last == '[') {
                    jsonFormat.append(c)
                } else {
                    jsonFormat.append("\n" + getSpaceOrTab(tabNum) + c)
                }
            } else {
                jsonFormat.append(c)
            }
            last = c
        }
        return jsonFormat.toString()
    }

    private fun getSpaceOrTab(tabNum: Int): String {
        val sbTab = StringBuilder()
        for (i in 0 until tabNum) {
            sbTab.append('\t')
        }
        return sbTab.toString()
    }

    //Json字符串转换为ArrayList对象
    //使用：json2List(XXXString,Array<XXXBean>::class.java)
    fun <T> json2List(jsonString: String, cls: Class<Array<T>>): ArrayList<T> {
        val list = ArrayList<T>()
        try {
            val gson = Gson()
            val array = gson.fromJson(jsonString, cls)
            list.addAll(Arrays.asList(*array))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

}
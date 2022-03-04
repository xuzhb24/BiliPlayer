package com.android.http.interceptor

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Created by xuzhb on 2019/9/29
 * Desc:将服务器请求报文和响应报文Json格式化
 */
class HttpLogInterceptor : HttpLoggingInterceptor.Logger {

    companion object {
        const val TAG = "HttpLogger"
    }

    private val mMessage = StringBuilder()

    override fun log(message: String) {
        var message = message
        // 请求或者响应开始
        if (message.startsWith("--> POST")) {
            mMessage.setLength(0)
        }
        // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
        if (message.startsWith("{") && message.endsWith("}")
            || message.startsWith("[") && message.endsWith("]")
        ) {
            message = formatJson(message)
        }
        mMessage.append(message)
        mMessage.append("\n")
        // 响应结束，打印整条日志
        if (message.startsWith("<-- END HTTP")) {
            val spacingFlag =
                "=============================================================================================\n"
            val content = " \n$spacingFlag$mMessage$spacingFlag"
            printLog(TAG, content)
        }
    }

    private fun printLog(tag: String, msg: String) {  //信息太长,分段打印
        var msg = msg
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //把4*1024的MAX字节打印长度改为2001字符数
        val max_str_length = 2001 - tag.length
        //大于4000时
        while (msg.length > max_str_length) {
            Log.i(tag, msg.substring(0, max_str_length))
            msg = msg.substring(max_str_length)
        }
        //剩余部分
        Log.i(tag, msg)
    }

    //格式化Json字符串
    private fun formatJson(strJson: String): String {
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

}
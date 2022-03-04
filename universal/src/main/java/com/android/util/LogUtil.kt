package com.android.util

import android.util.Log
import com.android.http.Config
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by xuzhb on 2019/10/20
 * Desc:日志工具
 */
object LogUtil {

    private val Verbose = 1
    private val Debug = 2
    private val Info = 3
    private val Warn = 4
    private val Error = 5

    //日志文件总开关
    private val mLogSwitch = Config.DEBUG

    //输出日志级别，如Verbose代表输出Verbose及以上级别的日志信息，Warn代表输出Warn及以上级别的日志信息
    private val mLogType = Verbose

    //日志写入文件开关
    private val mLogToFile = false

    //日志文件存放路径
    private val mLogPath = "/sdcard/log"

    //日志文件名称
    private val mLogFileName = "Log.txt"

    private val threadLocal1 = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd")
        }
    }
    private val threadLocal2 = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        }
    }

    fun e(tag: String, e: Throwable) {
        val writer = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        log(tag, writer.toString(), Error)
    }

    fun e(tag: String, e: Exception) {
        val writer = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        log(tag, writer.toString(), Error)
    }

    fun e(tag: String, text: String) {
        log(tag, text, Error)
    }

    fun w(tag: String, text: String) {
        log(tag, text, Warn)
    }

    fun i(tag: String, text: String) {
        log(tag, text, Info)
    }

    fun d(tag: String, text: String) {
        log(tag, text, Debug)
    }

    fun v(tag: String, text: String) {
        log(tag, text, Verbose)
    }

    //根据tag, msg和等级，输出日志
    private fun log(tag: String, msg: String, level: Int) {
        if (mLogSwitch) {
            if (level == Verbose && level >= mLogType) {
                Log.v(tag, msg)
            } else if (level == Debug && level >= mLogType) {
                Log.d(tag, msg)
            } else if (level == Info && level >= mLogType) {
                Log.i(tag, msg)
            } else if (level == Warn && level >= mLogType) {
                Log.w(tag, msg)
            } else if (level == Error && level >= mLogType) {
                Log.e(tag, msg)
            }
        }
        if (mLogToFile) { //写入文件
            writeLogToFile(level, tag, msg)
        }
    }

    //打开日志文件并写入日志
    private fun writeLogToFile(level: Int, tag: String, text: String) {
        var logType = "i"
        if (level == Verbose) {
            logType = "V"
        } else if (level == Debug) {
            logType = "D"
        } else if (level == Info) {
            logType = "I"
        } else if (level == Warn) {
            logType = "W"
        } else {
            logType = "E"
        }
        val currDate = Date()
        val logFileNamePrefix = threadLocal1.get()!!.format(currDate)
        val logMessage = threadLocal2.get()!!.format(currDate) + " " + logType + "/" + tag + ": " + text
        val filePath = File(mLogPath)
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        val fullLogName = logFileNamePrefix + mLogFileName
        val logFileName = File(filePath, fullLogName)
        var writer: PrintWriter? = null
        try {
            writer = PrintWriter(BufferedWriter(FileWriter(logFileName, true)))
            writer.println(logMessage)
            writer.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            writer?.close()
        }
    }

    //删除除当天外的所有日志文件
    fun deleteFile() {  // 删除日志文件
        val currFileName = threadLocal1.get()!!.format(Date()) + mLogFileName
        println(currFileName)
        val filePath = File(mLogPath)
        if (!filePath.exists()) {
            return
        }
        val fileLists = filePath.listFiles()
        for (f in fileLists) {
            if (f.name != currFileName) {
                f.delete()
            }
        }
    }

    //分段打印超长日志
    fun logLongTag(tag: String, msg: String) {
        var msg = msg
        val max_str_length = 2001 - tag.length
        //大于4000时
        while (msg.length > max_str_length) {
            Log.i(tag, msg.substring(0, max_str_length))
            msg = msg.substring(max_str_length)
        }
        //剩余部分
        Log.i(tag, msg)
    }

}
package com.android.util

import android.text.TextUtils
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Created by xuzhb on 2020/11/19
 * Desc:Shell相关工具类，执行shell或root命令
 */
object ShellUtil {

    //获取操作系统对应的换行符，如java中的\r\n，windows中的\r\n，linux/unix中的\r，mac中的\n
    private val LINE_SEPARATOR = System.getProperty("line.separator")

    //执行shell命令
    fun execShellCmd(command: String, isNeedResult: Boolean = true): CommandResult =
        execCmd(arrayOf(command), false, isNeedResult)

    //执行root命令
    fun execRootCmd(command: String, isNeedResult: Boolean = true): CommandResult =
        execCmd(arrayOf(command), true, isNeedResult)

    fun execCmd(command: String, isRoot: Boolean, isNeedResult: Boolean = true): CommandResult =
        execCmd(arrayOf(command), isRoot, isNeedResult)

    fun execCmd(commands: List<String>?, isRoot: Boolean, isNeedResult: Boolean = true): CommandResult {
        if (commands.isNullOrEmpty()) {
            return CommandResult(-1, null, null)
        }
        val commandList: ArrayList<String> = arrayListOf()
        for (commend in commands) {
            commandList.add(commend)
        }
        return execCmd(commandList.toArray<String>(arrayOf<String>()), isRoot, isNeedResult)
    }


    /**
     * 执行Shell命令
     *
     * @param commands     命令数组
     * @param isRoot       是否需要root权限执行
     * @param isNeedResult 是否需要结果消息
     */
    fun execCmd(commands: Array<String>?, isRoot: Boolean, isNeedResult: Boolean = true): CommandResult {
        var code = -1
        if (commands.isNullOrEmpty()) {
            return CommandResult(code, null, null)
        }
        var process: Process? = null
        var successReader: BufferedReader? = null
        var failureReader: BufferedReader? = null
        var successMsg: StringBuilder? = null
        var failureMsg: StringBuilder? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec(if (isRoot) "su" else "sh")
            os = DataOutputStream(process.outputStream)
            for (command in commands) {
                if (TextUtils.isEmpty(command)) {
                    continue
                }
                os.write(command.toByteArray())
                os.writeBytes("\n")
                os.flush()
            }
            os.writeBytes("exit\n")
            os.flush()
            code = process.waitFor()
            if (isNeedResult) {
                successMsg = StringBuilder()
                failureMsg = StringBuilder()
                successReader = BufferedReader(InputStreamReader(process.inputStream, "UTF-8"))
                failureReader = BufferedReader(InputStreamReader(process.errorStream, "UTF-8"))
                var s: String?
                while (successReader.readLine().also { s = it } != null) {
                    successMsg.append(s).append(LINE_SEPARATOR)
                }
                while (failureReader.readLine().also { s = it } != null) {
                    failureMsg.append(s).append(LINE_SEPARATOR)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                process?.destroy()
                successReader?.close()
                failureReader?.close()
                os?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return CommandResult(code, successMsg?.toString(), failureMsg?.toString())
    }

    data class CommandResult(
        val code: Int,           //结果码，返回值说明见https://blog.csdn.net/qq_35661171/article/details/79096786
        val successMsg: String?,  //成功信息
        val failureMsg: String?   //失败信息
    ) {
        fun isSuccess() = code == 0
    }

}
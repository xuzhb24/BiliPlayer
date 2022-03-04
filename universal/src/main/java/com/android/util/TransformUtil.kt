package com.android.util

/**
 * Created by xuzhb on 2019/10/17
 * Desc:转换工具
 */
object TransformUtil {

    //byte转16进制
    fun byte2Hex(b: Byte): String {
        var hex = Integer.toHexString(b.toInt() and 0xFF)
        if (hex.length < 2) {
            hex = "0$hex"
        }
        return hex
    }

    //16进制转byte
    fun hex2Byte(hex: String): Byte = Integer.parseInt(hex, 16).toByte()

    //byte数组转16进制
    fun bytes2Hex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (i in bytes.indices) {
            val hex = Integer.toHexString(bytes[i].toInt() and 0xFF)
            if (hex.length < 2) {
                sb.append(0)
            }
            sb.append(hex)
        }
        return sb.toString()
    }

    //16进制转byte数组
    fun hex2Bytes(hex: String): ByteArray {
        var hex = hex
        var hexlen = hex.length
        val result: ByteArray
        if (hexlen % 2 == 1) {  //奇数
            hexlen++
            result = ByteArray(hexlen / 2)
            hex = "0$hex"
        } else {  //偶数
            result = ByteArray(hexlen / 2)
        }
        var j = 0
        var i = 0
        while (i < hexlen) {
            result[j] = hex2Byte(hex.substring(i, i + 2))
            j++
            i += 2
        }
        return result
    }

    @JvmStatic
    fun main(args: Array<String>) {
        logTitlt("16进制和byte数组相互转换")
        val data = "HIJKLMN"
        val hex = bytes2Hex(data.toByteArray(charset("UTF-8")))
        println(hex)
        println(String(hex2Bytes(hex), Charsets.UTF_8))
    }

    private fun logTitlt(title: String) {
        println("===============$title===============")
    }

}
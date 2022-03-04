package com.android.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * Created by xuzhb on 2019/10/16
 * Desc:一些浮点数格式化的工具类
 */
object DigitalUtil {

    //保留2位小数，四舍五入
    fun formatNumberByRounding1(number: Double, length: Int = 2): String = String.format("%.${length}f", number)

    //保留2位小数，四舍五入
    fun formatNumberByRounding2(number: Double): String = DecimalFormat("#0.00").format(number)

    //保留2位小数，四舍五入
    fun formatNumberByRounding3(number: Double, length: Int = 2): String =
        BigDecimal(number.toString()).setScale(length, BigDecimal.ROUND_HALF_UP).toString()

    //保留2位小数，五舍六入，舍弃的部分如果大于5才进位，小于或等于5直接舍弃
    fun formatNumberByRounding4(number: Double, length: Int = 2): String =
        BigDecimal(number.toString()).setScale(length, BigDecimal.ROUND_HALF_DOWN).toString()

    //保留2位小数，舍弃的部分非零，则直接进位；舍弃的部分为零，不进位
    fun formatNumberByRounding5(number: Double, length: Int = 2): String =
        BigDecimal(number.toString()).setScale(length, BigDecimal.ROUND_UP).toString()

    //保留2位小数，直接截取
    fun formatNumberNoRounding(number: Double, length: Int = 2): String =
        BigDecimal(number.toString()).setScale(length, BigDecimal.ROUND_DOWN).toString()

    //去除小数点尾部的0
    fun trimEndZero(numberStr: String): String {
        var number = numberStr
        if (number.indexOf(".") > 0) {
            number = number.replace("0+?$".toRegex(), "")  //去掉后面无用的零
            number = number.replace("[.]$".toRegex(), "")  //若小数点后面全是零则去掉小数点
        }
        return number
    }

    //去除小数点尾部的0
    fun trimEndZero(number: Double): String {
        val format = NumberFormat.getInstance()
        format.isGroupingUsed = false  //不显示千分位分隔符
        return format.format(number)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        test("四舍五入(String.format)：", 1, 1.23499, 1.23999, 0.0, 1.1)
        test("四舍五入(DecimalFormat)：", 2, 1.23499, 1.23999, 0.0, 1.1)
        test("四舍五入(BigDecimal)：", 3, 1.23499, 1.23999, 0.0, 1.1)
        test("五舍六入(BigDecimal)：", 4, 1.235, 1.23500001, 1.236)
        test("非零进位(BigDecimal)：", 5, 1.231, 1.23000, 0.0, 1.000001)
        test("直接截取(BigDecimal)：", 6, 0.0, 0.1, 1.0912344, 1.23999999999)
        test("去除尾部的零(正则表达式)：", 7, 1000.0, 2.1, 1.00, 1.2345, 0.0)
        test("去除尾部的零(NumberFormat)：", 8, 1000.0, 2.1, 1.00, 1.2345, 0.0)
    }

    private fun test(tag: String, type: Int, vararg numbers: Double) {
        print(tag + "\t")
        for (num in numbers) {
            when (type) {
                1 -> print(formatNumberByRounding1(num) + "\t")
                2 -> print(formatNumberByRounding2(num) + "\t")
                3 -> print(formatNumberByRounding3(num) + "\t")
                4 -> print(formatNumberByRounding4(num) + "\t")
                5 -> print(formatNumberByRounding5(num) + "\t")
                6 -> print(formatNumberNoRounding(num) + "\t")
                7 -> print(trimEndZero(formatNumberByRounding1(num)) + "\t")
                8 -> print(trimEndZero(num) + "\t")
            }
        }
        println()
    }

}
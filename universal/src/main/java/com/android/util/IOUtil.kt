package com.android.util

import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/**
 * Created by xuzhb on 2020/2/20
 * Desc:IO流相关工具类
 */
object IOUtil {

    //获取操作系统对应的换行符，如java中的\r\n，windows中的\r\n，linux/unix中的\r，mac中的\n
    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    //将输入流写入文件
    fun writeFileFromInputStream(filePath: String, inputStream: InputStream?, append: Boolean): Boolean =
        writeFileFromInputStream(FileUtil.getFileByPath(filePath), inputStream, append)

    /**
     * 将输入流写入文件
     *
     * @param file   文件
     * @param is     输入流
     * @param append 是否追加在文件末
     * @return true：写入成功，false：写入失败
     */
    fun writeFileFromInputStream(file: File?, inputStream: InputStream?, append: Boolean): Boolean {
        if (!FileUtil.createOrExistsFile(file) || inputStream == null) {
            return false
        }
        var os: OutputStream? = null
        try {
            os = BufferedOutputStream(FileOutputStream(file, append))
            val data = ByteArray(1024)
            var len: Int = 0
            while (inputStream.read(data, 0, 1024).also { len = it } != -1) {
                os.write(data, 0, len)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            closeIO(os)
        }
    }

    //将字符串写入文件
    fun writeFileFromString(filePath: String, content: String, append: Boolean) =
        writeFileFromString(FileUtil.getFileByPath(filePath), content, append)

    /**
     * 将字符串写入文件
     *
     * @param file    文件
     * @param content 字符串内容
     * @param append  是否追加在文件末
     * @return true：写入成功，false：写入失败
     */
    fun writeFileFromString(file: File?, content: String, append: Boolean): Boolean {
        if (!FileUtil.createOrExistsFile(file) || StringUtil.isEmpty(content)) {
            return false
        }
        var bw: BufferedWriter? = null
        try {
            bw = BufferedWriter(FileWriter(file, append))
            bw.write(content)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            closeIO(bw)
        }
    }

    //读取文件到字符串中
    fun readFileToString(filePath: String?) = readFileToString(FileUtil.getFileByPath(filePath), null)

    //读取文件到字符串中
    fun readFileToString(filePath: String?, charsetName: String?) =
        readFileToString(FileUtil.getFileByPath(filePath), charsetName)

    //读取文件到字符串中
    fun readFileToString(file: File?) = readFileToString(file, null)

    /**
     * 读取文件到字符串中
     *
     * @param file        文件
     * @param charsetName 编码格式
     * @return 字符串
     */
    fun readFileToString(file: File?, charsetName: String?): String? {
        if (!FileUtil.isFileExists(file)) {
            return null
        }
        var reader: BufferedReader? = null
        try {
            val sb = StringBuilder()
            reader = if (StringUtil.isEmpty(charsetName)) {
                BufferedReader(InputStreamReader(FileInputStream(file)))
            } else {
                BufferedReader(InputStreamReader(FileInputStream(file), charsetName))
            }
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append(LINE_SEPARATOR)
            }
            //删除最后的换行符
            return sb.delete(sb.length - LINE_SEPARATOR.length, sb.length).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            closeIO(reader)
        }
    }

    //读取InputStream到字符串中
    fun readInputStreameToString(inputStream: InputStream) = readInputStreameToString(inputStream, "")

    /**
     * 读取InputStream到字符串中
     *
     * @param inputStream 输出流
     * @param charsetName 编码格式
     * @return 字符串
     */
    fun readInputStreameToString(inputStream: InputStream, charsetName: String): String? {
        var reader: BufferedReader? = null
        try {
            val sb = StringBuilder()
            reader = if (StringUtil.isEmpty(charsetName)) {
                BufferedReader(InputStreamReader(inputStream))
            } else {
                BufferedReader(InputStreamReader(inputStream, charsetName))
            }
            do {
                val line = reader.readLine()
                if (line != null) {
                    sb.append(line).append(LINE_SEPARATOR)
                } else {
                    break
                }
            } while (true)
            //删除最后的换行符
            return sb.delete(sb.length - LINE_SEPARATOR.length, sb.length).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            closeIO(reader)
        }
    }

    //将字节数组写入文件
    fun writeFileFromBytes(filePath: String?, bytes: ByteArray?, append: Boolean) =
        writeFileFromBytes(FileUtil.getFileByPath(filePath), bytes, append)

    /**
     * 将字节数组写入文件
     *
     * @param file   文件
     * @param bytes  字节数组
     * @param append 是否追加在文件末
     * @return true：写入成功，false：写入失败
     */
    fun writeFileFromBytes(file: File?, bytes: ByteArray?, append: Boolean): Boolean {
        if (!FileUtil.createOrExistsFile(file) || bytes == null) {
            return false
        }
        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(FileOutputStream(file, append))
            bos.write(bytes)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            closeIO(bos)
        }
    }

    //读取文件到字节数组中
    fun readFileToBytes(filePath: String?) = readFileToBytes(FileUtil.getFileByPath(filePath))

    //读取文件到字节数组中
    fun readFileToBytes(file: File?): ByteArray? {
        if (FileUtil.isFileExists(file)) {
            return null
        }
        var fis: FileInputStream? = null
        var os: ByteArrayOutputStream? = null
        try {
            fis = FileInputStream(file)
            os = ByteArrayOutputStream()
            val b = ByteArray(1024)
            var len: Int
            while (fis.read(b, 0, 1024).also { len = it } != -1) {
                os.write(b, 0, len)
            }
            return os.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            closeIO(fis, os)
        }
    }

    //将字节数组写入文件，使用FileChannel
    fun writeFileFromBytesByFileChannel(filePath: String?, bytes: ByteArray?, isForce: Boolean) =
        writeFileFromBytesByFileChannel(FileUtil.getFileByPath(filePath), bytes, isForce)

    /**
     * 将字节数组写入文件，使用FileChannel
     *
     * @param file    文件
     * @param bytes   字节数组
     * @param isForce 是否立即写入磁盘
     * @return true：写入成功，false：写入失败
     */
    fun writeFileFromBytesByFileChannel(file: File?, bytes: ByteArray?, isForce: Boolean): Boolean {
        if (!FileUtil.createFileAfterDeleteOldFile(file) || bytes == null) {
            return false
        }
        var fc: FileChannel? = null
        try {
            fc = RandomAccessFile(file, "rw").channel
            fc.position(fc.size())
            fc.write(ByteBuffer.wrap(bytes))
            if (isForce) {
                fc.force(true) //立即写入磁盘
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            closeIO(fc)
        }
    }

    //读取文件到字节数组中，使用FileChannel
    fun readFileToBytesByFileChannel(filePath: String?): ByteArray? =
        readFileToBytesByFileChannel(FileUtil.getFileByPath(filePath))

    //读取文件到字节数组中，使用FileChannel
    fun readFileToBytesByFileChannel(file: File?): ByteArray? {
        if (!FileUtil.isFileExists(file)) {
            return null
        }
        var fc: FileChannel? = null
        try {
            fc = RandomAccessFile(file, "r").channel
            val byteBuffer = ByteBuffer.allocate(fc.size().toInt())
            while (true) {
                if (fc.read(byteBuffer) <= 0) break
            }
            return byteBuffer.array()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            closeIO(fc)
        }
    }

    //将字节数组写入文件，使用MappedByteBuffer
    fun writeFileFromBytesByMapp(filePath: String?, bytes: ByteArray?, isForce: Boolean) =
        writeFileFromBytesByMapp(FileUtil.getFileByPath(filePath), bytes, isForce)

    /**
     * 将字节数组写入文件，使用MappedByteBuffer
     *
     * @param file    文件
     * @param bytes   字节数组
     * @param isForce 是否立即写入磁盘
     * @return true：写入成功，false：写入失败
     */
    fun writeFileFromBytesByMapp(file: File?, bytes: ByteArray?, isForce: Boolean): Boolean {
        if (!FileUtil.createFileAfterDeleteOldFile(file) || bytes == null) {
            return false
        }
        var fc: FileChannel? = null
        try {
            fc = RandomAccessFile(file, "rw").channel
            val mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), bytes.size.toLong())
            mbb.put(bytes)
            if (isForce) {
                mbb.force()
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            closeIO(fc)
        }
    }

    //读取文件到字节数组中，使用MappedByteBuffer
    fun readFileToBytesByMapp(filePath: String?) = readFileToBytesByMapp(FileUtil.getFileByPath(filePath))

    //读取文件到字节数组中，使用MappedByteBuffer
    fun readFileToBytesByMapp(file: File?): ByteArray? {
        if (!FileUtil.isFileExists(file)) {
            return null
        }
        var fc: FileChannel? = null
        try {
            fc = RandomAccessFile(file, "r").channel
            val size = fc.size().toInt()
            val mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, size.toLong()).load()
            val result = ByteArray(size)
            mbb[result, 0, size]
            return result
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            closeIO(fc)
        }
    }

    //关闭IO流
    fun closeIO(vararg closeables: Closeable?) {
        for (closeable in closeables) {
            closeable?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}
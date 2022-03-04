package com.android.util

import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by xuzhb on 2020/8/9
 * Desc:文件管理类
 */
object FileUtil {

    //根据文件路径获取文件
    fun getFileByPath(filePath: String?): File? = if (StringUtil.isEmpty(filePath)) null else File(filePath)

    //判断文件是否存在，不存在则判断是否创建成功
    fun createOrExistsFile(filePath: String?) = createOrExistsFile(getFileByPath(filePath))

    //判断文件是否存在，不存在则判断是否创建成功
    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) {
            return false
        }
        if (file.exists()) {  //如果存在，判断是否是文件
            return file.isFile
        }
        if (!createOrExistsDirectory(file.parentFile)) {
            return false
        }
        try {
            return file.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    //判断目录是否存在，不存在则判断是否创建成功
    fun createOrExistsDirectory(dirPath: String?) = createOrExistsDirectory(getFileByPath(dirPath))

    //判断目录是否存在，不存在则判断是否创建成功
    fun createOrExistsDirectory(file: File?): Boolean {
        //如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    //创建文件，如果文件已存在则删除重新创建
    fun createFileAfterDeleteOldFile(filePath: String?) = createFileAfterDeleteOldFile(getFileByPath(filePath))

    //创建文件，如果文件已存在则删除重新创建，返回true：创建成功，false：创建失败
    fun createFileAfterDeleteOldFile(file: File?): Boolean {
        if (file == null) {
            return false
        }
        //如果文件已存在，则先删除，若删除失败返回false
        if (file.exists() && file.isFile && !file.delete()) {
            return false
        }
        //如果目录创建失败，返回false
        if (!createOrExistsDirectory(file.parentFile)) {
            return false
        }
        try {
            return file.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    //判断文件是否存在
    fun isFileExists(filePath: String?) = isFileExists(getFileByPath(filePath))

    //判断文件是否存在
    fun isFileExists(file: File?) = file != null && file.exists()

    //判断是否是文件
    fun isFile(filePath: String?) = isFile(getFileByPath(filePath))

    //判断是否是文件
    fun isFile(file: File?) = isFileExists(file) && file != null && file.isFile

    //判断是否是目录
    fun isDirectory(filePath: String?) = isDirectory(getFileByPath(filePath))

    //判断是否是目录
    fun isDirectory(file: File?) = isFileExists(file) && file != null && file.isDirectory

    //重命名文件
    fun renameFile(filePath: String?, newName: String) = renameFile(getFileByPath(filePath), newName)

    //重命名文件，返回true：重命名成功，false：重命名失败
    fun renameFile(file: File?, newName: String): Boolean {
        if (file == null || !file.exists() || StringUtil.isEmpty(newName)) {
            return false
        }
        if (file.name == newName) {
            return true
        }
        val newFile = File(file.parent + File.separator + newName)
        //如果重命名文件已存在则返回false
        return !newFile.exists() && file.renameTo(newFile)
    }

    //删除文件
    fun deleteFile(filePath: String?) = deleteFile(getFileByPath(filePath))

    //删除文件，返回true：删除成功，false：删除失败
    fun deleteFile(file: File?): Boolean = file != null && (!file.exists() || file.isFile && file.delete())

    //删除目录
    fun deleteDirectory(dirPath: String): Boolean {
        return deleteDirectory(getFileByPath(dirPath))
    }

    //删除目录，返回true：删除成功，false：删除失败
    fun deleteDirectory(dir: File?): Boolean {
        if (dir == null) {
            return false
        }
        if (!dir.exists()) {
            return true
        }
        if (!dir.isDirectory) {
            return false
        }
        val files = dir.listFiles()
        if (files != null && files.size != 0) {
            for (file in files) {
                if (file.isFile) {
                    if (!deleteFile(file)) {
                        return false
                    }
                } else if (file.isDirectory) {
                    if (!deleteDirectory(file)) {
                        return false
                    }
                }
            }
        }
        return dir.delete()
    }

    //复制文件
    fun copyFile(srcFilePath: String?, destFilePath: String?) = copyFile(getFileByPath(srcFilePath), getFileByPath(destFilePath))

    //复制文件
    fun copyFile(srcFile: File?, destFile: File?): Boolean = copyOrMoveFile(srcFile, destFile, false)

    //移动文件
    fun moveFile(srcFilePath: String?, destFilePath: String?) = moveFile(getFileByPath(srcFilePath), getFileByPath(destFilePath))

    //移动文件
    fun moveFile(srcFile: File?, destFile: File?) = copyOrMoveFile(srcFile, destFile, true)

    //复制目录
    fun copyDirectory(srcDirPath: String?, destDirPath: String?) =
        copyDirectory(getFileByPath(srcDirPath), getFileByPath(destDirPath))

    //复制目录
    fun copyDirectory(srcDir: File?, destDir: File?) = copyOrMoveDirectory(srcDir, destDir, false)

    //移动目录
    fun moveDirectory(srcDirPath: String?, destDirPath: String?) =
        moveDirectory(getFileByPath(srcDirPath), getFileByPath(destDirPath))

    //移动目录
    fun moveDirectory(srcDir: File?, destDir: File?) = copyOrMoveDirectory(srcDir, destDir, true)

    /**
     * 复制或移动文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @param isMove   是否移动
     * @return true：复制或移动成功，false：复制或移动失败
     */
    fun copyOrMoveFile(srcFile: File?, destFile: File?, isMove: Boolean): Boolean {
        if (srcFile == null || destFile == null) {
            return false
        }
        //源文件不存在或不是文件则返回false
        if (!srcFile.exists() || !srcFile.isFile) {
            return false
        }
        //目标文件存在且是文件则返回false
        if (destFile.exists() && destFile.isFile) {
            return false
        }
        //目标目录不存在返回false
        if (!createOrExistsDirectory(destFile.parentFile)) {
            return false
        }
        try {
            return (IOUtil.writeFileFromInputStream(destFile, FileInputStream(srcFile), false)
                    && !(isMove && !deleteFile(srcFile))) //若要移动，则在复制后删除源文件
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 复制或移动目录
     *
     * @param srcDir  源文件
     * @param destDir 目标文件
     * @param isMove  是否移动
     * @return true：复制或移动成功，false：复制或移动失败
     */
    fun copyOrMoveDirectory(srcDir: File?, destDir: File?, isMove: Boolean): Boolean {
        if (srcDir == null || destDir == null) {
            return false
        }
        //为什么在后面加File.separator，是为了防止类似"test\dir1"和"test\dir"的误判
        val srcPath = srcDir.path + File.separator
        val destPath = destDir.path + File.separator
        if (destPath.contains(srcPath)) {
            return false
        }
        // 源文件不存在或者不是目录则返回false
        if (!srcDir.exists() || !srcDir.isDirectory) {
            return false
        }
        // 目标目录不存在返回false
        if (!createOrExistsDirectory(destDir)) {
            return false
        }
        val files = srcDir.listFiles()
        for (file in files) {
            val tempDestFile = File(destPath + file.name)
            if (file.isFile) {
                // 如果操作失败返回false
                if (!copyOrMoveFile(file, tempDestFile, isMove)) {
                    return false
                }
            } else if (file.isDirectory) {
                // 如果操作失败返回false
                if (!copyOrMoveDirectory(file, tempDestFile, isMove)) {
                    return false
                }
            }
        }
        return !isMove || deleteDirectory(srcDir)
    }

    //删除目录下的所有文件，不包括根目录
    fun deleteFilesInDirectory(dirPath: String?) = deleteFilesInDirectory(getFileByPath(dirPath))

    //删除目录下的所有文件，不包括根目录
    fun deleteFilesInDirectory(dir: File?): Boolean {
        if (dir == null) {
            return false
        }
        if (!dir.exists()) {
            return true
        }
        if (!dir.isDirectory) {
            return false
        }
        val files = dir.listFiles()
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                if (file.isFile) {
                    if (!deleteFile(file)) {
                        return false
                    }
                } else if (file.isDirectory) {
                    if (!deleteDirectory(file)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    //获取目录下所有文件，包括目录和文件
    fun listFilesInDirectory(dirPath: String?, isIncludeSubDir: Boolean) =
        listFilesInDirectory(getFileByPath(dirPath), isIncludeSubDir)

    /**
     * 获取目录下所有文件，包括目录和文件
     *
     * @param dir             目录
     * @param isIncludeSubDir 是否包含子目录
     * @return 文件链表
     */
    fun listFilesInDirectory(dir: File?, isIncludeSubDir: Boolean): MutableList<File>? {
        if (dir == null || !isDirectory(dir)) {
            return null
        }
        val list: MutableList<File> = mutableListOf()
        val files = dir.listFiles()
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                list.add(file)
                if (isIncludeSubDir && file.isDirectory) {
                    val fileList = listFilesInDirectory(file, isIncludeSubDir)
                    if (fileList != null) {
                        list.addAll(fileList)
                    }
                }
            }
        }
        return list
    }

    //获取目录下所有后缀名为suffix的文件，包括目录和文件
    fun listFilesInDirectoryEndWithSuffix(dirPath: String?, suffix: String, isIncludeSubDir: Boolean, isIgnoreCase: Boolean) =
        listFilesInDirectoryEndWithSuffix(getFileByPath(dirPath), suffix, isIncludeSubDir, isIgnoreCase)

    /**
     * 获取目录下所有后缀名为suffix的文件，包括目录和文件
     *
     * @param dir             目录
     * @param suffix          后缀名
     * @param isIncludeSubDir 是否包含子目录
     * @param isIgnoreCase    是否忽略大小写
     * @return 文件链表
     */
    fun listFilesInDirectoryEndWithSuffix(
        dir: File?,
        suffix: String,
        isIncludeSubDir: Boolean,
        isIgnoreCase: Boolean
    ): MutableList<File>? = listFilesInDirectoryWithFilter(dir, isIncludeSubDir, FilenameFilter { _, name ->
        if (isIgnoreCase) {  //忽略大小写
            name.toUpperCase().endsWith(suffix.toUpperCase())
        } else {  //区分大小写
            name.endsWith(suffix)
        }
    })

    //获取目录下指定文件名的文件，包括目录和文件
    fun searchFileInDirectory(dirPath: String?, fileName: String, isIncludeSubDir: Boolean, isIgnoreCase: Boolean) =
        searchFileInDirectory(getFileByPath(dirPath), fileName, isIncludeSubDir, isIgnoreCase)

    /**
     * 获取目录下指定文件名的文件，包括目录和文件
     *
     * @param dir             目录
     * @param fileName        文件名
     * @param isIncludeSubDir 是否包含子目录
     * @param isIgnoreCase    是否忽略大小写
     * @return 文件链表
     */
    fun searchFileInDirectory(
        dir: File?,
        fileName: String,
        isIncludeSubDir: Boolean,
        isIgnoreCase: Boolean
    ): MutableList<File>? = listFilesInDirectoryWithFilter(dir, isIncludeSubDir, FilenameFilter { _, name ->
        if (isIgnoreCase) {  //忽略大小写
            name.toUpperCase() == fileName.toUpperCase()
        } else {
            name == fileName
        }
    })

    //获取目录下所有符合filter的文件，包括目录和文件
    fun listFilesInDirectoryWithFilter(dirPath: String?, isIncludeSubDir: Boolean, filter: FilenameFilter) =
        listFilesInDirectoryWithFilter(getFileByPath(dirPath), isIncludeSubDir, filter)

    /**
     * 获取目录下所有符合filter的文件，包括目录和文件
     *
     * @param dir             目录
     * @param filter          过滤器
     * @param isIncludeSubDir 是否包含子目录
     * @return 文件链表
     */
    fun listFilesInDirectoryWithFilter(dir: File?, isIncludeSubDir: Boolean, filter: FilenameFilter): MutableList<File>? {
        if (dir == null || !isDirectory(dir)) {
            return null
        }
        val list: MutableList<File> = mutableListOf()
        val files = dir.listFiles()
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                if (filter.accept(file.parentFile, file.name)) {
                    list.add(file)
                }
                if (isIncludeSubDir && file.isDirectory) {
                    val fileList = listFilesInDirectoryWithFilter(file, isIncludeSubDir, filter)
                    if (fileList != null) {
                        list.addAll(fileList)
                    }
                }
            }
        }
        return list
    }

    //获取文件最后修改的毫秒时间戳
    fun getFileLastModified(filePath: String?) = getFileLastModified(getFileByPath(filePath))

    //获取文件最后修改的毫秒时间戳
    fun getFileLastModified(file: File?): Long = file?.lastModified() ?: -1

    //获取文件编码格式
    fun getFileCharset(filePath: String?) = getFileCharset(getFileByPath(filePath))

    //获取文件编码格式
    fun getFileCharset(file: File?): String {
        var p = 0
        var inputStream: InputStream? = null
        try {
            inputStream = BufferedInputStream(FileInputStream(file))
            p = (inputStream.read() shl 8) + inputStream.read()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtil.closeIO(inputStream)
        }
        return when (p) {
            0xefbb -> "UTF-8"
            0xfffe -> "Unicode"
            0xfeff -> "UTF-16BE"
            else -> "GBK"
        }
    }

    //获取文件行数，返回-1表示读取失败，比readLine快
    fun getFileLines(filePath: String?) = getFileLines(getFileByPath(filePath))

    //获取文件行数，返回-1表示读取失败，比readLine快
    fun getFileLines(file: File?): Int {
        var fileReader: FileReader? = null
        var reader: LineNumberReader? = null
        try {
            fileReader = FileReader(file)
            reader = LineNumberReader(fileReader)
            reader.skip(Long.MAX_VALUE)
            return reader.lineNumber + 1 //实际上是读取换行符数量，而最后一行文本没有换行符，所以需要加1
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            IOUtil.closeIO(fileReader, reader)
        }
        return -1
    }

    //获取文件名
    fun getFileName(filePath: String?): String? {
        if (StringUtil.isEmpty(filePath)) {
            return filePath
        }
        val lastSep = filePath!!.lastIndexOf(File.separator)
        return if (lastSep == -1) filePath else filePath.substring(lastSep + 1)
    }

    //获取文件大小，以byte为单位
    fun getFileSize(filePath: String?) = getFileSize(getFileByPath(filePath))

    //获取文件大小，以byte为单位
    fun getFileSize(file: File?): Long = if (isFile(file)) file!!.length() else -1

    //获取文件大小，unit：字节单位，如b、kb、mb、gb等
    fun getFileSize(filePath: String?, @ByteUnit.ByteUnitDef unit: String) = getFileSize(getFileByPath(filePath), unit)

    //获取文件大小，unit：字节单位，如b、kb、mb、gb等
    fun getFileSize(file: File?, @ByteUnit.ByteUnitDef unit: String): Double =
        if (isFile(file)) ByteUnit.convertByteUnit(file!!.length().toDouble(), unit) else -1.0

    //获取目录大小，以byte为单位
    fun getDirectorySize(dirPath: String?) = getDirectorySize(getFileByPath(dirPath))

    //获取目录大小，以byte为单位
    fun getDirectorySize(dir: File?): Long {
        if (!isDirectory(dir)) {
            return -1
        }
        var size = 0L
        val files = dir!!.listFiles()
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                size += if (file.isDirectory) getDirectorySize(file) else file.length()
            }
        }
        return size
    }

    //获取目录大小，unit：字节单位，如b、kb、mb、gb等
    fun getDirectorySize(filePath: String?, @ByteUnit.ByteUnitDef unit: String) =
        getDirectorySize(getFileByPath(filePath), unit)

    //获取目录大小，unit：字节单位，如b、kb、mb、gb等
    fun getDirectorySize(file: File?, @ByteUnit.ByteUnitDef unit: String) =
        ByteUnit.convertByteUnit(getDirectorySize(file).toDouble(), unit)

    //获取文件的MD5
    fun getFileMD5String(filePath: String?) = getFileMD5String(getFileByPath(filePath))

    //获取文件的MD5
    fun getFileMD5String(file: File?): String? {
        val bytes = getFileMD5(file) ?: return null
        return TransformUtil.bytes2Hex(bytes)
    }

    //获取文件的MD5
    fun getFileMD5(filePath: String?) = getFileMD5(getFileByPath(filePath))

    //获取文件的MD5
    fun getFileMD5(file: File?): ByteArray? {
        if (file == null) {
            return null
        }
        var dis: DigestInputStream? = null
        try {
            val fis = FileInputStream(file)
            var md = MessageDigest.getInstance("MD5")
            dis = DigestInputStream(fis, md)
            val buffer = ByteArray(1024 * 256)
            while (true) {
                if (dis.read(buffer) <= 0) break
            }
            md = dis.messageDigest
            return md.digest()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtil.closeIO(dis)
        }
        return null
    }

}
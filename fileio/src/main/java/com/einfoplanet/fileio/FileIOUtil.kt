package com.einfoplanet.fileio

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import java.io.*
import java.text.DecimalFormat
import java.util.*

/**
 * Common class for internal and external storage implementations
 *
 * @author Eshvar Mali
 */
class FileIOUtil(private val mContext: Context) {

    val internalRootDirectory: String
        get() = Environment.getRootDirectory().absolutePath

    val internalFilesDirectory: String
        get() = mContext.filesDir.absolutePath

    val internalCacheDirectory: String
        get() = mContext.cacheDir.absolutePath

    fun createDirectory(path: String): Boolean {
        val directory = File(path)
        if (directory.exists()) {
            Log.e(TAG, "Directory '$path' already exists")
            return false
        }
        return directory.mkdirs()
    }

    fun createDirectory(path: String, override: Boolean): Boolean {

        // Check if directory exists. If yes, then delete all directory
        if (override && isDirectoryExists(path)) {
            deleteDirectory(path)
        }

        // Create new directory
        return createDirectory(path)
    }

    fun createFile(path: String, fileName: String): Boolean {
        val file = File(path + File.separator + fileName)
        return when {
            !file.exists() -> {
                file.createNewFile()
                Log.e(
                    TAG,
                    "File '" + file.absolutePath + " file created successfully"
                )
                true
            }
            else -> {
                Log.e(
                    TAG,
                    "File '" + file.absolutePath + " file already exists"
                )
                false
            }
        }
    }

    private fun deleteDirectory(path: String): Boolean {
        return deleteDirectoryImpl(path)
    }

    private fun isDirectoryExists(path: String?): Boolean {
        return File(path).exists()
    }

    fun writeToFile(path: String, content: String): Boolean {
        return writeToFile(path, content.toByteArray())
    }

    private fun writeToFile(path: String, content: ByteArray): Boolean {
        try {
            val stream: OutputStream = FileOutputStream(File(path))
            stream.write(content)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            Log.e(TAG, "Failed create file", e)
            return false
        }
        return true
    }

    fun writeToFile(path: String, bitmap: Bitmap): Boolean {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        return writeToFile(path, byteArray)
    }

    fun deleteFile(path: String?): Boolean {
        val file = File(path)
        return file.delete()
    }

    fun isFileExist(path: String?): Boolean {
        return File(path).exists()
    }

    fun appendFile(path: String?, content: String) {
        appendFile(path, content.toByteArray())
    }

    private fun appendFile(path: String?, bytes: ByteArray?) {
        if (!isFileExist(path)) {
            Log.e(
                TAG,
                "Impossible to append content, because such file doesn't exist"
            )
            return
        }
        try {
            val stream =
                FileOutputStream(File(path), true)
            stream.write(bytes)
            stream.write(System.getProperty("line.separator")!!.toByteArray())
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to append content to file", e)
        }
    }

    fun getNestedFiles(path: String?): List<File> {
        val file = File(path)
        val out: MutableList<File> = ArrayList()
        getDirectoryFilesImpl(file, out)
        return out
    }

    private fun getFiles(
        dir: String?
    ): List<File>? {
        val file = File(dir)
        var files: Array<File?>? = null
        files = file.listFiles()
        return if (files != null) listOf<File>(*files) else null
    }

    private fun getFile(path: String?): File {
        return File(path)
    }

    fun rename(fromPath: String?, toPath: String?): Boolean {
        val file = getFile(fromPath)
        val newFile = File(toPath)
        return file.renameTo(newFile)
    }

    fun getSize(file: File, unit: SizeUnit): Double {
        val length = file.length()
        return length.toDouble() / unit.inBytes().toDouble()
    }

    fun getReadableSize(file: File): String {
        val length = file.length()
        return SizeUnit.readableSizeUnit(length)
    }

    fun getFreeSpace(dir: String?, sizeUnit: SizeUnit): Long {
        val statFs = StatFs(dir)
        val availableBlocks: Long
        val blockSize: Long
        availableBlocks = statFs.availableBlocksLong
        blockSize = statFs.blockSizeLong
        val freeBytes = availableBlocks * blockSize
        return freeBytes / sizeUnit.inBytes()
    }

    enum class SizeUnit(private val inBytes: Long) {
        B(1),
        KB(1024L),
        MB((1024L * 1024L)),
        GB((1024L * 1024L * 1024L)),
        TB((1024L * 1024L * 1024L * 1024L));

        fun inBytes(): Long {
            return inBytes
        }

        companion object {
            fun readableSizeUnit(bytes: Long): String {
                val df = DecimalFormat("0.00")
                return when {
                    bytes < KB.inBytes() -> {
                        df.format(bytes / B.inBytes()) + " B"
                    }
                    bytes < MB.inBytes() -> {
                        df.format(bytes / KB.inBytes()) + " KB"
                    }
                    bytes < GB.inBytes() -> {
                        df.format(bytes / MB.inBytes()) + " MB"
                    }
                    else -> {
                        df.format(bytes / GB.inBytes()) + " GB"
                    }
                }
            }
        }

    }

    fun getUsedSpace(dir: String?, sizeUnit: SizeUnit): Long {
        val statFs = StatFs(dir)
        val availableBlocks: Long
        val blockSize: Long
        val totalBlocks: Long
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            availableBlocks = statFs.availableBlocks.toLong()
            blockSize = statFs.blockSize.toLong()
            totalBlocks = statFs.blockCount.toLong()
        } else {
            availableBlocks = statFs.availableBlocksLong
            blockSize = statFs.blockSizeLong
            totalBlocks = statFs.blockCountLong
        }
        val usedBytes = totalBlocks * blockSize - availableBlocks * blockSize
        return usedBytes / sizeUnit.inBytes()
    }

    /**
     * Delete the directory and all sub content.
     *
     * @param path The absolute directory path. For example:
     * *mnt/sdcard/NewFolder/ *.
     * @return `True` if the directory was deleted, otherwise return
     * `False`
     */
    private fun deleteDirectoryImpl(path: String): Boolean {
        val directory = File(path)

        // If the directory exists then delete
        if (directory.exists()) {
            val files = directory.listFiles() ?: return true
            // Run on all sub files and folders and delete them
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    deleteDirectoryImpl(files[i].absolutePath)
                } else {
                    files[i].delete()
                }
            }
        }
        return directory.delete()
    }

    /**
     * Get all files under the directory
     *
     * @param directory
     * @param out
     * @return
     */
    private fun getDirectoryFilesImpl(
        directory: File,
        out: MutableList<File>
    ) {
        if (directory.exists()) {
            val files = directory.listFiles()
            if (files == null) {
                return
            } else {
                for (i in files.indices) {
                    if (files[i].isDirectory) {
                        getDirectoryFilesImpl(files[i], out)
                    } else {
                        out.add(files[i])
                    }
                }
            }
        }
    }

    private fun closeSilently(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
            }
        }
    }

    companion object {
        private const val TAG = "Storage"
        val isExternalWritable: Boolean
            get() {
                val state = Environment.getExternalStorageState()
                return if (Environment.MEDIA_MOUNTED == state) {
                    true
                } else false
            }
    }

}
package com.lifengqiang.audiouse.utils

import android.annotation.SuppressLint
import com.lifengqiang.audiouse.MainActivity
import java.io.File

class FileManager {
    companion object {
        private var appFiles: File? = null

        @SuppressLint("SdCardPath")
        private fun getDataDirectoryPath(): String {
            return "/sdcard/Android/data/${MainActivity::class.java.`package`?.name}"
        }

        private fun getAppFilePath(): File {
            if (appFiles == null) {
                appFiles = File("${getDataDirectoryPath()}/files")
                appFiles!!.mkdirs()
            }
            return appFiles!!
        }

        fun getGroupFile(): File {
            return File(getAppFilePath(), "groups.json")
        }

        fun getTempSelectFilesFile(): File {
            return File(getAppFilePath(), "temp.txt")
        }

        fun getGroupDirectory(): File {
            val file = File(getAppFilePath(), "groups")
            file.mkdirs()
            return file
        }

        fun getGroupFile(gid: String): File {
            return File(getGroupDirectory(), "$gid.json")
        }
    }
}
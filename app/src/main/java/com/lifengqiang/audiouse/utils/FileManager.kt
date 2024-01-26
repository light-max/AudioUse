package com.lifengqiang.audiouse.utils

import android.content.Context
import java.io.File
import java.io.IOException

class FileManager {
    companion object {
        private var appFiles: File? = null

        private fun getDataDirectoryPath(): String {
            if (appFiles == null) throw IOException("appFiles not init")
            return appFiles!!.absolutePath
        }

        fun setDataDirectoryPath(context: Context) {
            appFiles = context.getExternalFilesDir("")?.parentFile
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
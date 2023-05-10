package com.lifengqiang.audiouse.data

import android.os.Handler
import android.os.Looper
import com.lifengqiang.audiouse.utils.FileManager
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class TempSelectFile {
    companion object {
        fun write(files: () -> ArrayList<String>, call: () -> Unit) {
            Thread {
                val out = FileOutputStream(FileManager.getTempSelectFilesFile())
                for (file in files()) {
                    out.write("${file}\n".toByteArray())
                }
                out.close()
                Handler(Looper.getMainLooper()).post(call)
            }.start()
        }

        fun read(call: (ArrayList<String>) -> Unit) {
            Thread {
                val list = ArrayList<String>()
                val input = FileInputStream(FileManager.getTempSelectFilesFile())
                val reader = BufferedReader(InputStreamReader(input))
                while (true) {
                    val line = reader.readLine()
                    if (line == null) {
                        break
                    } else {
                        list.add(line)
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    call(list)
                }
            }.start()
        }
    }
}
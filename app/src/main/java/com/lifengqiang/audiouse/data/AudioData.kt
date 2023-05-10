package com.lifengqiang.audiouse.data

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.google.gson.Gson
import com.lifengqiang.audiouse.utils.FileManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class Audio(val name: String, val path: String)

class Audios(val gid: String, val list: ArrayList<Audio>) {
    fun writeToFile() {
        val out = FileOutputStream(FileManager.getGroupFile(gid))
        val json = Gson().toJson(this)
        out.write(json.toByteArray())
        out.close()
    }

    companion object {
        fun readFromFile(gid: String): Audios {
            try {
                val input = FileInputStream(FileManager.getGroupFile(gid))
                return Gson().fromJson(InputStreamReader(input), Audios::class.java)
            } catch (e: Exception) {
                return Audios(gid, ArrayList())
            }
        }
    }
}

class AudioData() {
    private var thread: HandlerThread = HandlerThread("AudioFileReader")
    private var handler: Handler
    private var mainHandler: Handler = Handler(Looper.getMainLooper())

    init {
        thread.start()
        handler = Handler(thread.looper)
    }

    companion object {
        private var instance_: AudioData? = null

        fun getInstance(): AudioData {
            synchronized(AudioData::class.java) {
                if (instance_ == null) {
                    instance_ = AudioData()
                }
                return instance_!!
            }
        }
    }

    fun addAudio(gid: String, audio: Audio, call: (() -> Unit)?) {
        handler.post {
            val audios = Audios.readFromFile(gid)
            audios.list.add(audio)
            audios.writeToFile()
            mainHandler.post {
                call?.invoke()
            }
        }
    }

    fun addAudios(gid: String, list: ArrayList<String>, call: (() -> Unit)?) {
        handler.post {
            val audios = Audios.readFromFile(gid)
            val array = ArrayList<String>()
            for (s in list) {
                var find = false
                for (audio in audios.list) {
                    find = audio.path == s
                    if (find) {
                        break
                    }
                }
                if (!find) {
                    array.add(s)
                }
            }
            for (s in array) {
                audios.list.add(Audio(File(s).name, s))
            }
            audios.writeToFile()
            call?.invoke()
        }
    }

    fun readAudios(gid: String, call: (Audios) -> Unit) {
        handler.post {
            val audios = Audios.readFromFile(gid)
            mainHandler.post {
                call(audios)
            }
        }
    }

    fun deleteAudio(gid: String, audio: Audio, call: (() -> Unit)?) {
        handler.post {
            val audios = Audios.readFromFile(gid)
            audios.list.removeIf { it.path == audio.path }
            audios.writeToFile()
            mainHandler.post {
                call?.invoke()
            }
        }
    }
}
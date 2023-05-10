package com.lifengqiang.audiouse.data

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.google.gson.Gson
import com.lifengqiang.audiouse.utils.FileManager
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class Group(var id: String, var name: String)

class Groups() {
    var list: ArrayList<Group> = ArrayList()

    fun writeToFile() {
        try {
            val json = Gson().toJson(this)
            val out = FileOutputStream(FileManager.getGroupFile())
            out.write(json.toByteArray())
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun readFromFile(): Groups {
            val groupFile = FileManager.getGroupFile()
            return if (groupFile.exists()) {
                val input = FileInputStream(groupFile)
                Gson().fromJson(InputStreamReader(input), Groups::class.java)
            } else {
                Groups()
            }
        }
    }
}

class GroupData() {
    private var thread: HandlerThread = HandlerThread("GroupFileReader")
    private var handler: Handler
    private var mainHandler: Handler = Handler(Looper.getMainLooper())

    init {
        thread.start()
        handler = Handler(thread.looper)
    }

    companion object {
        private var instance_: GroupData? = null

        fun getInstance(): GroupData {
            synchronized(GroupData::class.java) {
                if (instance_ == null) {
                    instance_ = GroupData()
                }
                return instance_!!
            }
        }
    }

    fun addGroup(name: String, call: ((Group) -> Unit)?) {
        handler.post {
            val groups = Groups.readFromFile()
            val group = Group(UUID.randomUUID().toString(), name)
            groups.list.add(group)
            groups.writeToFile()
            mainHandler.post {
                if (call != null) {
                    call(group)
                }
            }
        }
    }

    fun readGroups(call: ((Groups) -> Unit)?) {
        handler.post {
            val groups = Groups.readFromFile()
            mainHandler.post {
                if (call != null) {
                    call(groups)
                }
            }
        }
    }

    fun deleteGroup(gid: String, call: (() -> Unit)?) {
        handler.post {
            val groups = Groups.readFromFile()
            groups.list.removeIf { it.id == gid }
            groups.writeToFile()
            mainHandler.post {
                if (call != null) {
                    call()
                }
            }
        }
    }

    fun editGroup(group: Group, call: (() -> Unit)?) {
        handler.post {
            val groups = Groups.readFromFile()
            for (g in groups.list) {
                if (g.id == group.id) {
                    g.name = group.name
                    break
                }
            }
            groups.writeToFile()
            mainHandler.post {
                if (call != null) {
                    call()
                }
            }
        }
    }
}
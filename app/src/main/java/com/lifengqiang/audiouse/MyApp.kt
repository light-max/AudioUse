package com.lifengqiang.audiouse

import android.app.Application
import com.lifengqiang.audiouse.utils.FileManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FileManager.setDataDirectoryPath(applicationContext)
    }
}
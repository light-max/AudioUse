package com.lifengqiang.audiouse.codec.base

import android.media.MediaFormat
import java.nio.ByteBuffer

interface IExtractor {
    /**
     * 设置文件路径
     */
    fun setFilePath(path: String): Boolean

    /**
     * 获取文件路径
     */
    fun getFilePath(): String?

    /**
     * 获取格式参数
     */
    fun getFormat(): MediaFormat

//    /**
//     * 获取轨道
//     */
//    fun getTrack(): Int

    /**
     * 读取数据
     */
    fun readBuffer(buffer: ByteBuffer): Int

    /**
     * 获取当前时间帧，微秒
     */
    fun getCurrentTimestamp(): Long

    /**
     * 跳转到指定位置, 微秒
     */
    fun seek(timestamp: Long): Long

    /**
     * 释放提取器
     */
    fun release()
}
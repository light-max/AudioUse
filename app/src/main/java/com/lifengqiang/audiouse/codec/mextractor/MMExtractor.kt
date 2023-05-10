package com.lifengqiang.audiouse.codec.mextractor

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer

class MMExtractor {
    /** 音视频分离器*/
    private var extractor: MediaExtractor? = null

    /** 对应轨道的格式参数*/
    var format: MediaFormat? = null

    /** 当前时间戳,原始时间,单位为微秒*/
    var currentTimestamp: Long = 0

    /** 文件路径 */
    var path: String? = null

    /**
     * 设置为分离音频文件
     */
    fun setAudioPath(filePath: String): Boolean = setFilePath(filePath, false)

    /**
     * 设置为分离视频文件
     */
    fun setVideoPath(filePath: String): Boolean = setFilePath(filePath, true)

    private fun setFilePath(filePath: String, video: Boolean): Boolean {
        try {
            extractor = MediaExtractor()
            extractor!!.setDataSource(filePath)
            path = filePath
            val type = if (video) "video" else "audio"
            for (i in 0 until extractor!!.trackCount) {
                val f = extractor!!.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME)
                if (mime != null && mime.startsWith(type)) {
                    extractor!!.selectTrack(i)
                    format = f
                    return true
                }
            }
            Log.w(this::javaClass.name, "无法读取的媒体文件: $filePath")
            return false
        } catch (e: Exception) {
            Log.w(this::javaClass.name, "媒体文件访问失败: $filePath")
            e.printStackTrace()
            return false
        }
    }

    /**
     * 从提取器中读取数据
     */
    fun readBuffer(buffer: ByteBuffer): Int {
        val sampleSize = extractor!!.readSampleData(buffer, 0)
        if (sampleSize >= 0) {
            currentTimestamp = extractor!!.sampleTime
            extractor!!.advance()
        }
        return sampleSize
    }

    /**
     * 跳转到指定时间附近的帧，单位为微秒
     */
    fun seekTo(timeUse: Long): Long {
        if (extractor == null) {
            return 0
        }
        extractor!!.seekTo(timeUse, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        return extractor!!.sampleTime
    }

    fun release() {
        extractor?.release()
        extractor = null
        currentTimestamp = 0
        format = null
    }
}
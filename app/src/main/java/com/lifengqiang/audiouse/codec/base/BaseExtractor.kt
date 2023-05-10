package com.lifengqiang.audiouse.codec.base

import android.media.MediaFormat
import com.lifengqiang.audiouse.codec.mextractor.MMExtractor
import java.nio.ByteBuffer

abstract class BaseExtractor : IExtractor {
    val extractor = MMExtractor()

    override fun getFilePath(): String? {
        return extractor.path
    }

    override fun getFormat(): MediaFormat {
        return extractor.format!!
    }

    override fun readBuffer(buffer: ByteBuffer): Int {
        return extractor.readBuffer(buffer)
    }

    override fun getCurrentTimestamp(): Long {
        return extractor.currentTimestamp
    }

    override fun seek(timestamp: Long): Long {
        return extractor.seekTo(timestamp)
    }

    override fun release() {
        extractor.release()
    }
}
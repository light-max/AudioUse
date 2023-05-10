package com.lifengqiang.audiouse.codec.mextractor

import com.lifengqiang.audiouse.codec.base.BaseExtractor

class AudioExtractor : BaseExtractor() {
    override fun setFilePath(path: String): Boolean {
        return extractor.setAudioPath(path)
    }
}
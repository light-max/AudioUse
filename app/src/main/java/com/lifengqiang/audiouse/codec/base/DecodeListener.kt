package com.lifengqiang.audiouse.codec.base

import android.media.MediaFormat

interface DecodeListener {
    /**
     * 解码开始时
     */
    fun onStartDecode(decoder: BaseDecoder) {
    }

    /**
     * 媒体资源准备好之后
     */
    fun onReady(decoder: BaseDecoder, format: MediaFormat)

    /**
     * 暂停时
     */
    fun onPause(decoder: BaseDecoder)

    /**
     * 继续时
     */
    fun whenContinue(decoder: BaseDecoder)

    /**
     * 下一帧时
     */
    fun onNextFrame(decoder: BaseDecoder)

    /**
     * 播放结束时
     */
    fun onFinish(decoder: BaseDecoder)

    /**
     * 停止解码时
     */
    fun onStopDecode(decoder: BaseDecoder) {
    }
}
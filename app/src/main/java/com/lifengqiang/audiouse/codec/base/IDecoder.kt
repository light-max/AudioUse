package com.lifengqiang.audiouse.codec.base

enum class ThreadState {
    /** 准备状态 */
    PREPARE,

    /** 释放状态 */
    FREE,

    /** 解码中 */
    DECODING,

    /** 暂停状态 */
    PAUSE,

    /** 解码完成 */
    FINISH,
}

interface IDecoder {
    /**
     * 设置媒体文件路径
     * @param isPlay 媒体文件加载后是否立即播放
     */
    fun setFilePath(filePath: String?, isPlay: Boolean = false)

    /**
     * 继续解码
     */
    fun goOn()

    /**
     * 暂停解码
     */
    fun pause()

    /**
     * 切换解码和暂停的状态
     */
    fun toggle()

    /**
     * 获取持续时间, 单位毫秒
     */
    fun getDuration(): Long

    /**
     * 获取进度, 单位毫秒
     */
    fun getProgress(): Long

    /**
     * 设置进度, 单位毫秒
     */
    fun setProgress(progress: Long)

    /**
     * 设置监听器
     */
    fun setDecodeListener(listener: DecodeListener)
}
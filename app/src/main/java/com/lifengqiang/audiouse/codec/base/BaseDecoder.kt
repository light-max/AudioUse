package com.lifengqiang.audiouse.codec.base

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer

private const val TAG = "BaseDecoder"

abstract class BaseDecoder : IDecoder {
    /** 线程同步锁 */
    private val lock = Object()

    /** 线程状态 */
    var state = ThreadState.PAUSE

    /** 准备后是否立即播放 */
    private var isPlayAfterPrepare = false

    /** 是否可以改变线程状态 */
    private var isCanChangeThreadState = true

    /** 流数据是否结束 */
    private var isEOS = false

    /** 提取器 */
    private lateinit var extractor: IExtractor

    /** 媒体文件路径 */
    var mediaPath: String? = null

    /** 解码器 */
    private var codec: MediaCodec? = null

    /** 持续时间, 毫秒*/
    private var duration: Long = 0L

    /** 解码数据信息 */
    private var bufferInfo = MediaCodec.BufferInfo()

    /** 解码状态监听器 */
    private var listener: DecodeListener? = null

    /** 是否使用帧监听 */
    var useFrameListener = true

    fun startThread() {
        extractor = makeExtractor()
        Thread({
            listener?.onStartDecode(this@BaseDecoder)
            do {
                isCanChangeThreadState = true
            } while (execute())
            release()
            listener?.onStopDecode(this@BaseDecoder)
        }, javaClass.name).start()
    }

    fun stopThread() {
        state = ThreadState.FREE
        notifyDecode()
    }

    private fun execute(): Boolean {
        if (state == ThreadState.PREPARE) {
            release()
            if (init()) {
                state = if (isPlayAfterPrepare) {
                    ThreadState.DECODING
                } else {
                    ThreadState.PAUSE
                }
                isEOS = false
                bufferInfo = MediaCodec.BufferInfo()
                listener?.onReady(this, extractor.getFormat())
                if (isPlayAfterPrepare) {
                    isPlayAfterPrepare = false
                    listener?.whenContinue(this)
                }
            } else {
                state = ThreadState.PAUSE
            }
        } else if (state == ThreadState.PAUSE) {
            listener?.onPause(this)
            waitDecode()
        } else if (state == ThreadState.DECODING) {
            if (!isEOS) {
                isEOS = pushBufferToDecoder()
            }
            val pair = pullBufferFromDecoder()
            if (pair.first >= 0) {
                render(pair.second!!, bufferInfo)
                codec!!.releaseOutputBuffer(pair.first, true)
                if (useFrameListener) {
                    listener?.onNextFrame(this)
                }
            }
            if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                state = ThreadState.FINISH
            }
        } else if (state == ThreadState.FREE) {
            return false
        } else if (state == ThreadState.FINISH) {
            listener?.onFinish(this)
            if (state == ThreadState.FINISH) {
                waitDecode()
            }
        }
        return true
    }

    override fun setFilePath(filePath: String?, isPlay: Boolean) {
        if (filePath != null && filePath != extractor.getFilePath()) {
            val upState = state
            mediaPath = filePath
            state = ThreadState.PREPARE
            isPlayAfterPrepare = isPlay
            isCanChangeThreadState = false
            if (upState == ThreadState.PAUSE || upState == ThreadState.FINISH) {
                notifyDecode()
            }
        }
    }

    override fun goOn() {
        if (isCanChangeThreadState) {
            if (state == ThreadState.PAUSE) {
                state = ThreadState.DECODING
                isPlayAfterPrepare = false
                isCanChangeThreadState = false
                listener?.whenContinue(this)
                notifyDecode()
            } else if (state == ThreadState.FINISH) {
                state = ThreadState.PREPARE
                isPlayAfterPrepare = true
                isCanChangeThreadState = false
                listener?.whenContinue(this)
                notifyDecode()
            }
        }
    }

    override fun pause() {
        if (isCanChangeThreadState) {
            state = ThreadState.PAUSE
            isCanChangeThreadState = false
        }
    }

    override fun toggle() {
        if (state == ThreadState.PAUSE || state == ThreadState.FINISH) {
            goOn()
        } else if (state == ThreadState.DECODING) {
            pause()
        }
    }

    fun invokeListener(listener: DecodeListener) {
        if (extractor.getFilePath() != null) {
            listener.onReady(this, extractor.getFormat())
        }
        if (state == ThreadState.DECODING) {
            listener.whenContinue(this)
        } else if (state == ThreadState.PAUSE) {
            listener.onPause(this)
        } else if (state == ThreadState.FINISH) {
            listener.onFinish(this)
        }
        listener.onNextFrame(this)
    }

    private fun init(): Boolean {
        if (mediaPath == null) {
            Log.e(TAG, "文件路径为空")
            return false
        }
        //1.检查子类
        if (!check()) return false
        //2.初始化提取器
        if (!extractor.setFilePath(mediaPath!!)) return false
        //3.初始化参数
        if (!initParams()) return false
        //4.初始化渲染器
        if (!initRender()) return false
        //5.初始化解码器
        if (!initCodec()) return false
        return true
    }

    /**
     * 初始化参数
     */
    private fun initParams(): Boolean {
        return try {
            val format = extractor.getFormat()
            duration = format.getLong(MediaFormat.KEY_DURATION)
            initSpecParams(format)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 初始化解码器
     */
    private fun initCodec(): Boolean {
        return try {
            // 1.根据音频编码格式初始化解码器
            val type = extractor.getFormat().getString(MediaFormat.KEY_MIME)
            codec = MediaCodec.createDecoderByType(type!!)
            // 2.配置解码器
            if (!configCodec(codec!!, extractor.getFormat())) {
                return false
            }
            // 3.启动解码器
            codec!!.start()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 将数据压入解码缓冲区
     */
    private fun pushBufferToDecoder(): Boolean {
        var isEndOfStream = false
        val index = codec!!.dequeueInputBuffer(2000)
        if (index >= 0) {
            val buffer = codec!!.getInputBuffer(index)
            val sampleSize = extractor.readBuffer(buffer!!)
            // 如果数据已经取完，压入数据结束标志：BUFFER_FLAG_END_OF_STREAM
            if (sampleSize < 0) {
                codec!!.queueInputBuffer(
                    index, 0, 0, 0,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEndOfStream = false
            } else {
                codec!!.queueInputBuffer(
                    index, 0, sampleSize,
                    extractor.getCurrentTimestamp(), 0
                )
            }
        }
        return isEndOfStream
    }

    /**
     * 将解码缓冲区的数据拉出来
     */
    private fun pullBufferFromDecoder(): Pair<Int, ByteBuffer?> {
        val index = codec!!.dequeueOutputBuffer(bufferInfo, 1000)
        return if (index >= 0) {
            Pair(index, codec!!.getOutputBuffer(index))
        } else {
            Pair(index, null)
        }
    }

    /**
     * 释放所有资源
     */
    protected open fun release() {
        try {
            extractor.release()
            codec?.stop()
            codec?.release()
            codec = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 检查子类参数
     */
    abstract fun check(): Boolean

    /**
     * 创建数据提取器
     */
    abstract fun makeExtractor(): IExtractor;

    /**
     * 初始化子类自己特有的参数
     */
    abstract fun initSpecParams(format: MediaFormat)

    /**
     * 初始化渲染器
     */
    abstract fun initRender(): Boolean

    /** 渲染*/
    abstract fun render(buffer: ByteBuffer, info: MediaCodec.BufferInfo)

    /**
     * 配置解码器
     */
    abstract fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean

    override fun setDecodeListener(listener: DecodeListener) {
        this.listener = listener
    }

    override fun getDuration(): Long {
        return duration / 1000
    }

    override fun getProgress(): Long {
        return extractor.getCurrentTimestamp() / 1000
    }

    override fun setProgress(progress: Long) {
        extractor.seek(progress * 1000)
    }

    private fun notifyDecode() {
        try {
            synchronized(lock) {
                lock.notifyAll()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun waitDecode() {
        try {
            synchronized(lock) {
                lock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
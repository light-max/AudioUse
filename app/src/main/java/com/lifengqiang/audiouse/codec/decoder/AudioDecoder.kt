package com.lifengqiang.audiouse.codec.decoder

import android.media.*
import com.lifengqiang.audiouse.codec.base.BaseDecoder
import com.lifengqiang.audiouse.codec.base.IExtractor
import com.lifengqiang.audiouse.codec.mextractor.AudioExtractor
import java.nio.ByteBuffer

class AudioDecoder : BaseDecoder() {
    /** 音频播放器 */
    private var track: AudioTrack? = null

    /** 声道数 */
    private var channel = 0

    /** 采样位数 */
    private var pcmEncodeBit = 0

    /** 采样率 */
    private var sampleRate = 0

    /** 最小数据缓冲大小 */
    private var minBufferSize = 0

    override fun check(): Boolean {
        return true
    }

    override fun makeExtractor(): IExtractor {
        return AudioExtractor()
    }

    override fun initSpecParams(format: MediaFormat) {
        channel = if (format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 1) {
            AudioFormat.CHANNEL_OUT_MONO
        } else {
            AudioFormat.CHANNEL_OUT_STEREO
        }
        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        pcmEncodeBit = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
            format.getInteger(MediaFormat.KEY_PCM_ENCODING)
        } else {
            AudioFormat.ENCODING_PCM_16BIT
        }
        minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, pcmEncodeBit)
    }

    override fun initRender(): Boolean {
        return try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setChannelMask(channel)
                        .setSampleRate(sampleRate)
                        .setEncoding(pcmEncodeBit)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(minBufferSize)
                .build()
            track!!.play()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun render(buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        track!!.write(buffer, info.size, AudioTrack.WRITE_BLOCKING)
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        codec.configure(format, null, 0, null)
        return true
    }

    override fun release() {
        super.release()
        track?.stop()
        track?.release()
        track = null
    }
}
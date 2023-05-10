package com.lifengqiang.audiouse.codec

import android.media.MediaFormat
import com.lifengqiang.audiouse.codec.PlayMode.*
import com.lifengqiang.audiouse.codec.base.BaseDecoder
import com.lifengqiang.audiouse.codec.base.DecodeListener
import com.lifengqiang.audiouse.codec.base.ThreadState
import com.lifengqiang.audiouse.codec.decoder.AudioDecoder

private var instance: AudioPlayer? = null

enum class PlayMode {
    /** 当体循环 */
    SINGLE,

    /** 列表循环 */
    LIST_LOOP,

    /** 顺序播放 */
    PLAY_IN_ORDER,

    /** 随机播放 */
    RANDOM,
}

interface OnPlayListListener {
    fun onPlayListChange(list: ArrayList<String>)
}

interface OnPlayModeListener {
    fun onPlayModeChange(player: AudioPlayer)
}

class AudioPlayer {
    private val listeners: ArrayList<DecodeListener> = ArrayList()
    private val playListListeners: ArrayList<OnPlayListListener> = ArrayList()
    private val playModeListeners: ArrayList<OnPlayModeListener> = ArrayList()
    private val decoder: AudioDecoder = AudioDecoder()
    private val sourceList: ArrayList<String> = ArrayList()
    private var playList: ArrayList<String> = ArrayList()
    private var mode: PlayMode = SINGLE

    init {
        decoder.startThread()
        decoder.setDecodeListener(object : DecodeListener {
            override fun onReady(decoder: BaseDecoder, format: MediaFormat) {
                for (listener in listeners) {
                    listener.onReady(decoder, format)
                }
            }

            override fun onPause(decoder: BaseDecoder) {
                for (listener in listeners) {
                    listener.onPause(decoder)
                }
            }

            override fun whenContinue(decoder: BaseDecoder) {
                for (listener in listeners) {
                    listener.whenContinue(decoder)
                }
            }

            override fun onNextFrame(decoder: BaseDecoder) {
                for (listener in listeners) {
                    listener.onNextFrame(decoder)
                }
            }

            override fun onFinish(decoder: BaseDecoder) {
                for (listener in listeners) {
                    listener.onFinish(decoder)
                }
                var currentIndex = findCurrentIndex()
                when (mode) {
                    SINGLE -> {
                        decoder.setProgress(0)
                        decoder.goOn()
                    }
                    LIST_LOOP -> {
                        if (currentIndex == -1) return
                        currentIndex += 1
                        if (currentIndex >= playList.size) {
                            currentIndex = 0
                        }
                        decoder.setFilePath(playList[currentIndex], true)
                    }
                    PLAY_IN_ORDER -> {
                        if (currentIndex == -1) return
                        currentIndex += 1
                        if (currentIndex < playList.size) {
                            decoder.setFilePath(playList[currentIndex], true)
                        } else {
                            decoder.setFilePath(playList[0], false)
                        }
                    }
                    RANDOM -> {
                        if (currentIndex == -1) return
                        currentIndex += 1
                        if (currentIndex >= playList.size) {
                            playList.clear()
                            val temp = ArrayList(sourceList)
                            val r = java.util.Random()
                            while (temp.isNotEmpty()) {
                                val i = r.nextInt(temp.size)
                                playList.add(temp[i])
                                temp.removeAt(i)
                            }
                            currentIndex = 0
                            invokePlayListListener()
                        }
                        decoder.setFilePath(playList[currentIndex], true)
                    }
                }
            }
        })
    }

    fun setAudios(list: ArrayList<String>) {
        if (!listEquals(list, sourceList)) {
            sourceList.clear()
            sourceList.addAll(list)
            playList.clear()
            setPlayMode(mode)
        }
    }

    fun setPlayMode(mode: PlayMode) {
        this.mode = mode
        when (mode) {
            SINGLE, LIST_LOOP, PLAY_IN_ORDER -> {
                playList.clear()
                playList.addAll(sourceList)
            }
            RANDOM -> {
                playList.clear()
                val temp = ArrayList(sourceList)
                val r = java.util.Random()
                while (temp.isNotEmpty()) {
                    val i = r.nextInt(temp.size)
                    playList.add(temp[i])
                    temp.removeAt(i)
                }
            }
        }
        invokePlayListListener()
        invokePlayModeListener()
    }

    fun getPlayMode(): PlayMode = mode
    fun getPlayModeString(): String = when (mode) {
        SINGLE -> "单曲循环"
        LIST_LOOP -> "列表循环"
        PLAY_IN_ORDER -> "顺序播放"
        RANDOM -> "随机播放"
    }

    fun togglePlayMode() {
        val v = PlayMode.values()
        val i = v.indexOf(getPlayMode()) + 1
        mode = if (i == v.size) v[0] else v[i]
        setPlayMode(mode)
    }

    fun findCurrentIndex(): Int {
        if (decoder.mediaPath == null) {
            return -1
        }
        return playList.indexOf(decoder.mediaPath)
    }

    /**
     * @param isPlay 立即播放
     */
    fun setFilePath(filePath: String, isPlay: Boolean = true) {
        decoder.setFilePath(filePath, isPlay)
//        try {
//            PlayerTools.play(filePath)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    fun getCurrentFile(): String? {
        return decoder.mediaPath
    }

    fun registerListener(listener: DecodeListener) {
        decoder.invokeListener(listener)
        listeners.add(listener)
    }

    fun unregisterListener(listener: DecodeListener) {
        listeners.remove(listener)
    }

    fun registerPlayListListener(listener: OnPlayListListener) {
        listener.onPlayListChange(playList)
        playListListeners.add(listener)
    }

    fun unregisterPlayListListener(listener: OnPlayListListener) {
        playListListeners.remove(listener)
    }

    fun registerPlayModeListener(listener: OnPlayModeListener, invokeListener: Boolean = true) {
        if (invokeListener) {
            listener.onPlayModeChange(this)
        }
        playModeListeners.add(listener)
    }

    fun unregisterPlayModeListener(listener: OnPlayModeListener) {
        playModeListeners.remove(listener)
    }

    fun invokePlayListListener() {
        for (listener in playListListeners) {
            listener.onPlayListChange(playList)
        }
    }

    fun invokePlayModeListener() {
        for (listener in playModeListeners) {
            listener.onPlayModeChange(this)
        }
    }

    fun previousSong() {
        val i = findCurrentIndex() - 1
        if (i < 0) {
            setFilePath(playList.last(), true)
        } else {
            setFilePath(playList[i], true)
        }
    }

    fun nextSong() {
        val i = findCurrentIndex() + 1
        if (i >= playList.size) {
            setFilePath(playList[0])
        } else {
            setFilePath(playList[i], true)
        }
    }

    fun toggle() {
        if (decoder.state == ThreadState.PAUSE ||
            decoder.state == ThreadState.FINISH
        ) {
            goOn()
        } else if (decoder.state == ThreadState.DECODING) {
            pause()
        }
    }

    fun pause() = decoder.pause()

    fun goOn() = decoder.goOn()

    fun getProgress(): Int = decoder.getProgress().toInt()

    fun setProgress(progress: Int) = decoder.setProgress(progress.toLong())

    fun getDuration(): Int = decoder.getDuration().toInt()

    fun setUseFrameListener(flag: Boolean) {
        decoder.useFrameListener = flag
    }

    companion object {
        fun getInstance(): AudioPlayer {
            synchronized(AudioPlayer::class.java) {
                if (instance == null) {
                    instance = AudioPlayer()
                }
                return instance!!
            }
        }
    }
}

private fun listEquals(list: ArrayList<String>, list2: ArrayList<String>): Boolean {
    if (list.size != list2.size) {
        return false
    }
    for (i in 0 until list.size) {
        if (list[i] != list2[i]) {
            return false
        }
    }
    return true
}
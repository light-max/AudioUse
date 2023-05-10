package com.lifengqiang.audiouse.codec

import android.content.Context
import android.content.Intent
import android.media.MediaFormat
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.lifengqiang.audiouse.R
import com.lifengqiang.audiouse.codec.base.BaseDecoder
import com.lifengqiang.audiouse.codec.base.DecodeListener
import com.lifengqiang.audiouse.ui.play.PlayActivity
import java.io.File

class MiniAudioPlayView : LinearLayout, DecodeListener, OnPlayModeListener {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val hide: TextView
    private val name: TextView
    private val mode: TextView
    private val controllerView: LinearLayout
    private val progress: TextView
    private val seek: SeekBar
    private val duration: TextView
    private val previous: ImageView
    private val controller: ImageView
    private val next: ImageView
    private val look: TextView
    private val player: AudioPlayer

    constructor(context: Context?) :
            this(context, null, 0, 0)

    constructor(context: Context?, attrs: AttributeSet?) :
            this(context, attrs, 0, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        visibility = GONE
        View.inflate(context, R.layout.view_mini_audio_play, this)
        hide = findViewById(R.id.hide)
        name = findViewById(R.id.name)
        mode = findViewById(R.id.mode)
        controllerView = findViewById(R.id.controller_view)
        progress = findViewById(R.id.progress)
        seek = findViewById(R.id.seek)
        duration = findViewById(R.id.duration)
        previous = findViewById(R.id.previous)
        controller = findViewById(R.id.controller)
        next = findViewById(R.id.next)
        look = findViewById(R.id.look)
        player = AudioPlayer.getInstance()
        hide.text = if (controllerView.visibility == VISIBLE) "收起" else "展开"
        hide.setOnClickListener {
            if (controllerView.visibility == GONE) {
                controllerView.visibility = VISIBLE
            } else {
                controllerView.visibility = GONE
            }
            hide.text = if (controllerView.visibility == VISIBLE) "收起" else "展开"
        }
        previous.setOnClickListener { player.previousSong() }
        controller.setOnClickListener { player.toggle() }
        next.setOnClickListener { player.nextSong() }
        mode.setOnClickListener { player.togglePlayMode() }
        look.setOnClickListener {
            context?.startActivity(
                Intent(
                    context,
                    PlayActivity::class.java
                )
            )
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val second = progress / 1000
                    this@MiniAudioPlayView.progress.text =
                        String.format("%02d:%02d", second / 60, second % 60)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                player.setUseFrameListener(false)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player.setUseFrameListener(true)
                player.setProgress(seek.progress)
            }
        })
    }

    fun listenPlayer() {
        player.registerListener(this)
        player.registerPlayModeListener(this)
    }

    fun unListenPlayer() {
        player.unregisterListener(this)
        player.unregisterPlayModeListener(this)
        mode.setOnClickListener(null)
    }

    override fun onPlayModeChange(player: AudioPlayer) {
        mainHandler.post { mode.text = player.getPlayModeString() }
    }

    override fun onReady(decoder: BaseDecoder, format: MediaFormat) {
        mainHandler.post {
            if (visibility != VISIBLE) {
                visibility = VISIBLE
            }
            val second = player.getDuration() / 1000
            name.text = File(decoder.mediaPath!!).name
            duration.text = String.format("%02d:%02d", second / 60, second % 60)
            seek.max = AudioPlayer.getInstance().getDuration()
        }
    }

    override fun onPause(decoder: BaseDecoder) {
        mainHandler.post {
            controller.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    override fun whenContinue(decoder: BaseDecoder) {
        mainHandler.post {
            controller.setImageResource(R.drawable.ic_baseline_pause_24)
        }
    }

    override fun onNextFrame(decoder: BaseDecoder) {
        mainHandler.post {
            val second = player.getProgress() / 1000
            progress.text = String.format("%02d:%02d", second / 60, second % 60)
            seek.progress = player.getProgress()
        }
    }

    override fun onFinish(decoder: BaseDecoder) {
        mainHandler.post {
            controller.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }
}
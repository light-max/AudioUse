package com.lifengqiang.audiouse.ui.play

import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.lifengqiang.audiouse.R
import com.lifengqiang.audiouse.codec.AudioPlayer
import com.lifengqiang.audiouse.codec.OnPlayListListener
import com.lifengqiang.audiouse.codec.OnPlayModeListener
import com.lifengqiang.audiouse.codec.base.BaseDecoder
import com.lifengqiang.audiouse.codec.base.DecodeListener
import java.io.File

class PlayActivity : AppCompatActivity(), DecodeListener, OnPlayListListener, OnPlayModeListener {
    private lateinit var seek: SeekBar
    private lateinit var name: TextView
    private lateinit var duration: TextView;
    private lateinit var progress: TextView
    private lateinit var controller: ImageView
    private lateinit var previous: ImageView
    private lateinit var next: ImageView
    private lateinit var playMode: TextView
    private lateinit var recycler: RecyclerView
    private val adapter: PlayListAdapter = PlayListAdapter()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val player = AudioPlayer.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        startService(Intent(this, ThreadService::class.java))
        supportActionBar?.hide()
        setContentView(R.layout.activity_play)
        seek = findViewById(R.id.seek)
        name = findViewById(R.id.name)
        duration = findViewById(R.id.duration)
        progress = findViewById(R.id.progress)
        controller = findViewById(R.id.controller)
        previous = findViewById(R.id.previous)
        next = findViewById(R.id.next)
        playMode = findViewById(R.id.play_mode)
        recycler = findViewById(R.id.recycler)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler.adapter = adapter
        player.registerListener(this)
        player.registerPlayListListener(this)
        player.registerPlayModeListener(this)
        controller.setOnClickListener { player.toggle() }
        previous.setOnClickListener { player.previousSong() }
        next.setOnClickListener { player.nextSong() }
        playMode.setOnClickListener { player.togglePlayMode() }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val time = progress / 1000
                    this@PlayActivity.progress.text =
                        String.format("%02d:%02d", time / 60, time % 60)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                player.setUseFrameListener(false)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player.setUseFrameListener(true)
                player.setProgress(seekBar.progress)
            }
        })
        adapter.listener = object : PlayListAdapter.OnItemClickListener {
            override fun onItemClick(value: String, position: Int) {
                player.setFilePath(value, true)
            }
        }
        findViewById<View>(R.id.back).setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.unregisterListener(this)
        player.unregisterPlayListListener(this)
        player.unregisterPlayModeListener(this)
    }

    override fun onPlayListChange(list: ArrayList<String>) {
        mainHandler.post {
            adapter.list.clear()
            adapter.list.addAll(list)
            adapter.playIndex = player.findCurrentIndex()
            adapter.notifyDataSetChanged()
        }
    }

    override fun onPlayModeChange(player: AudioPlayer) {
        mainHandler.post { playMode.text = player.getPlayModeString() }
    }

    override fun onReady(decoder: BaseDecoder, format: MediaFormat) {
        mainHandler.post {
            name.text = File(decoder.mediaPath!!).name
            seek.max = player.getDuration()
            val time = player.getDuration() / 1000
            duration.text = String.format("%02d:%02d", time / 60, time % 60)
            adapter.playIndex = player.findCurrentIndex()
            adapter.notifyDataSetChanged()
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
            seek.progress = player.getProgress()
            val time = player.getProgress() / 1000
            progress.text = String.format("%02d:%02d", time / 60, time % 60)
        }
    }

    override fun onFinish(decoder: BaseDecoder) {
        mainHandler.post {
            controller.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }
}
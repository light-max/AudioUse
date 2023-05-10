package com.lifengqiang.audiouse.ui.audiolist

import android.content.Intent
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.lifengqiang.audiouse.R
import com.lifengqiang.audiouse.codec.AudioPlayer
import com.lifengqiang.audiouse.codec.MiniAudioPlayView
import com.lifengqiang.audiouse.codec.ThreadService
import com.lifengqiang.audiouse.codec.base.BaseDecoder
import com.lifengqiang.audiouse.codec.base.DecodeListener
import com.lifengqiang.audiouse.data.Audio
import com.lifengqiang.audiouse.data.AudioData
import com.lifengqiang.audiouse.data.TempSelectFile
import com.lifengqiang.audiouse.ui.search.SearchActivity

class AudioListActivity : AppCompatActivity(), AudioListAdapter.OnActionListener, DecodeListener {
    private var gid = ""
    private var adapter: AudioListAdapter = AudioListAdapter()
    private lateinit var playerView: MiniAudioPlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_audio_list)
        val recycler: RecyclerView = findViewById(R.id.recycler)
        playerView = findViewById(R.id.player_view)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler.adapter = adapter
        adapter.onActionListener = this
        gid = intent.getStringExtra("gid")!!
        updateUI()
    }

    override fun onStart() {
        super.onStart()
        playerView.listenPlayer()
        AudioPlayer.getInstance().registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        playerView.unListenPlayer()
        AudioPlayer.getInstance().unregisterListener(this)
    }

    override fun onPlay(audio: Audio, position: Int) {
        val arrayList = ArrayList<String>()
        for (a in adapter.list) {
            arrayList.add(a.path)
        }
        val player = AudioPlayer.getInstance()
        player.setAudios(arrayList)
        player.setFilePath(audio.path, true)
//        startActivity(Intent(this, PlayActivity::class.java))
        startService(Intent(this, ThreadService::class.java))
    }

    override fun onDelete(audio: Audio, position: Int) {
        AlertDialog.Builder(this)
            .setMessage("你确定要移除吗?")
            .setNegativeButton("取消", null)
            .setPositiveButton("确定") { _, _ ->
                AudioData.getInstance().deleteAudio(gid, audio) {
                    updateUI()
                }
            }.show()
    }

    private fun updateUI() {
        AudioData.getInstance().readAudios(gid) { audios ->
            adapter.list = audios.list
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.audio_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.search -> {
                startActivityForResult(
                    Intent(this, SearchActivity::class.java),
                    SearchActivity.REQUEST_CODE
                )
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SearchActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            TempSelectFile.read { list ->
                AudioData.getInstance().addAudios(gid, list) {
                    updateUI()
                }
            }
        }
    }

    override fun onReady(decoder: BaseDecoder, format: MediaFormat) {
        Handler(Looper.getMainLooper()).post {
            adapter.currentPlayPath = decoder.mediaPath
            adapter.notifyDataSetChanged()
        }
    }

    override fun onPause(decoder: BaseDecoder) {
    }

    override fun whenContinue(decoder: BaseDecoder) {
    }

    override fun onNextFrame(decoder: BaseDecoder) {
    }

    override fun onFinish(decoder: BaseDecoder) {
    }
}
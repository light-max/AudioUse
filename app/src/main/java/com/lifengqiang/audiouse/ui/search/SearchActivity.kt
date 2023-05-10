package com.lifengqiang.audiouse.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.lifengqiang.audiouse.R
import com.lifengqiang.audiouse.data.TempSelectFile
import java.io.File

class SearchActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 0x30
    }

    private var isSearching = false
    private var adapter: SearchAudioAdapter = SearchAudioAdapter()
    private lateinit var mainHandler: Handler
    private lateinit var path: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainHandler = Handler(Looper.getMainLooper())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_search)
        path = findViewById(R.id.path)
        val recycler: RecyclerView = findViewById(R.id.recycler)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler.adapter = adapter
        goSearch()
        val reverse: Button = findViewById(R.id.reverse)
        val post: Button = findViewById(R.id.post)
        reverse.setOnClickListener {
            for (file in adapter.list) {
                file.checked = !file.checked
            }
            adapter.notifyDataSetChanged()
        }
        post.setOnClickListener {
            TempSelectFile.write({
                val list = ArrayList<String>()
                for (file in adapter.list) {
                    if (file.checked) {
                        list.add(file.path)
                    }
                }
                list
            }, {
                setResult(RESULT_OK)
                finish()
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        isSearching = false
    }

    @SuppressLint("SdCardPath")
    private fun goSearch() {
        isSearching = true
        Thread {
            search(File("/sdcard"))
            isSearching = false
            mainHandler.post {
                adapter.notifyDataSetChanged()
            }
        }.start()
        Thread {
            while (isSearching) {
                SystemClock.sleep(300)
                mainHandler.post {
                    adapter.notifyDataSetChanged()
                }
            }
        }.start()
    }

    private fun search(file: File) {
        if (file.parent.startsWith("/sdcard/Android/data/")) {
            return
        }
        if (file.isDirectory) {
            mainHandler.post { path.text = file.path }
            val listFiles = file.listFiles() ?: return
            for (f in listFiles) {
                if (isSearching) {
                    if (!f.name.startsWith(".")) {
                        search(f)
                    }
                } else {
                    break
                }
            }
        } else {
            val name = file.name
            if (name.endsWith(".mp3") ||
                name.endsWith(".cda") ||
                name.endsWith(".wav") ||
                name.endsWith(".m4a")
            ) {
                adapter.list.add(SearchFile(file.name, file.path, false))
            }
        }
    }
}
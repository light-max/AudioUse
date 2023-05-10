package com.lifengqiang.audiouse.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.lifengqiang.audiouse.R
import com.lifengqiang.audiouse.codec.MiniAudioPlayView
import com.lifengqiang.audiouse.data.Group
import com.lifengqiang.audiouse.data.GroupData
import com.lifengqiang.audiouse.ui.audiolist.AudioListActivity
import com.lifengqiang.audiouse.utils.getStorageCard
import com.lifengqiang.audiouse.utils.isStorageCard

class GroupActivity : AppCompatActivity(), GroupAdapter.OnActionCall {
    lateinit var adapter: GroupAdapter
    lateinit var playerView: MiniAudioPlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_group)
        val recycler: RecyclerView = findViewById(R.id.recycler)
        playerView = findViewById(R.id.player_view)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        adapter = GroupAdapter()
        recycler.adapter = adapter
        if (isStorageCard(this)) {
            updateUI()
        } else {
            getStorageCard(this)
        }
        adapter.onActionCall = this
    }

    override fun onStart() {
        super.onStart()
        playerView.listenPlayer()
    }

    override fun onStop() {
        super.onStop()
        playerView.unListenPlayer()
    }

    private fun updateUI() {
        GroupData.getInstance().readGroups { groups ->
            adapter.list = groups.list
            adapter.notifyDataSetChanged()
        }
    }

    override fun onEdit(group: Group, position: Int) {
        val text = EditText(this)
        text.setText(group.name)
        AlertDialog.Builder(this)
            .setMessage("修改分组")
            .setView(text)
            .setNegativeButton("取消", null)
            .setPositiveButton("确定") { _, _ ->
                val value = text.text.toString()
                if (value.trim().isNotEmpty()) {
                    GroupData.getInstance().editGroup(Group(group.id, value)) {
                        updateUI()
                    }
                }
            }.show()
    }

    override fun onDelete(group: Group, position: Int) {
        AlertDialog.Builder(this)
            .setMessage("你确定要删除这个分组？")
            .setNegativeButton("取消", null)
            .setPositiveButton("确定") { _, _ ->
                GroupData.getInstance().deleteGroup(group.id) {
                    updateUI()
                }
            }.show()
    }

    override fun onClick(group: Group, position: Int) {
        val intent = Intent(this, AudioListActivity::class.java)
        intent.putExtra("gid", group.id)
        startActivity(intent)
    }

    private fun addGroup() {
        if (isStorageCard(this)) {
            val text = EditText(this)
            AlertDialog.Builder(this)
                .setMessage("添加分组")
                .setView(text)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    val value = text.text.toString()
                    if (value.trim().isNotEmpty()) {
                        GroupData.getInstance().addGroup(value) {
                            updateUI()
                        }
                    }
                }.show()
        } else {
            getStorageCard(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.add -> {
                addGroup()
                true
            }
            R.id.refresh -> {
                if (isStorageCard(this)) {
                    updateUI()
                } else {
                    getStorageCard(this)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
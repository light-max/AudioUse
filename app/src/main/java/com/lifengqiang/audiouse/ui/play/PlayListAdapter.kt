package com.lifengqiang.audiouse.ui.play

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lifengqiang.audiouse.R
import java.io.File

class PlayListAdapter : RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {
    val list = ArrayList<String>()
    var playIndex = -1
    var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_play_audio, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = File(list[position]).name
        holder.play.visibility = if (playIndex == position) View.VISIBLE else View.INVISIBLE
        holder.itemView.setOnClickListener {
            listener?.onItemClick(list[position], position)
        }
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val play: ImageView = itemView.findViewById(R.id.play)
    }

    interface OnItemClickListener {
        fun onItemClick(value: String, position: Int)
    }
}
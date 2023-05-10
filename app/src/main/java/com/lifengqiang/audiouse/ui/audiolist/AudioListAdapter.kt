package com.lifengqiang.audiouse.ui.audiolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lifengqiang.audiouse.R
import com.lifengqiang.audiouse.data.Audio

class AudioListAdapter : RecyclerView.Adapter<AudioListAdapter.ViewHolder>() {
    var list: ArrayList<Audio> = ArrayList()
    var onActionListener: OnActionListener? = null
    var currentPlayPath: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_audio, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val audio = list[position]
        holder.name.text = audio.name
        holder.path.text = audio.path
        holder.itemView.setOnClickListener {
            val p = holder.adapterPosition
            onActionListener?.onPlay(list[p], p)
        }
        holder.delete.setOnClickListener {
            val p = holder.adapterPosition
            onActionListener?.onDelete(list[p], p)
        }
        holder.play.visibility =
            if (audio.path == currentPlayPath) View.VISIBLE
            else View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val path: TextView = itemView.findViewById(R.id.path)
        val delete: ImageView = itemView.findViewById(R.id.delete)
        val play: ImageView = itemView.findViewById(R.id.play)
    }

    interface OnActionListener {
        fun onPlay(audio: Audio, position: Int)
        fun onDelete(audio: Audio, position: Int)
    }
}
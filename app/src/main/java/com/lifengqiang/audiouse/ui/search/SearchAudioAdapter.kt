package com.lifengqiang.audiouse.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lifengqiang.audiouse.R

class SearchFile(val name: String, val path: String, var checked: Boolean)

class SearchAudioAdapter : RecyclerView.Adapter<SearchAudioAdapter.ViewHolder>() {
    var list: ArrayList<SearchFile> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_audio, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            holder.check.toggle()
        }
        val file = list[position]
        holder.name.text = file.name
        holder.path.text = file.path
        holder.check.isChecked = file.checked
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            list[holder.adapterPosition].checked = isChecked
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val path: TextView = itemView.findViewById(R.id.path)
        val check: CheckBox = itemView.findViewById(R.id.check)
    }
}
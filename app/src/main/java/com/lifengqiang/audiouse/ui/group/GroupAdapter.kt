package com.lifengqiang.audiouse.ui.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lifengqiang.audiouse.R
import com.lifengqiang.audiouse.data.Group

class GroupAdapter() : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {
    var list: MutableList<Group>? = null
    var onActionCall: OnActionCall? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = list!![position].name
        holder.edit.setOnClickListener {
            val p = holder.adapterPosition
            onActionCall?.onEdit(list!![p], p)
        }
        holder.delete.setOnClickListener {
            val p = holder.adapterPosition
            onActionCall?.onDelete(list!![p], p)
        }
        holder.itemView.setOnClickListener {
            val p = holder.adapterPosition
            onActionCall?.onClick(list!![p], p)
        }
    }

    override fun getItemCount(): Int {
        return if (list == null) 0 else list!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val edit: ImageView = itemView.findViewById(R.id.edit)
        val delete: ImageView = itemView.findViewById(R.id.delete)
    }

    interface OnActionCall {
        fun onEdit(group: Group, position: Int)
        fun onDelete(group: Group, position: Int)
        fun onClick(group: Group, position: Int)
    }
}
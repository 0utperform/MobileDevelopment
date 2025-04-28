package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.edittaskdirectory

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R

class EvidenceAdapter(
    private val onAddClick: () -> Unit
) : ListAdapter<Uri, RecyclerView.ViewHolder>(DiffCallback()) {

    private val TYPE_IMAGE = 0
    private val TYPE_ADD_BUTTON = 1

    override fun getItemCount(): Int = currentList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList.size) TYPE_IMAGE else TYPE_ADD_BUTTON
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_IMAGE) {
            val view = inflater.inflate(R.layout.item_evidence_img, parent, false)
            ImageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_add_image_button, parent, false)
            AddViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder && position < currentList.size) {
            Glide.with(holder.itemView)
                .load(currentList[position])
                .into(holder.imageView)
        } else if (holder is AddViewHolder) {
            holder.itemView.setOnClickListener { onAddClick() }
        }
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_evidence)
    }

    inner class AddViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }
}
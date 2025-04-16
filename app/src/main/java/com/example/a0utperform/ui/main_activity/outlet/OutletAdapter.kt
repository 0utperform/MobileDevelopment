package com.example.a0utperform.ui.main_activity.outlet

import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.OutletDetail

class OutletAdapter : RecyclerView.Adapter<OutletAdapter.OutletViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<OutletDetail>() {
        override fun areItemsTheSame(oldItem: OutletDetail, newItem: OutletDetail): Boolean {
            return oldItem.outlet_id == newItem.outlet_id
        }

        override fun areContentsTheSame(oldItem: OutletDetail, newItem: OutletDetail): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<OutletDetail>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outlet, parent, false)
        return OutletViewHolder(view)
    }

    override fun onBindViewHolder(holder: OutletViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class OutletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOutletName = itemView.findViewById<TextView>(R.id.tvOutletName)
        private val tvOutletLocation = itemView.findViewById<TextView>(R.id.tvAddress)
        private val mediaCoverImageView: ImageView = itemView.findViewById(R.id.imgOutlet)
        private val tvManager = itemView.findViewById<TextView>(R.id.tvManager)
        private val tvSize = itemView.findViewById<TextView>(R.id.tvSize)



        fun bind(outlet: OutletDetail) {
            tvOutletName.text = outlet.name ?: "No name"
            tvOutletLocation.text = outlet.location ?: "No location"
            tvManager.text = outlet.manager_name
            tvSize.text = itemView.context.getString(R.string.outlet_size_format, outlet.staff_size.toString())


            Glide.with(itemView.context)
                .load(outlet.image_url)
                .into(mediaCoverImageView)
        }
    }
}
package com.example.a0utperform.ui.main_activity.outlet.outletdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.databinding.ItemStaffBinding

class StaffAdapter : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    private val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<StaffData>() {
        override fun areItemsTheSame(oldItem: StaffData, newItem: StaffData) =
            oldItem.userId == newItem.userId

        override fun areContentsTheSame(oldItem: StaffData, newItem: StaffData) =
            oldItem == newItem
    })

    fun submitList(list: List<StaffData>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class StaffViewHolder(private val binding: ItemStaffBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(staff: StaffData) {
            binding.tvStaffName.text = staff.name
            binding.tvStaffRole.text = staff.email

            Glide.with(binding.root.context)
                .load(staff.avatarUrl ?: R.drawable.placeholder_user)
                .into(binding.profileImage)

        }
    }
}
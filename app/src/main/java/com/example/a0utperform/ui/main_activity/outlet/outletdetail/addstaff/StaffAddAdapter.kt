package com.example.a0utperform.ui.main_activity.outlet.outletdetail.addstaff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.UserWithAssignment
import com.example.a0utperform.databinding.ItemStaffBinding

class StaffAddAdapter(
    private val onToggleAssignment: (UserWithAssignment) -> Unit
) : ListAdapter<UserWithAssignment, StaffAddAdapter.StaffViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StaffViewHolder(private val binding: ItemStaffBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: UserWithAssignment) {
            binding.tvStaffName.text = data.user.name
            binding.tvStaffRole.text = data.user.role ?: "No Role"
            binding.btnToggle.visibility = View.VISIBLE

            Glide.with(binding.root.context)
                .load(data.user.avatarUrl ?: R.drawable.placeholder_user)
                .into(binding.profileImage)

            val iconRes = if (data.isAssigned) R.drawable.ic_remove else R.drawable.ic_add_
            binding.btnToggle.setImageResource(iconRes)

            binding.btnToggle.setOnClickListener {
                onToggleAssignment(data)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserWithAssignment>() {
        override fun areItemsTheSame(oldItem: UserWithAssignment, newItem: UserWithAssignment) =
            oldItem.user.userId == newItem.user.userId

        override fun areContentsTheSame(oldItem: UserWithAssignment, newItem: UserWithAssignment) =
            oldItem == newItem
    }
}
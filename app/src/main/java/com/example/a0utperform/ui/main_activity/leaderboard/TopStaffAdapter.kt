package com.example.a0utperform.ui.main_activity.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TopStaffItem
import com.example.a0utperform.databinding.ItemTeamMemberBinding

class TopStaffAdapter : RecyclerView.Adapter<TopStaffAdapter.StaffViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<TopStaffItem>() {
        override fun areItemsTheSame(oldItem: TopStaffItem, newItem: TopStaffItem): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: TopStaffItem, newItem: TopStaffItem): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<TopStaffItem>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class StaffViewHolder(private val binding: ItemTeamMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(staff: TopStaffItem) {
            binding.checkbox.visibility = View.GONE
            binding.tvCompletionRate.visibility = View.VISIBLE
            binding.tvStaffName.text = staff.name
            binding.tvStaffRole.text = staff.role
            binding.tvCompletionRate.text = binding.root.context.getString(
                R.string.formatted_completion_rate,
                staff.completionRate
            )

            Glide.with(binding.root.context)
                .load(staff.profileUrl?.ifEmpty { R.drawable.placeholder_user })
                .placeholder(R.drawable.placeholder_user)
                .into(binding.profileImage)


        }
    }
}

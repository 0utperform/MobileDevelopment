package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.createtaskactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.StaffData
import com.example.a0utperform.databinding.ItemStaffBinding
import com.example.a0utperform.databinding.ItemTeamMemberBinding

class TeamMemberAdapter(
    private var members: List<StaffData>,
    private val onChecked: (String, Boolean) -> Unit
) : RecyclerView.Adapter<TeamMemberAdapter.ViewHolder>() {

    private val selectedIds = mutableSetOf<String>()

    inner class ViewHolder(val binding: ItemTeamMemberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = members.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = members[position]
        holder.binding.tvStaffName.text = user.name
        holder.binding.tvStaffRole.text = user.role

        Glide.with(holder.binding.root.context)
            .load(user.avatarUrl ?: R.drawable.placeholder_user)
            .into(holder.binding.profileImage)


        holder.binding.checkbox.setOnCheckedChangeListener(null)
        holder.binding.checkbox.isChecked = selectedIds.contains(user.userId)
        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onChecked(user.userId, isChecked)
            if (isChecked) selectedIds.add(user.userId) else selectedIds.remove(user.userId)
        }
    }

    fun updateData(newList: List<StaffData>, preSelected: Set<String>) {
        members = newList
        selectedIds.clear()
        selectedIds.addAll(preSelected)
        notifyDataSetChanged()
    }
}

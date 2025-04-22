package com.example.a0utperform.ui.main_activity.outlet.outletdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.databinding.ItemTeamBinding

class TeamAdapter(private val onTeamClick: (TeamDetail) -> Unit) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<TeamDetail>() {
        override fun areItemsTheSame(oldItem: TeamDetail, newItem: TeamDetail): Boolean {
            return oldItem.team_id == newItem.team_id
        }

        override fun areContentsTheSame(oldItem: TeamDetail, newItem: TeamDetail): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<TeamDetail>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val binding = ItemTeamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeamViewHolder(binding)
    }
    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class TeamViewHolder(private val binding: ItemTeamBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(team: TeamDetail) {
            binding.tvTeamName.text = team.name
            binding.tvTeamSize.text = binding.root.context.getString(R.string.size_format, team.staffSize)
            binding.tvTeamDescription.text = binding.root.context.getString(R.string.description_format, team.description)

            Glide.with(binding.root.context)
                .load(team.img_url ?: R.drawable.placeholder_user)
                .into(binding.imgTeam)

            binding.root.setOnClickListener {
                onTeamClick(team)
            }
        }
    }
}
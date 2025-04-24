package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.databinding.ItemTaskBinding


class TaskAdapter(
    private val onClick: (TaskData) -> Unit
) : ListAdapter<TaskData, TaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TaskData>() {
            override fun areItemsTheSame(oldItem: TaskData, newItem: TaskData): Boolean =
                oldItem.task_id == newItem.task_id

            override fun areContentsTheSame(oldItem: TaskData, newItem: TaskData): Boolean =
                oldItem == newItem
        }
    }

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskData) {
            binding.tvTaskName.text = task.title
            binding.root.setOnClickListener { onClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

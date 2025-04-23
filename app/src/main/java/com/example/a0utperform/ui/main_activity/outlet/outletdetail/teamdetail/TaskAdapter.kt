package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.databinding.ItemTaskBinding


class TaskAdapter(
    private var userRole: String,
    private var submissionProgress: Map<String, Int>,
    private val onManagerClick: (TaskData) -> Unit,
    private val onStaffClick: (TaskData, Int) -> Unit
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

            binding.root.setOnClickListener {
                if (userRole.equals("manager", ignoreCase = true)) {
                    onManagerClick(task)
                } else {
                    val currentSubmissions = submissionProgress[task.task_id] ?: 0
                    val maxPerDay = task.submission_per_day ?: 0 // default to 1 or another sensible value
                    if (currentSubmissions < maxPerDay) {
                        onStaffClick(task, currentSubmissions + 1) // increment for display like 2/3
                    } else {
                        Toast.makeText(
                            binding.root.context,
                            "Task already completed today",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
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
    fun submitList(role: String, submissionMap: Map<String, Int>) {
        this.userRole = role
        this.submissionProgress = submissionMap
        submitList(currentList) // Triggers only if currentList is different
    }
}

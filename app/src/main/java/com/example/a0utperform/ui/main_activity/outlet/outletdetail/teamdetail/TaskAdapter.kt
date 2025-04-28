package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.databinding.ItemTaskBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.edittaskdirectory.EditTaskActivity
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.viewtaskactivity.ViewTaskActivity
import kotlinx.serialization.json.Json


class TaskAdapter(
    private val context: Context,
    private val role: String,
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
            binding.tvTaskCompletionLabel.text =  itemView.context.getString(
                R.string.formatted_completion,
                task.completedSubmissions.toString(),
                task.totalTargetSubmissions.toString()
            )
            Glide.with(context)
                .load(task.img_url)
                .into(binding.taskImage)
            binding.root.setOnClickListener {
                val intent = if (role == "Staff") {
                    Intent(context, EditTaskActivity::class.java)
                } else {
                    Intent(context, ViewTaskActivity::class.java)
                }
                intent.putExtra("TASK_ID", task.task_id)
                intent.putExtra("TASK_DETAIL_JSON", Json.encodeToString(task))
                intent.putExtra("COMPLETED_SUBMISSIONS", task.completedSubmissions)
                intent.putExtra("TOTAL_TARGET_SUBMISSIONS", task.totalTargetSubmissions)
                context.startActivity(intent)
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
}

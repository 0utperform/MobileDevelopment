package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.viewtaskactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TaskData
import com.example.a0utperform.databinding.EditTaskActivityBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.edittaskdirectory.EvidenceAdapter

class ViewTaskAdapter : ListAdapter<TaskData, ViewTaskAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TaskData>() {
            override fun areItemsTheSame(oldItem: TaskData, newItem: TaskData) = oldItem.task_id == newItem.task_id
            override fun areContentsTheSame(oldItem: TaskData, newItem: TaskData) = oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: EditTaskActivityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskData) {
            binding.taskTitle.text = task.title
            binding.tvTaskDescription.text = task.description

            Glide.with(binding.root.context)
                .load(task.img_url)
                .into(binding.imgTask)

            binding.root.findViewById<TextView>(R.id.formatted_progress).text =
                binding.root.context.getString(
                    R.string.formatted_completion,
                    task.completedSubmissions.toString(),
                    task.totalTargetSubmissions.toString()
                )

            // Make EditText read-only
            binding.etTaskDescription.isEnabled = false
            binding.etTaskDescription.setText(task.description)

            // Hide Submit button
            binding.btnSubmitTask.visibility = View.GONE

            // Evidence RecyclerView (you can fill this if you have image evidence)
            val dummyAdapter = EvidenceAdapter(
                onAddClick = {}
            )
            binding.rvEvidenceImages.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvEvidenceImages.adapter = dummyAdapter
            dummyAdapter.submitList(emptyList()) // or actual evidence images if you have
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EditTaskActivityBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
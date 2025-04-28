package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.viewtaskactivity

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.data.model.SubmissionWithEvidence
import com.example.a0utperform.databinding.ItemviewtaskBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.edittaskdirectory.EvidenceAdapter

class SubmissionAdapter : ListAdapter<SubmissionWithEvidence, SubmissionAdapter.SubmissionViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SubmissionWithEvidence>() {
            override fun areItemsTheSame(oldItem: SubmissionWithEvidence, newItem: SubmissionWithEvidence) =
                oldItem.submission.submission_id == newItem.submission.submission_id

            override fun areContentsTheSame(oldItem: SubmissionWithEvidence, newItem: SubmissionWithEvidence) =
                oldItem == newItem
        }
    }

    inner class SubmissionViewHolder(val binding: ItemviewtaskBinding) : RecyclerView.ViewHolder(binding.root) {

        private val evidenceAdapter = ViewEvidenceAdapter() // <-- create ONCE here

        init {
            binding.rvEvidenceImages.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvEvidenceImages.adapter = evidenceAdapter
        }

        fun bind(data: SubmissionWithEvidence) {
            binding.etTaskDescription.text = data.submission.description


            Log.d("ViewEvidenceAdapter", "Evidence list size: ${data.evidenceList.size}")
            evidenceAdapter.submitList(data.evidenceList)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemviewtaskBinding.inflate(inflater, parent, false)
        return SubmissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.viewtaskactivity

import android.R
import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.data.model.TaskEvidence
import com.example.a0utperform.databinding.ItemEvidenceImgBinding

class ViewEvidenceAdapter : ListAdapter<TaskEvidence, ViewEvidenceAdapter.EvidenceViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TaskEvidence>() {
            override fun areItemsTheSame(oldItem: TaskEvidence, newItem: TaskEvidence) =
                oldItem.evidence_id == newItem.evidence_id

            override fun areContentsTheSame(oldItem: TaskEvidence, newItem: TaskEvidence) =
                oldItem == newItem
        }
    }

    inner class EvidenceViewHolder(val binding: ItemEvidenceImgBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(evidence: TaskEvidence) {
            Log.d("ViewEvidenceAdapter", "Image URL: ${evidence.file_url}")
            Glide.with(binding.root.context)
                .load(evidence.file_url)
                .override(200, 200)
                .into(binding.ivEvidence)
            binding.ivEvidence.setOnClickListener {
                val context = binding.root.context
                val dialog = Dialog(context, R.style.Theme_Black_NoTitleBar_Fullscreen)
                val imageView = ImageView(context)

                Glide.with(context)
                    .load(evidence.file_url)
                    .fitCenter()
                    .into(imageView)

                dialog.setContentView(imageView)
                imageView.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvidenceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEvidenceImgBinding.inflate(inflater, parent, false)
        return EvidenceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EvidenceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}
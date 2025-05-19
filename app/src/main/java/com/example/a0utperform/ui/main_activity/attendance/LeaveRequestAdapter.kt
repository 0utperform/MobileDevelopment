package com.example.a0utperform.ui.main_activity.attendance

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.data.model.LeaveRequest
import com.example.a0utperform.databinding.ItemLeaveBinding
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LeaveRequestAdapter(
    private val onItemClick: (LeaveRequest) -> Unit
) : ListAdapter<LeaveRequest, LeaveRequestAdapter.LeaveViewHolder>(DiffCallback()) {

    inner class LeaveViewHolder(private val binding: ItemLeaveBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: LeaveRequest) {
            val startDateTime = ZonedDateTime.parse(item.startDate)
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            binding.tvClockIn.text = startDateTime.format(timeFormatter)
            val endDateTime = ZonedDateTime.parse(item.endDate)
            binding.tvClockOut.text = endDateTime.format(timeFormatter)

            val duration = Duration.between(startDateTime, endDateTime)
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60

            // Format to "Xh Ym" or just "Xh" if no minutes
            val totalHoursText = if (minutes > 0) {
                "${hours}: ${minutes}"
            } else {
                "${hours} : 00"
            }
            binding.tvTotalHrs.text = totalHoursText

            binding.ctgType.text = item.type
            val parsedDate = OffsetDateTime.parse(item.startDate)
            val month = parsedDate.format(DateTimeFormatter.ofPattern("MMM"))
            val day = parsedDate.format(DateTimeFormatter.ofPattern("dd"))
            binding.tvDate.text = day
            binding.tvMonth.text = month

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val binding = ItemLeaveBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeaveViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<LeaveRequest>() {
        override fun areItemsTheSame(oldItem: LeaveRequest, newItem: LeaveRequest) =
            oldItem.requestId == newItem.requestId

        override fun areContentsTheSame(oldItem: LeaveRequest, newItem: LeaveRequest) =
            oldItem == newItem
    }
}

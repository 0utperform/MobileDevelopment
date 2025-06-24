package com.example.a0utperform.ui.main_activity.attendance

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.R
import com.example.a0utperform.data.model.CalendarDay
import com.example.a0utperform.databinding.DayItemLayoutBinding
import java.util.Locale

class CalendarAdapter : ListAdapter<CalendarDay, CalendarAdapter.DayViewHolder>(CalendarDiffCallback()) {

    class DayViewHolder(val binding: DayItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DayItemLayoutBinding.inflate(inflater, parent, false)
        return DayViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = getItem(position)
        val dayNumber = String.format(Locale.getDefault(), "%d", day.date.dayOfMonth)
        holder.binding.dayText.text = dayNumber
        val context = holder.itemView.context

        when (day.status) {
            "completed" -> {
                holder.binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.brandBlue)
                )
            }
            "absent" -> {
                holder.binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.dark_red)
                )
            }
            else -> {
                holder.binding.root
            }
        }
    }

    class CalendarDiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem == newItem
        }
    }
}

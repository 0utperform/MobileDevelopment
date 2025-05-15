package com.example.a0utperform.ui.main_activity.attendance

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.a0utperform.data.model.CalendarDay
import com.example.a0utperform.databinding.FragmentAttendanceBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels()
    private lateinit var adapter: CalendarAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?) : View? {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)

        setupCalendar()

        binding.btnPrevMonth.setOnClickListener {
            viewModel.changeMonth(-1)
        }
        binding.btnNextMonth.setOnClickListener {
            viewModel.changeMonth(1)
        }

        observeViewModel()

        return binding.root
    }

    private fun setupCalendar() {
        adapter = CalendarAdapter()
        binding.calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
        binding.calendarRecyclerView.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeViewModel() {
        viewModel.selectedMonth.observe(viewLifecycleOwner) { month ->
            binding.tvMonthYear.text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            viewModel.fetchAttendance()
        }

        viewModel.attendanceMap.observe(viewLifecycleOwner) { attendance ->
            val days = generateCalendarDays(viewModel.selectedMonth.value!!, attendance)
            adapter.submitList(days)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateCalendarDays(month: YearMonth, map: Map<LocalDate, String>): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        var current = month.atDay(1)
        val end = month.atEndOfMonth()

        while (!current.isAfter(end)) {
            val status = map[current]
            days.add(CalendarDay(current, status))
            current = current.plusDays(1)
        }

        return days
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

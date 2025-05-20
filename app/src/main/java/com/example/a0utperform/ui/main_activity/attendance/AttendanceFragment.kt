package com.example.a0utperform.ui.main_activity.attendance

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.data.model.CalendarDay
import com.example.a0utperform.data.model.LeaveRequest
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
    private lateinit var leaveRequestAdapter: LeaveRequestAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?) : View? {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)

        setupCalendar()
        setupRecyclerView(binding.leaveRequestRecyclerView)
        observeData()
        binding.btnPrevMonth.setOnClickListener {
            viewModel.changeMonth(-1)
        }
        binding.btnNextMonth.setOnClickListener {
            viewModel.changeMonth(1)
        }

        observeViewModel()

        return binding.root
    }

    private fun setupRecyclerView(rv: RecyclerView) {
        leaveRequestAdapter = LeaveRequestAdapter { request ->
            handleLeaveRequestClick(request)
        }
        binding.leaveRequestRecyclerView.layoutManager = LinearLayoutManager(context)
        rv.adapter = leaveRequestAdapter
    }

    private fun observeData() {
        lifecycleScope.launchWhenStarted {
            viewModel.leaveRequests.collect { requests ->
                leaveRequestAdapter.submitList(requests)
            }
        }
    }

    private fun handleLeaveRequestClick(request: LeaveRequest) {
        val role = viewModel.userRole.value
        val context = requireContext()

        if (role == "Manager" && request.status == "Progress") {
            val intent = Intent(context, EditLeaveRequestActivity::class.java)
            intent.putExtra("leave_requests", request)
            startActivity(intent)
        } else {
            val intent = Intent(context, DetailLeaveRequestActivitiy::class.java)
            intent.putExtra("leave_requests", request)
            startActivity(intent)
        }
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
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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

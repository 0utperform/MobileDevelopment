package com.example.a0utperform.ui.main_activity.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.databinding.FragmentDashboardBinding
import com.example.a0utperform.databinding.FragmentProfileBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.TeamAdapter
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.DetailTeamActivity
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.TaskAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var teamAdapter: TeamAdapter
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var binding: FragmentDashboardBinding

    private var taskListCollectorJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        teamAdapter = TeamAdapter { team -> navigateToTeamDetail(team) }
        setupRecyclerView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAttendanceObservers()
        setupAttendanceButtons()
        dashboardViewModel.clockInState.observe(viewLifecycleOwner) { showClockIn ->
            dashboardViewModel.checkInitialClockInState()
            when (showClockIn) {
                true -> {
                    binding.btnClockIn.visibility = View.VISIBLE
                    binding.btnClockOut.visibility = View.GONE
                }
                false -> {
                    binding.btnClockIn.visibility = View.GONE
                    binding.btnClockOut.visibility = View.VISIBLE
                }
                null -> {
                    binding.btnClockIn.visibility = View.GONE
                    binding.btnClockOut.visibility = View.GONE
                }
            }
        }
        binding.availableTask.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            dashboardViewModel.getUserRole().collect { role ->
                if (!role.isNullOrEmpty()) {
                    taskAdapter = TaskAdapter(requireContext(), role)
                    binding.task.adapter = taskAdapter
                    observeTaskList(role)
                }
            }
        }

        dashboardViewModel.attendanceStats.observe(viewLifecycleOwner) { stats ->
            binding.percent.text = String.format(Locale.US, "%.0f%%", stats.percentage)
        }
        // Call the ViewModel function to get the attendance completion


        updateTimeAndDate()
        dashboardViewModel.userSession.observe(viewLifecycleOwner) { session ->
            session?.let {
                dashboardViewModel.fetchAvatarUrl()
                dashboardViewModel.fetchTeamAssignment()
                dashboardViewModel.fetchAttendanceStats(session.userId)

                binding.profileName.text = it.name.split(" ").firstOrNull() ?: "User"
                binding.profileRole.text = it.role ?: "Role"
            }
        }


        dashboardViewModel.avatarUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .into(binding.profileImage)
        }

        // Observe team assignments
        dashboardViewModel.teamAssignment.observe(viewLifecycleOwner) { teamList ->
            Log.d("DashboardFragment", "Fetched teamList: $teamList")

            teamAdapter.submitList(teamList)
            teamList.firstOrNull()?.team_id?.let { teamId ->
                Log.d("DashboardFragment", "Fetching tasks for teamId: $teamId")
                dashboardViewModel.fetchTasksWithProgress(teamId)
            }
        }



    }

    private fun setupRecyclerView() {
        binding.team.layoutManager = LinearLayoutManager(requireContext())
        binding.team.adapter = teamAdapter
        binding.task.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeTaskList(role: String) {
        taskListCollectorJob?.cancel()

        taskListCollectorJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            dashboardViewModel.taskList.observe(viewLifecycleOwner) { tasks ->
                val filteredTasks = tasks?.filter {
                    it.status == "Progress" && it.completedSubmissions < it.totalTargetSubmissions
                }.orEmpty()

                taskAdapter.submitList(filteredTasks)

                if (filteredTasks.isEmpty()) {
                    binding.taskLabel.visibility = View.GONE
                    binding.task.visibility = View.GONE
                    binding.availableTask.visibility = View.VISIBLE
                    binding.availableTask.text = getString(R.string.formatted_task, "0")
                } else {
                    binding.taskLabel.visibility = View.VISIBLE
                    binding.task.visibility = View.VISIBLE
                    binding.availableTask.visibility = View.VISIBLE
                    binding.availableTask.text = getString(R.string.formatted_task, filteredTasks.size.toString())
                }
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTimeAndDate() {
        val jakartaZone = ZoneId.of("Asia/Jakarta")
        val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formatterDate = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id", "ID"))

        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val currentTime = ZonedDateTime.now(jakartaZone)
                binding.time.text = currentTime.format(formatterTime)
                binding.date.text = currentTime.format(formatterDate)
                delay(1000L) // update every second
            }
        }
    }
    private fun navigateToTeamDetail(team: TeamDetail) {
        val intent = Intent(requireContext(), DetailTeamActivity::class.java)
        val teamJson = Json.encodeToString(team)
        intent.putExtra("TEAM_DETAIL_JSON", teamJson)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()


        taskListCollectorJob?.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAttendanceObservers() {


        // Observe attendance times
        dashboardViewModel.clockInTime.observe(viewLifecycleOwner) { time ->
            binding.tvClockIn.text = if (time.isNullOrEmpty()) null else time
        }
        dashboardViewModel.clockOutTime.observe(viewLifecycleOwner) { time ->
            binding.tvClockOut.text = if (time.isNullOrEmpty()) null else time
        }

        dashboardViewModel.totalHours.observe(viewLifecycleOwner) { hours ->
            binding.tvTotalHrs.text = hours
        }

        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.dashboardLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        dashboardViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                // Show error message, e.g., using a Snackbar or Toast
                Log.e("DashboardFragment", "Attendance error: $error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAttendanceButtons() {
        binding.btnClockIn.setOnClickListener {
            dashboardViewModel.clockIn()
        }

        binding.btnClockOut.setOnClickListener {
            dashboardViewModel.clockOut()
        }
    }


}
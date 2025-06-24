package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.databinding.ActivityDetailTeamBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.StaffAdapter
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.addstaff.ActivityAddStaff
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.addteam.ActivityAddTeam
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.addstaffteam.ActivityAddStaffTeam
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.createtaskactivity.CreateTaskActivity
import com.example.a0utperform.utils.formatToReadableDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@AndroidEntryPoint
class DetailTeamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTeamBinding
    private val teamViewModel: DetailTeamViewModel by viewModels()
    private val staffAdapter = StaffAdapter()
    private lateinit var taskAdapter: TaskAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val teamJson = intent.getStringExtra("TEAM_DETAIL_JSON")
        val outletJson = intent.getStringExtra("OUTLET_DETAIL_JSON")
        val teamDetail = teamJson?.let { Json.decodeFromString<TeamDetail>(it) }

        binding.rvStaff.adapter = staffAdapter
        binding.rvStaff.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        lifecycleScope.launch {
            teamViewModel.getUserRole().collect { role ->
                if (!role.isNullOrEmpty()) {
                    taskAdapter = TaskAdapter(this@DetailTeamActivity, role)
                    binding.rvTasks.adapter = taskAdapter

                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    var fromDate: LocalDate? = null
                    var untilDate: LocalDate? = null
                    var currentSearchQuery: String? = null


                    fun applySearchAndFilter(titleQuery: String?, from: LocalDate?, until: LocalDate?) {
                        val originalTasks = teamViewModel.taskList.value.orEmpty()

                        val filtered = originalTasks.filter { task ->
                            val matchesTitle = task.title.contains(titleQuery.orEmpty(), ignoreCase = true)

                            val created = task.createdAt?.substring(0, 10)
                            val createdDate = try {
                                LocalDate.parse(created)
                            } catch (e: Exception) {
                                null
                            }

                            val matchesDate = createdDate != null &&
                                    (from == null || !createdDate.isBefore(from)) &&
                                    (until == null || !createdDate.isAfter(until))

                            matchesTitle && matchesDate
                        }

                        taskAdapter.submitList(filtered)
                    }

                    fun showDatePicker(callback: (LocalDate) -> Unit) {
                        val now = LocalDate.now()
                        val datePicker = DatePickerDialog(
                            this@DetailTeamActivity,
                            { _, year, month, day ->
                                callback(LocalDate.of(year, month + 1, day))
                            },
                            now.year, now.monthValue - 1, now.dayOfMonth
                        )
                        datePicker.show()
                    }

                    binding.tvStartDate.setOnClickListener {
                        showDatePicker {
                            fromDate = it
                            binding.tvStartDate.text = "From: ${it.format(dateFormatter)}"
                        }
                    }

                    binding.tvEndDate.setOnClickListener {
                        showDatePicker {
                            untilDate = it
                            binding.tvEndDate.text = "Until: ${it.format(dateFormatter)}"
                        }
                    }

                    binding.btnApplyFilter.setOnClickListener {
                        applySearchAndFilter(currentSearchQuery, fromDate, untilDate)
                        binding.btnApplyFilter.visibility = View.GONE
                        binding.btnClearFilter.visibility = View.VISIBLE
                    }

                    binding.btnClearFilter.setOnClickListener {
                        fromDate = null
                        untilDate = null
                        binding.tvStartDate.text = "From"
                        binding.tvEndDate.text = "Until"
                        applySearchAndFilter(currentSearchQuery, null, null)
                        binding.btnApplyFilter.visibility = View.VISIBLE
                        binding.btnClearFilter.visibility = View.GONE
                    }

                    if (role == "Manager") {
                        binding.taskFilterContainer.visibility = View.VISIBLE
                        binding.fabAddTasks.visibility = View.VISIBLE
                        binding.fabAddStaff.visibility = View.VISIBLE
                        binding.searchView.visibility = View.VISIBLE

                        // 🔍 SearchView behavior
                        binding.searchView.setOnClickListener {
                            binding.searchView.isIconified = false
                            binding.searchView.requestFocus()
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
                        }

                        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean = true
                            override fun onQueryTextChange(newText: String?): Boolean {
                                currentSearchQuery = newText
                                applySearchAndFilter(currentSearchQuery, fromDate, untilDate)
                                return true
                            }
                        })

                        binding.fabAddTasks.setOnClickListener {
                            val intent = Intent(this@DetailTeamActivity, CreateTaskActivity::class.java)
                            intent.putExtra("TEAM_DETAIL_JSON", teamJson)
                            startActivity(intent)
                        }

                        binding.fabAddStaff.setOnClickListener {
                            val intent = Intent(this@DetailTeamActivity, ActivityAddStaffTeam::class.java)
                            intent.putExtra("TEAM_DETAIL_JSON", teamJson)
                            intent.putExtra("OUTLET_DETAIL_JSON", outletJson)
                            startActivity(intent)
                        }

                        // Default button visibility
                        binding.btnApplyFilter.visibility = View.VISIBLE
                        binding.btnClearFilter.visibility = View.GONE
                    } else {
                        binding.fabAddTasks.visibility = View.GONE
                        binding.fabAddStaff.visibility = View.GONE
                        binding.searchView.visibility = View.GONE
                        binding.taskFilterContainer.visibility = View.GONE
                    }

                    // Observe tasks
                    teamViewModel.taskList.observe(this@DetailTeamActivity) { tasks ->
                        if (tasks.isNullOrEmpty()) {
                            binding.rvTasks.visibility = View.GONE
                        } else {
                            val filteredTasks = if (role == "Staff") {
                                tasks.filter {
                                    it.status == "Progress" && it.completedSubmissions < it.totalTargetSubmissions
                                }
                            } else {
                                tasks
                            }
                            taskAdapter.submitList(filteredTasks)
                            binding.tasksLabel.visibility = View.VISIBLE
                            binding.rvTasks.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        // Team info
        teamDetail?.let {
            teamViewModel.setTeamDetail(it)
            it.team_id?.let { id -> teamViewModel.fetchTasksWithProgress(id) }
        }

        // Staff
        teamViewModel.staffList.observe(this) { staff ->
            if (staff.isNullOrEmpty()) {
                binding.rvStaff.visibility = View.GONE
            } else {
                staffAdapter.submitList(staff)
                binding.rvStaff.visibility = View.VISIBLE
            }
        }

        // Loading state
        teamViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Team metadata
        teamViewModel.team.observe(this) { team ->
            binding.tvTeamName.text = team.name
            binding.tvTeamId.text = getString(R.string.team_id_format, team.team_id)
            binding.tvCreatedAt.text = getString(R.string.created_format, team.createdAt?.formatToReadableDate())
            binding.tvStaffSize.text = getString(R.string.size_format, team.staffSize)
            binding.tvTeamDescription.text = getString(R.string.description_format, team.description)

            Glide.with(this)
                .load(team.img_url ?: R.drawable.placeholder_user)
                .into(binding.imgTeam)
        }
    }
}

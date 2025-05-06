package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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


                    if (role == "Manager") {
                        binding.fabAddTasks.visibility = View.VISIBLE
                        binding.fabAddStaff.visibility = View.VISIBLE
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
                    } else {
                        binding.fabAddTasks.visibility = View.GONE
                        binding.fabAddStaff.visibility = View.GONE
                    }

                    // Observe task list AFTER adapter is initialized
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




        teamDetail?.let {
            teamViewModel.setTeamDetail(it)
            it.team_id?.let { it1 -> teamViewModel.fetchTasksWithProgress(it1) }
        }

        teamViewModel.staffList.observe(this) { staff ->
            if (staff.isNullOrEmpty()) {
                binding.rvStaff.visibility = View.GONE
            } else {
                staffAdapter.submitList(staff)
                binding.rvStaff.visibility = View.VISIBLE
            }
        }
        teamViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        teamViewModel.team.observe(this) { team ->
            binding.tvTeamName.text = team.name
            binding.tvTeamId.text = getString(R.string.team_id_format, team.team_id)
            binding.tvCreatedAt.text = getString(R.string.created_format,
                team.createdAt?.formatToReadableDate()
            )
            binding.tvStaffSize.text = getString(R.string.size_format, team.staffSize)
            binding.tvTeamDescription.text = getString(R.string.description_format, team.description)

            Glide.with(this)
                .load(team.img_url ?: R.drawable.placeholder_user)
                .into(binding.imgTeam)
        }
    }
}
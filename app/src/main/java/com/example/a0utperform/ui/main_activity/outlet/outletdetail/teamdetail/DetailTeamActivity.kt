package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.databinding.ActivityDetailTeamBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.StaffAdapter
import com.example.a0utperform.utils.formatToReadableDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class DetailTeamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTeamBinding
    private val teamViewModel: DetailTeamViewModel by viewModels()
    private val staffAdapter = StaffAdapter()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvStaff.adapter = staffAdapter
        binding.rvStaff.layoutManager = LinearLayoutManager(this)

        val teamJson = intent.getStringExtra("TEAM_DETAIL_JSON")
        val teamDetail = teamJson?.let { Json.decodeFromString<TeamDetail>(it) }

        teamDetail?.let {
            teamViewModel.setTeamDetail(it)
        }
        teamViewModel.staffList.observe(this) { staff ->
            if (staff.isNullOrEmpty()) {
                binding.rvStaff.visibility = View.GONE
                binding.staffLabel.visibility = View.GONE
            } else {
                staffAdapter.submitList(staff)
                binding.rvStaff.visibility = View.VISIBLE
                binding.staffLabel.visibility = View.VISIBLE
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
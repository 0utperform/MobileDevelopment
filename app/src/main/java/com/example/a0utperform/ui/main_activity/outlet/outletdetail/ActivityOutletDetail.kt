package com.example.a0utperform.ui.main_activity.outlet.outletdetail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.databinding.ActivityOutletDetailBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.addstaff.ActivityAddStaff
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.addteam.ActivityAddTeam
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.DetailTeamActivity
import com.example.a0utperform.utils.formatToReadableDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class ActivityOutletDetail : AppCompatActivity() {
    private lateinit var outletJson: String
    private lateinit var binding: ActivityOutletDetailBinding
    private val outletViewModel: OutletDetailViewModel by viewModels()
    private val adapter = TeamAdapter { team ->
        val intent = Intent(this, DetailTeamActivity::class.java)
        val teamJson = Json.encodeToString(team)
        intent.putExtra("TEAM_DETAIL_JSON", teamJson)
        intent.putExtra("OUTLET_DETAIL_JSON", outletJson)
        startActivity(intent)
    }
    private val staffAdapter = StaffAdapter()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityOutletDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvTeams.layoutManager = LinearLayoutManager(this)
        binding.rvTeams.adapter = adapter

        binding.rvStaff.adapter = staffAdapter
        binding.rvStaff.layoutManager = LinearLayoutManager(this)



        outletJson = intent.getStringExtra("OUTLET_DETAIL_JSON") ?: ""
        val outletDetail = outletJson.takeIf { it.isNotEmpty() }?.let {
            Json.decodeFromString<OutletDetail>(it)
        }

        outletDetail?.let { outlet ->
            binding.tvOutletName.text = outlet.name
            binding.tvManager.text = getString(R.string.outlet_manager_format, outlet.manager_name)
            binding.tvOutletId.text = getString(R.string.outletId_format, outlet.outlet_id)
            binding.tvSize.text = getString(R.string.size_format, outlet.staff_size.toString())
            binding.tvAddress.text = getString(R.string.outlet_address_format, outlet.location)
            binding.tvCreated.text = getString(R.string.created_format, outlet.created_at?.formatToReadableDate())

            Glide.with(this)
                .load(outlet.image_url ?: R.drawable.placeholder_user)
                .into(binding.imgOutlet)

            outletViewModel.fetchTeamsByOutlet(outlet.outlet_id)
            outletViewModel.fetchStaffByOutlet(outlet.outlet_id)
        }

        outletViewModel.getUserRole() // Call this to start collecting

        outletViewModel.userRole.observe(this) { role ->
            if (role.equals("Manager", ignoreCase = true)) {
                binding.fabAddTeam.visibility = View.VISIBLE
                binding.fabAddStaff.visibility = View.VISIBLE
                binding.fabAddTeam.setOnClickListener {
                        val intent = Intent(this, ActivityAddTeam::class.java)
                        intent.putExtra("OUTLET_DETAIL_JSON", outletJson)
                        startActivity(intent)
                }
                binding.fabAddStaff.setOnClickListener {
                    val intent = Intent(this, ActivityAddStaff::class.java)
                    intent.putExtra("OUTLET_DETAIL_JSON", outletJson)
                    startActivity(intent)
                }
            } else {
                binding.fabAddTeam.visibility = View.GONE
                binding.fabAddStaff.visibility = View.GONE
            }
        }
        outletViewModel.staffList.observe(this) { staff ->
            if (staff.isNullOrEmpty()) {
                binding.rvStaff.visibility = View.GONE
            } else {
                staffAdapter.submitList(staff)
                binding.rvStaff.visibility = View.VISIBLE
            }
        }

        outletViewModel.teams.observe(this) { teams ->
            if (teams.isNullOrEmpty()) {
                binding.rvTeams.visibility = View.GONE
            } else {
                adapter.submitList(teams)
                binding.rvTeams.visibility = View.VISIBLE
            }
        }

        outletViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        outletViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
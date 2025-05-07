package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.addstaffteam

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a0utperform.R
import com.example.a0utperform.data.model.OutletData
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.TeamDetail
import com.example.a0utperform.data.model.UserWithAssignment
import com.example.a0utperform.databinding.ActivityAddStaffTeamBinding
import com.example.a0utperform.databinding.ActivityDecideLoginBinding
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.addstaff.StaffAddAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class ActivityAddStaffTeam : AppCompatActivity() {

    private lateinit var binding: ActivityAddStaffTeamBinding
    private val viewModel: AddStaffTeamViewModel by viewModels()
    private lateinit var adapter: StaffAddAdapter
    private var teamId: String? = null
    private var outletId: String? = null
    private var fullUserList: List<UserWithAssignment> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStaffTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val teamJson = intent.getStringExtra("TEAM_DETAIL_JSON")
        val teamDetail = teamJson?.let { Json.decodeFromString<TeamDetail>(it) }
        teamId = teamDetail?.team_id

        val outletJson = intent.getStringExtra("OUTLET_DETAIL_JSON")
        val outletDetail = outletJson?.let { Json.decodeFromString<OutletDetail>(it) }
        outletId = outletDetail?.outlet_id

        if (outletId == null) {
            Toast.makeText(this, "Outlet data is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (teamId == null) {
            Toast.makeText(this, "Team data is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        adapter = StaffAddAdapter { userWithAssignment ->
            toggleTeamAssignment(userWithAssignment)
        }

        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
            binding.searchView.requestFocus()

            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = fullUserList.filter {
                    it.user.name.contains(newText.orEmpty(), ignoreCase = true)
                }
                adapter.submitList(filteredList)
                return true
            }
        })

        binding.rvStaff.layoutManager = LinearLayoutManager(this)
        binding.rvStaff.adapter = adapter

        viewModel.users.observe(this) { userList ->
            fullUserList = userList
            adapter.submitList(userList)
        }

        viewModel.loadUsersWithTeamStatusFilteredByOutlet(teamId!!, outletId!!)
    }

    private fun toggleTeamAssignment(user: UserWithAssignment) {
        if (teamId == null) return

        if (user.isAssigned) {
            viewModel.removeUserFromTeam(user.user.userId, teamId!!) {
                Toast.makeText(this, "${user.user.name} has been removed from team.", Toast.LENGTH_SHORT).show()
                viewModel.loadUsersWithTeamStatusFilteredByOutlet(teamId!!, outletId!!)
            }
        } else {
            viewModel.addUserToTeam(user.user.userId, teamId!!) {
                Toast.makeText(this, "${user.user.name} has been added to team.", Toast.LENGTH_SHORT).show()
                viewModel.loadUsersWithTeamStatusFilteredByOutlet(teamId!!, outletId!!)
            }
        }
    }
}
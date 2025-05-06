package com.example.a0utperform.ui.main_activity.outlet.outletdetail.addstaff

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.data.model.UserWithAssignment
import com.example.a0utperform.databinding.ActivitityAddStaffBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class ActivityAddStaff : AppCompatActivity() {

    private lateinit var binding: ActivitityAddStaffBinding
    private val viewModel: AddStaffViewModel by viewModels()
    private lateinit var adapter: StaffAddAdapter
    private var outletId: String? = null
    private var fullUserList: List<UserWithAssignment> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitityAddStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val outletJson = intent.getStringExtra("OUTLET_DETAIL_JSON")
        val outletDetail = outletJson?.let { Json.decodeFromString<OutletDetail>(it) }
        outletId = outletDetail?.outlet_id

        if (outletId == null) {
            Toast.makeText(this, "Outlet data is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        adapter = StaffAddAdapter { userWithAssignment ->
            toggleAssignment(userWithAssignment)
        }
        binding.rvStaff.layoutManager = LinearLayoutManager(this)
        binding.rvStaff.adapter = adapter

        viewModel.users.observe(this) { userList ->
            fullUserList = userList
            adapter.submitList(userList)
        }

        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
            binding.searchView.requestFocus()

            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = viewModel.users.value?.filter {
                    it.user.name.contains(newText.orEmpty(), ignoreCase = true)
                }.orEmpty()
                adapter.submitList(filtered)
                return true
            }
        })

        if (outletId != null) {
            viewModel.loadUsers(outletId!!)
        }
    }

    private fun toggleAssignment(user: UserWithAssignment) {
        if (user.isAssigned) {
            if (outletId != null) {
                viewModel.removeUserFromOutlet(user.user.userId, outletId!!) {
                    Toast.makeText(this, "${user.user.name} has been unassigned.", Toast.LENGTH_SHORT).show()
                    viewModel.loadUsers(outletId!!)
                }
            }
        } else {
            if (outletId != null) {
                viewModel.addUserToOutlet(user.user.userId, outletId!!) {
                    Toast.makeText(this, "${user.user.name} has been assigned.", Toast.LENGTH_SHORT).show()
                    viewModel.loadUsers(outletId!!)
                }
            }
        }
    }
}

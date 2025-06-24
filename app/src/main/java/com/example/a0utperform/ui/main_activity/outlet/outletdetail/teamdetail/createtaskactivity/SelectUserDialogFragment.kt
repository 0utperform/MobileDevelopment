package com.example.a0utperform.ui.main_activity.outlet.outletdetail.teamdetail.createtaskactivity

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a0utperform.databinding.DialogSelectUsersBinding
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class SelectUserDialogFragment : DialogFragment() {

    private lateinit var binding: DialogSelectUsersBinding
    private val viewModel: SelectUserViewModel by viewModels()

    private lateinit var teamId: String
    private var preselectedUserIds: List<String> = emptyList()
    private var onSelectionConfirmed: ((List<String>) -> Unit)? = null

    private val selectedUserIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teamId = requireArguments().getString("TEAM_ID") ?: ""
        preselectedUserIds = requireArguments().getStringArrayList("SELECTED_IDS") ?: emptyList()
        selectedUserIds.addAll(preselectedUserIds)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSelectUsersBinding.inflate(layoutInflater)

        val adapter = TeamMemberAdapter(emptyList()) { userId, isChecked ->
            if (isChecked) selectedUserIds.add(userId)
            else selectedUserIds.remove(userId)
        }

        binding.rvUsers.layoutManager = LinearLayoutManager(context)
        binding.rvUsers.adapter = adapter

        // Load staff for the team
        viewModel.getStaffByTeam(teamId)
        viewModel.staffList.observe(this) { result ->
            result.onSuccess { list ->
                adapter.updateData(list, selectedUserIds)
            }
            result.onFailure {
                Toast.makeText(context, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Assign Users")
            .setView(binding.root)
            .setPositiveButton("Confirm") { _, _ ->
                onSelectionConfirmed?.invoke(selectedUserIds.toList())
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    companion object {
        fun newInstance(
            teamId: String,
            selectedUserIds: List<String>,
            onConfirm: (List<String>) -> Unit
        ): SelectUserDialogFragment {
            val fragment = SelectUserDialogFragment()
            fragment.arguments = bundleOf(
                "TEAM_ID" to teamId,
                "SELECTED_IDS" to ArrayList(selectedUserIds)
            )
            fragment.onSelectionConfirmed = onConfirm
            return fragment
        }
    }
}

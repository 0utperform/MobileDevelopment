package com.example.a0utperform.ui.main_activity.profile

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.a0utperform.R
import com.example.a0utperform.databinding.FragmentProfileBinding
import com.example.a0utperform.ui.main_activity.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val profileViewModel : ProfileViewModel by viewModels()
    private lateinit var binding : FragmentProfileBinding
    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signOutButton: Button = view.findViewById(R.id.logout_btn)
        val teamText: TextView = view.findViewById(R.id.personal_team)
        val outletText: TextView = view.findViewById(R.id.personal_outlet)

        signOutButton.setOnClickListener {
            mainViewModel.signOut()
        }

        mainViewModel.userSession.observe(viewLifecycleOwner) { session ->
            session?.let {
                profileViewModel.fetchAvatarUrl()
                binding.name.text = it.name.split(" ").firstOrNull() ?: "User"
                binding.role.text = it.role ?: "Role"
                binding.personalAge.text = it.age

                binding.personalName.text = getString(R.string.name_format, it.name ?: "N/A")
                profileViewModel.fetchUserTeamAssignments(it.userId)
                profileViewModel.fetchUserOutletAssignments(it.userId)

            }
        }

        profileViewModel.avatarUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url ?: R.drawable.placeholder_user)
                .into(binding.profileImage)
        }

        profileViewModel.teamDetail.observe(viewLifecycleOwner) { team ->
            teamText.text = getString(R.string.team_format,team?.name ?: "N/A")
        }

        profileViewModel.outletDetail.observe(viewLifecycleOwner) { outlet ->
            outletText.text = getString(R.string.outlet_format,outlet?.name ?: "N/A")
        }

        signOutButton.setOnClickListener {
            mainViewModel.signOut()
        }
    }

}
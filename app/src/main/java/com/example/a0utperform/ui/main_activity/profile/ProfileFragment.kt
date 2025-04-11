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
import com.example.a0utperform.R
import com.example.a0utperform.ui.main_activity.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
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

        return inflater.inflate(R.layout.fragment_profile, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signOutButton: Button = view.findViewById(R.id.logout_btn)
        val teamText: TextView = view.findViewById(R.id.team_format)
        val outletText: TextView = view.findViewById(R.id.outlet_format)
        signOutButton.setOnClickListener {
            mainViewModel.signOut()
        }

        mainViewModel.userSession.observe(viewLifecycleOwner) { session ->
            session?.let {
                mainViewModel.fetchUserAssignments(it.userId)
            }
        }

        mainViewModel.teamDetail.observe(viewLifecycleOwner) { team ->
            teamText.text = getString(R.string.team_format,team?.name ?: "N/A")
        }

        signOutButton.setOnClickListener {
            mainViewModel.signOut()
        }
    }

}
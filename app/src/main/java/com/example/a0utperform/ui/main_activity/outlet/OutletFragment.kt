package com.example.a0utperform.ui.main_activity.outlet

import android.content.Context
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.R
import com.example.a0utperform.data.model.OutletDetail
import com.example.a0utperform.databinding.FragmentOutletBinding
import com.example.a0utperform.databinding.FragmentProfileBinding
import com.example.a0utperform.ui.main_activity.ActivityMain
import com.example.a0utperform.ui.main_activity.outlet.outletdetail.ActivityOutletDetail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class OutletFragment : Fragment(), OutletAdapter.OnOutletClickListener {

    private lateinit var binding : FragmentOutletBinding

    companion object {
        fun newInstance() = OutletFragment()
    }

    private val viewModel: OutletViewModel by viewModels()
    private lateinit var outletAdapter: OutletAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onOutletClick(outletDetail: OutletDetail) {
        val outletJson = Json.encodeToString(outletDetail)
        val detailIntent = Intent(context, ActivityOutletDetail::class.java).apply {
            putExtra("OUTLET_DETAIL_JSON", outletJson)
        }
        startActivity(detailIntent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOutletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getUserRole().collect { role ->
                if (role.equals("Manager", ignoreCase = true)) {
                    binding.searchView.visibility = View.VISIBLE
                    binding.searchView.isIconified = true // collapsed initially

                    // Expand only when user taps it
                    binding.searchView.setOnClickListener {
                        binding.searchView.isIconified = false
                        binding.searchView.requestFocus()

                        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
                    }

                    binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean = true
                        override fun onQueryTextChange(newText: String?): Boolean {
                            val filtered = viewModel.outlets.value?.filter {
                                it.name.contains(newText.orEmpty(), ignoreCase = true)
                            }.orEmpty()
                            outletAdapter.submitList(filtered)
                            return true
                        }
                    })
                } else {
                    binding.searchView.visibility = View.GONE
                }
            }
        }

        val rvOutlets = view.findViewById<RecyclerView>(R.id.rv_outlets)
        rvOutlets.layoutManager = LinearLayoutManager(requireContext())

        outletAdapter = OutletAdapter(listener = this)
        rvOutlets.adapter = outletAdapter


        viewModel.outlets.observe(viewLifecycleOwner) { outletList ->
            outletAdapter.submitList(outletList)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.fetchOutlets()


    }

}
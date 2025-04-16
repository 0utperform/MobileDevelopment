package com.example.a0utperform.ui.main_activity.outlet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a0utperform.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OutletFragment : Fragment() {

    companion object {
        fun newInstance() = OutletFragment()
    }

    private val viewModel: OutletViewModel by viewModels()
    private lateinit var outletAdapter: OutletAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_outlet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvOutlets = view.findViewById<RecyclerView>(R.id.rv_outlets)
        rvOutlets.layoutManager = LinearLayoutManager(requireContext())

        outletAdapter = OutletAdapter()
        rvOutlets.adapter = outletAdapter


        viewModel.outlets.observe(viewLifecycleOwner) { outletList ->
            outletAdapter.submitList(outletList)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.fetchOutlets()
    }
}
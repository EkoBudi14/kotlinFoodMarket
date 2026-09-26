package com.example.foodmarketkotlin.ui.home

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodmarketkotlin.data.model.response.Product
import com.example.foodmarketkotlin.databinding.FragmentHomeBinding
import com.example.foodmarketkotlin.ui.detail.DetailActivity
import com.example.foodmarketkotlin.viewModel.FoodUiState
import com.example.foodmarketkotlin.viewModel.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), HomeAdapter.ItemAdapterCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val foodAdapter by lazy { HomeAdapter(itemAdapterCallback = this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupViewPager()
        observeViewModel()
        fetchFood()
    }

    private fun setupRecyclerView() {
        binding.rcList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = foodAdapter
        }
    }

    private fun setupViewPager() {
        val sectionPagerAdapter = SectionPagerAdapter(childFragmentManager)
        binding.viewPager.adapter = sectionPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun fetchFood() {
        viewModel.fetchFood()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is FoodUiState.Loading -> {
                            Log.d(TAG, "Loading")
                        }
                        is FoodUiState.Success -> {
                            foodAdapter.setData(state.foods.products)
                        }
                        is FoodUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View, data: Product) {
        val intent = Intent(activity, DetailActivity::class.java).apply {
            putExtra("foodResponse", data)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
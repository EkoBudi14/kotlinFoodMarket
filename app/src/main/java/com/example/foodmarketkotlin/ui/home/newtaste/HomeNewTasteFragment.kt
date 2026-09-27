package com.example.foodmarketkotlin.ui.home.newtaste

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmarketkotlin.databinding.FragmentHomeNewTasteBinding
import com.example.foodmarketkotlin.data.model.dummy.HomeVerticalModel
import com.example.foodmarketkotlin.data.model.response.Product
import com.example.foodmarketkotlin.ui.detail.DetailActivity
import com.example.foodmarketkotlin.ui.home.HomeAdapter
import com.example.foodmarketkotlin.viewModel.FoodUiState
import com.example.foodmarketkotlin.viewModel.HomeViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


class HomeNewTasteFragment : Fragment(), HomeNewTasteAdapter.ItemAdapterCallback{

    private var _binding: FragmentHomeNewTasteBinding? = null
    private val binding get() = _binding!!


    private val viewModel: HomeViewModel by viewModels()
    private val foodAdapterVertical by lazy { HomeNewTasteAdapter(itemAdapterCallback = this) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeNewTasteBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        fetchFood()
    }

    private fun setupRecyclerView() {
        binding.rcListVertical.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = foodAdapterVertical
        }
    }

    private fun fetchFood() {
        viewModel.fetchFood()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    state ->
                        when(state) {
                            is FoodUiState.Loading -> {
                                Log.d(TAG, "Loading")
                            }
                            is FoodUiState.Success -> {
                                foodAdapterVertical.setData(state.foods.products)
                            }
                            is FoodUiState.Error -> {
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
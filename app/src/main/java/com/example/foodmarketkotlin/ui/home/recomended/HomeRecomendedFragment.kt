package com.example.foodmarketkotlin.ui.home.recomended

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.foodmarketkotlin.ui.home.newtaste.HomeNewTasteAdapter
import com.example.foodmarketkotlin.ui.home.popular.HomePopularAdapter
import com.example.foodmarketkotlin.ui.home.popular.HomeRecomendedAdapter
import com.example.foodmarketkotlin.viewModel.FoodUiState
import com.example.foodmarketkotlin.viewModel.HomeViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


class HomeRecomendedFragment : Fragment(), HomeRecomendedAdapter.ItemAdapterCallback {

    private var _binding: FragmentHomeNewTasteBinding? = null
    private val binding get() = _binding!!


    private val viewModel: HomeViewModel by viewModels()
    private val foodAdapterVertical by lazy { HomeRecomendedAdapter(itemAdapterCallback = this) }


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
        viewModel.fetchFood()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is FoodUiState.Loading -> { /* opsional: tampilkan progress bar */ }
                        is FoodUiState.Success -> {
                            val popular = state.foods.products.sortedByDescending { it.rating }
                            foodAdapterVertical.setData(popular)
                        }
                        is FoodUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }


    private fun setupRecyclerView() {
        binding.rcListVertical.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = foodAdapterVertical
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
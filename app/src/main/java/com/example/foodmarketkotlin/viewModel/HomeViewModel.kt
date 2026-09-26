package com.example.foodmarketkotlin.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodmarketkotlin.data.model.response.FoodResponse
import com.example.foodmarketkotlin.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FoodUiState {
    object Loading : FoodUiState()
    data class Success(val foods: FoodResponse) : FoodUiState()
    data class Error(val message: String) : FoodUiState()
}

class HomeViewModel(private val repository: FoodRepository = FoodRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<FoodUiState>(FoodUiState.Loading)

    val uiState: StateFlow<FoodUiState> = _uiState

    fun fetchFood() {
        viewModelScope.launch {
            val result = repository.fetchAllFood()
                .onSuccess {
                    _uiState.value = FoodUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = FoodUiState.Error(it.message ?: "Unknown error")
                }
        }
    }
}
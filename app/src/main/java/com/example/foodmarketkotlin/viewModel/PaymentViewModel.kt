package com.example.foodmarketkotlin.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmarketkotlin.data.model.response.CreateTransactionResponse
import com.example.foodmarketkotlin.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val transaction: CreateTransactionResponse) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

class PaymentViewModel(private val repository: PaymentRepository = PaymentRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)

    val uiState: StateFlow<PaymentUiState> = _uiState

    fun checkout(productId: Int, qty: Int = 1) {
        if (_uiState.value is PaymentUiState.Loading) return
        _uiState.value = PaymentUiState.Loading

        viewModelScope.launch {
            repository.createTransaction(productId, qty)
                .onSuccess {
                    _uiState.value = PaymentUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = PaymentUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    // Dipanggil setelah Success/Error ditangani supaya tidak terpicu lagi saat view dibuat ulang.
    fun resetState() {
        _uiState.value = PaymentUiState.Idle
    }
}

package com.example.foodmarketkotlin.data.repository

import com.example.foodmarketkotlin.data.model.request.CreateTransactionRequest
import com.example.foodmarketkotlin.data.model.response.CreateTransactionResponse
import com.example.foodmarketkotlin.data.model.response.ErrorResponse
import com.example.foodmarketkotlin.data.remote.BackendApiService
import com.example.foodmarketkotlin.data.remote.BackendInstance
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class PaymentRepository(
    private val apiService: BackendApiService = BackendInstance.api,
    private val auth: FirebaseAuth = Firebase.auth
) {

    suspend fun createTransaction(productId: Int, qty: Int = 1): Result<CreateTransactionResponse> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Silakan login terlebih dahulu"))

            // Backend memverifikasi Firebase ID token ini, bukan uid dari client.
            val idToken = user.getIdToken(false).await().token
                ?: return Result.failure(Exception("Gagal mengambil token login"))

            val response = apiService.createTransaction(
                "Bearer $idToken",
                CreateTransactionRequest(productId, qty)
            )
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val message = runCatching {
                    Gson().fromJson(response.errorBody()?.string(), ErrorResponse::class.java)?.message
                }.getOrNull()
                Result.failure(Exception(message ?: "Gagal membuat transaksi (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

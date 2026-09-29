package com.example.foodmarketkotlin.data.remote

import com.example.foodmarketkotlin.data.model.request.CreateTransactionRequest
import com.example.foodmarketkotlin.data.model.response.CreateTransactionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface BackendApiService {
    @POST("api/create-transaction")
    suspend fun createTransaction(
        @Header("Authorization") authorization: String,
        @Body body: CreateTransactionRequest
    ): Response<CreateTransactionResponse>
}
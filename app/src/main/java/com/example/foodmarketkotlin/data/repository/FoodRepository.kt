package com.example.foodmarketkotlin.data.repository

import com.example.foodmarketkotlin.data.model.response.FoodResponse
import com.example.foodmarketkotlin.data.remote.FoodApiService
import com.example.foodmarketkotlin.data.remote.FoodInstance

class FoodRepository(private val apiService: FoodApiService = FoodInstance.api) {

    suspend fun fetchAllFood(): Result<FoodResponse> {
        return try {
            val response = apiService.getFood()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
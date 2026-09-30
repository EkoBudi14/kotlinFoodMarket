package com.example.foodmarketkotlin.data.repository

import com.example.foodmarketkotlin.data.model.response.FoodResponse
import com.example.foodmarketkotlin.data.remote.FoodApiService
import com.example.foodmarketkotlin.data.remote.FoodInstance

class FoodRepository(private val apiService: FoodApiService = FoodInstance.api) {

    suspend fun fetchAllFood(): Result<FoodResponse> {
        return try {
            val response = apiService.getFood()
            val foods = response.products.filterNot { it.title in NON_FOOD_TITLES }
            Result.success(response.copy(products = foods, total = foods.size))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        // Produk di kategori groceries dummyjson yang bukan makanan manusia.
        private val NON_FOOD_TITLES = setOf("Cat Food", "Dog Food", "Tissue Paper Box")
    }
}
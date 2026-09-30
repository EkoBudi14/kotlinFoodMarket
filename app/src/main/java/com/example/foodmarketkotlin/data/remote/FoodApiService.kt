package com.example.foodmarketkotlin.data.remote

import com.example.foodmarketkotlin.data.model.response.FoodResponse
import retrofit2.http.GET

interface FoodApiService {
    // Hanya kategori groceries (buah, sayur, daging, minuman). limit=0 = ambil semua.
    @GET("products/category/groceries?limit=0")
    suspend fun getFood(): FoodResponse
}
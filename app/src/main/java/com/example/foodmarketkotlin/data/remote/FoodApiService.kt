package com.example.foodmarketkotlin.data.remote

import com.example.foodmarketkotlin.data.model.response.FoodResponse
import retrofit2.http.GET

interface FoodApiService {
    @GET("/products")
    suspend fun getFood(): FoodResponse
}
package com.example.foodmarketkotlin.data.model.request

import com.google.gson.annotations.SerializedName

data class CreateTransactionRequest(
    @SerializedName("productId") val productId: Int,
    @SerializedName("qty") val qty: Int,
)
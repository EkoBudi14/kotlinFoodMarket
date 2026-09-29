package com.example.foodmarketkotlin.data.model.response

import com.google.gson.annotations.SerializedName

data class CreateTransactionResponse(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("token") val token: String,
    @SerializedName("redirectUrl") val redirectUrl: String,
    @SerializedName("grossAmount") val grossAmount: Long
)

data class ErrorResponse(
    @SerializedName("message") val message: String? = null
)

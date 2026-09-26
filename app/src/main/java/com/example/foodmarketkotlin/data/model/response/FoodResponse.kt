package com.example.foodmarketkotlin.data.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodResponse(
    @SerializedName("products") val products: List<Product> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("skip") val skip: Int = 0,
    @SerializedName("limit") val limit: Int = 0
) : Parcelable

@Parcelize
data class Product(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("discountPercentage") val discountPercentage: Double = 0.0,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("tags") val tags: List<String>? = emptyList(),
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("weight") val weight: Int? = null,
    @SerializedName("dimensions") val dimensions: Dimensions? = null,
    @SerializedName("warrantyInformation") val warrantyInformation: String? = null,
    @SerializedName("shippingInformation") val shippingInformation: String? = null,
    @SerializedName("availabilityStatus") val availabilityStatus: String? = null,
    @SerializedName("reviews") val reviews: List<Review>? = emptyList(),
    @SerializedName("returnPolicy") val returnPolicy: String? = null,
    @SerializedName("minimumOrderQuantity") val minimumOrderQuantity: Int? = null,
    @SerializedName("meta") val meta: Meta? = null,
    @SerializedName("images") val images: List<String>? = emptyList(),
    @SerializedName("thumbnail") val thumbnail: String = ""
) : Parcelable

@Parcelize
data class Dimensions(
    @SerializedName("width") val width: Double = 0.0,
    @SerializedName("height") val height: Double = 0.0,
    @SerializedName("depth") val depth: Double = 0.0
) : Parcelable

@Parcelize
data class Review(
    @SerializedName("rating") val rating: Int = 0,
    @SerializedName("comment") val comment: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("reviewerName") val reviewerName: String = "",
    @SerializedName("reviewerEmail") val reviewerEmail: String = ""
) : Parcelable

@Parcelize
data class Meta(
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("qrCode") val qrCode: String? = null
) : Parcelable

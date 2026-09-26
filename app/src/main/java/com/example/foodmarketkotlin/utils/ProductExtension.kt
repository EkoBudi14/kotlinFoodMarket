package com.example.foodmarketkotlin.utils

import com.example.foodmarketkotlin.data.model.response.Product


fun Product.discountedPrice(): Double =
    price - (price * discountPercentage / 100)
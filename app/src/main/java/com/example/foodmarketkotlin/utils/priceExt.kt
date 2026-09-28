package com.example.foodmarketkotlin.utils

import com.example.foodmarketkotlin.data.model.response.Product
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

const val USD_TO_IDR = 16000

val Product.priceIDR: Long
    get() = (price * USD_TO_IDR).roundToLong()

fun Long.toRupiah(): String =
    "Rp." + NumberFormat.getNumberInstance(Locale("in", "ID")).format(this)
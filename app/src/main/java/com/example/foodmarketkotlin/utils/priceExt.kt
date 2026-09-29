package com.example.foodmarketkotlin.utils

import com.example.foodmarketkotlin.data.model.response.Product
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

// Harus sama dengan USD_TO_IDR / DRIVER_FEE / TAX_PERCENT di backend (api/create-transaction.js).
const val USD_TO_IDR = 16000
const val DRIVER_FEE = 10000L
const val TAX_PERCENT = 10

val Product.priceIDR: Long
    get() = (price * USD_TO_IDR).roundToLong()

fun taxOf(subtotal: Long): Long = (subtotal * TAX_PERCENT / 100.0).roundToLong()

fun totalOf(subtotal: Long): Long = subtotal + DRIVER_FEE + taxOf(subtotal)

fun Long.toRupiah(): String =
    "Rp." + NumberFormat.getNumberInstance(Locale("in", "ID")).format(this)

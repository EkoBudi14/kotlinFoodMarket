package com.example.foodmarketkotlin.data.model.response

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

// Dokumen orders/{orderId} yang ditulis backend (api/create-transaction.js & api/notification.js).
data class Order(
    val id: String,
    val title: String,
    val thumbnail: String?,
    val qty: Long,
    val grossAmount: Long,
    val status: String,
    val createdAt: Timestamp?
) {
    companion object {
        fun from(doc: DocumentSnapshot) = Order(
            id = doc.id,
            title = doc.getString("title").orEmpty(),
            thumbnail = doc.getString("thumbnail"),
            qty = doc.getLong("qty") ?: 1,
            grossAmount = doc.getLong("grossAmount") ?: 0,
            status = doc.getString("status") ?: "PENDING",
            createdAt = doc.getTimestamp("createdAt")
        )
    }
}

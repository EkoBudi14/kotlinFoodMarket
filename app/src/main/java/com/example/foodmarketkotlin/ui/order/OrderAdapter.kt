package com.example.foodmarketkotlin.ui.order

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodmarketkotlin.data.model.response.Order
import com.example.foodmarketkotlin.databinding.ItemOrderBinding
import com.example.foodmarketkotlin.utils.toRupiah
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter : ListAdapter<Order, OrderAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvTitle.text = order.title
            binding.tvInfo.text = "${order.qty} items • ${order.grossAmount.toRupiah()}"
            binding.tvDate.text = order.createdAt?.toDate()?.let { DATE_FORMAT.format(it) }.orEmpty()

            val (label, color) = statusStyle(order.status)
            binding.tvStatus.text = label
            binding.tvStatus.setTextColor(color)

            Glide.with(itemView.context)
                .load(order.thumbnail)
                .into(binding.ivPoster)
        }
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))

        // Nilai status sama dengan mapStatus() di backend (lib/midtrans.js).
        private fun statusStyle(status: String): Pair<String, Int> = when (status) {
            "PAID" -> "Paid" to Color.parseColor("#1ABC9C")
            "PENDING" -> "Waiting payment" to Color.parseColor("#F2994A")
            "EXPIRED" -> "Expired" to Color.parseColor("#D9435E")
            "CANCELLED" -> "Cancelled" to Color.parseColor("#D9435E")
            "REFUNDED" -> "Refunded" to Color.parseColor("#8D92A3")
            else -> status to Color.parseColor("#8D92A3")
        }

        private val DIFF = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
        }
    }
}

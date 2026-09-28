package com.example.foodmarketkotlin.ui.home.popular

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodmarketkotlin.data.model.response.Product
import com.example.foodmarketkotlin.databinding.ItemHomeVerticalBinding
import com.example.foodmarketkotlin.utils.priceIDR
import com.example.foodmarketkotlin.utils.toRupiah

class HomePopularAdapter(
    private var productList: ArrayList<Product> = ArrayList(),
    private val itemAdapterCallback: ItemAdapterCallback
) : RecyclerView.Adapter<HomePopularAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeVerticalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productList[position], itemAdapterCallback)
    }

    override fun getItemCount(): Int = productList.size

    fun setData(newList: List<Product>) {
        productList.clear()
        productList.addAll(newList)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemHomeVerticalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Product, callback: ItemAdapterCallback) {
            binding.tvTitle.text = data.title
            binding.tvPrice.text = data.priceIDR.toRupiah()
            binding.rbFood.rating = data.rating.toFloat()

            Glide.with(itemView.context)
                .load(data.thumbnail)
                .into(binding.ivPoster)

            itemView.setOnClickListener {
                callback.onClick(it, data)
            }
        }
    }

    interface ItemAdapterCallback {
        fun onClick(v: View, data: Product)
    }
}
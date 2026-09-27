package com.example.foodmarketkotlin.ui.home.newtaste

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.response.Product

class HomeNewTasteAdapter(
    private var productList: ArrayList<Product> = ArrayList(),
    private val itemAdapterCallback : ItemAdapterCallback
) : RecyclerView.Adapter<HomeNewTasteAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view  = layoutInflater.inflate(R.layout.item_home_vertical, parent, false)
        return ViewHolder(view)
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


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ LEBIH BAIK: findViewById sekali saja di init
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val rbFood: RatingBar = itemView.findViewById(R.id.rbFood)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)

        fun bind(data: Product, itemAdapterCallback: ItemAdapterCallback) {
            tvTitle.text = data.title
            tvPrice.text = data.title
            rbFood.rating = data.rating.toFloat()

            Glide.with(itemView.context).load(data.thumbnail).into(ivPoster)

            itemView.setOnClickListener {
                itemAdapterCallback.onCLick(it, data)
            }
        }
    }

    interface ItemAdapterCallback {
        fun onCLick(v: View, data: Product)
    }

}
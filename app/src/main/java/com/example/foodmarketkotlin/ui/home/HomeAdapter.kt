package com.example.foodmarketkotlin.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.dummy.HomeModel

class HomeAdapter(
    private val listData : List<HomeModel>,
    private val itemAdapterCallback : ItemAdapterCallback
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view  = layoutInflater.inflate(R.layout.item_home_horizontal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeAdapter.ViewHolder, position: Int) {
        holder.bind(listData[position], itemAdapterCallback)
    }


    override fun getItemCount(): Int {
        return listData.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ LEBIH BAIK: findViewById sekali saja di init
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitleItem)
        private val rbFood: RatingBar = itemView.findViewById(R.id.rbFood)
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)

        fun bind(data: HomeModel, itemAdapterCallback: ItemAdapterCallback) {
            // Langsung pakai, tidak perlu findViewById lagi
            tvTitle.text = data.title
            rbFood.rating = data.rating

//            Glide.with(itemView.context).load(data.src).into(ivPoster)

            itemView.setOnClickListener {
                itemAdapterCallback.onCLick(it, data)
            }
        }
    }

    interface ItemAdapterCallback {
        fun onCLick(v: View, data:HomeModel)
    }


}
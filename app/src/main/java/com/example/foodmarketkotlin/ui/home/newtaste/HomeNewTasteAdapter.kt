package com.example.foodmarketkotlin.ui.home.newtaste

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.dummy.HomeVerticalModel

class HomeNewTasteAdapter(
    private val listData : List<HomeVerticalModel>,
    private val itemAdapterCallback : ItemAdapterCallback
) : RecyclerView.Adapter<HomeNewTasteAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeNewTasteAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view  = layoutInflater.inflate(R.layout.item_home_vertical, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeNewTasteAdapter.ViewHolder, position: Int) {
        holder.bind(listData[position], itemAdapterCallback)
    }


    override fun getItemCount(): Int {
        return listData.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ LEBIH BAIK: findViewById sekali saja di init
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val rbFood: RatingBar = itemView.findViewById(R.id.rbFood)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)

        fun bind(data: HomeVerticalModel, itemAdapterCallback: ItemAdapterCallback) {
            // Langsung pakai, tidak perlu findViewById lagi
            tvTitle.text = data.title
            tvPrice.text = data.title
            rbFood.rating = data.rating

//            Glide.with(itemView.context).load(data.src).into(ivPoster)

            itemView.setOnClickListener {
                itemAdapterCallback.onCLick(it, data)
            }
        }
    }

    interface ItemAdapterCallback {
        fun onCLick(v: View, data:HomeVerticalModel)
    }


}
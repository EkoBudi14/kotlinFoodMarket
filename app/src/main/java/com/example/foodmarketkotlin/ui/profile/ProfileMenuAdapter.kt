package com.example.foodmarketkotlin.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.dummy.ProfileMenuModel

class ProfileMenuAdapter(
    private val listData : List<ProfileMenuModel>,
    private val itemAdapterCallback : ItemAdapterCallback
) : RecyclerView.Adapter<ProfileMenuAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileMenuAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view  = layoutInflater.inflate(R.layout.item_menu_profile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileMenuAdapter.ViewHolder, position: Int) {
        holder.bind(listData[position], itemAdapterCallback)
    }


    override fun getItemCount(): Int {
        return listData.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ LEBIH BAIK: findViewById sekali saja di init
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

        fun bind(data: ProfileMenuModel, itemAdapterCallback: ItemAdapterCallback) {
            tvTitle.text = data.title

            itemView.setOnClickListener {
                itemAdapterCallback.onCLick(it, data)
            }
        }
    }

    interface ItemAdapterCallback {
        fun onCLick(v: View, data:ProfileMenuModel)
    }


}
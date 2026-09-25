package com.example.foodmarketkotlin.ui.profile.foodmarket

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.dummy.ProfileMenuModel
import com.example.foodmarketkotlin.ui.profile.ProfileMenuAdapter

class ProfileFoodMarketFragment : Fragment(), ProfileMenuAdapter.ItemAdapterCallback  {

    private val menuArrayList = ArrayList<ProfileMenuModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_food_market, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initDataDummy()

            val recyclerView: RecyclerView = view.findViewById(R.id.rcListProfile)

            val adapter = ProfileMenuAdapter(menuArrayList, this)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter


        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initDataDummy() {
        menuArrayList.clear()
        menuArrayList.add(ProfileMenuModel("Rate Apps"))
        menuArrayList.add(ProfileMenuModel("Help Center"))
        menuArrayList.add(ProfileMenuModel("Privacy & Policy"))
        menuArrayList.add(ProfileMenuModel("Term & Conditions"))
    }

    override fun onCLick(v: View, data: ProfileMenuModel) {
        Toast.makeText(requireContext(), "Clicked: ${data.title}", Toast.LENGTH_SHORT).show()
    }

}
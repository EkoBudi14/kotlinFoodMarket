package com.example.foodmarketkotlin.ui.profile.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.dummy.ProfileMenuModel
import com.example.foodmarketkotlin.ui.profile.ProfileMenuAdapter

class ProfileAccountFragment : Fragment(), ProfileMenuAdapter.ItemAdapterCallback {

    private val menuArrayList = ArrayList<ProfileMenuModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_account, container, false)
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
        menuArrayList.add(ProfileMenuModel("Edit Profile"))
        menuArrayList.add(ProfileMenuModel("Home Address"))
        menuArrayList.add(ProfileMenuModel("Security"))
        menuArrayList.add(ProfileMenuModel("Payments"))
    }

    override fun onCLick(v: View, data: ProfileMenuModel) {
        if (data.title == "Edit Profile") {
            Navigation.findNavController(v).navigate(R.id.profileEditFragment)
        } else {
            Toast.makeText(requireContext(), "Clicked: ${data.title}", Toast.LENGTH_SHORT).show()
        }
    }
}
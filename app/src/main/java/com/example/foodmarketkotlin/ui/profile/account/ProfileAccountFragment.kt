package com.example.foodmarketkotlin.ui.profile.account

import android.content.Intent
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
import com.example.foodmarketkotlin.ui.auth.AuthActivity
import com.example.foodmarketkotlin.ui.profile.ProfileMenuAdapter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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
        menuArrayList.add(ProfileMenuModel("Log out"))
    }


    override fun onCLick(v: View, data: ProfileMenuModel) {
        if (data.title == "Edit Profile") {
            Navigation.findNavController(v).navigate(R.id.profileEditFragment)
        } else if (data.title == "Log out") {
            logoutAccount()
        } else {
            Toast.makeText(requireContext(), "Clicked: ${data.title}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutAccount() {
        Firebase.auth.signOut()
        Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()

        val intent = Intent(activity, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}
package com.example.foodmarketkotlin.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.foodmarketkotlin.databinding.FragmentProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var profileListener: com.google.firebase.firestore.ListenerRegistration? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setProfile()
    }

    private fun setupViewPager() {
        val sectionPagerAdapter = SectionPagerAdapter(childFragmentManager)

        binding.viewPager.adapter = sectionPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun setProfile() {
        val auth = Firebase.auth
        val db = Firebase.firestore
        val currentUser = auth.currentUser?.uid

        if (currentUser != null) {
            profileListener = db.collection("users").document(currentUser).addSnapshotListener { document, error ->
                val binding = _binding ?: return@addSnapshotListener

                if (error != null) {
                    Toast.makeText(
                        context,
                        "Gagal mengambil data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val name = document.getString("fullName") ?: document.getString("name")
                    val email = document.getString("email")

                    binding.tvNameProfile.text = name
                    binding.tvEmailProfile.text = email
                } else {
                    Toast.makeText(context, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileListener?.remove()
        profileListener = null
        _binding = null
    }
}
package com.example.foodmarketkotlin.ui.profile.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.databinding.FragmentProfileEditBinding
import com.example.foodmarketkotlin.ui.auth.AuthActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etHouseNo: EditText
    private lateinit var etCity: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDeleteProfile: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupFunction()

    }

    private fun setupToolbar() {
        binding.includedToolbar.toolbar.apply {
            title = "Edit Profile"
            subtitle = "Update your personal data"
            navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_000, null)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    private fun setupFunction() {
        etFullName = binding.etFullName
        etPhone = binding.etPhone
        etAddress = binding.etAddress
        etHouseNo = binding.etHouseNo
        etCity = binding.etCity
        btnSave = binding.btnSaveProfile
        btnDeleteProfile = binding.btnDeleteProfile

        loaded()

        btnSave.setOnClickListener {
            updateProfile()
        }

        btnDeleteProfile.setOnClickListener {
            confirmDelete()
        }
    }
    
    private fun loaded() {
        val currentUser = auth.currentUser?.uid

        if (currentUser != null) {
            db.collection("users").document(currentUser).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    etFullName.setText(document.getString("fullName") ?: document.getString("name"))
                    etPhone.setText(document.getString("phone"))
                    etAddress.setText(document.getString("address"))
                    etHouseNo.setText(document.getString("houseNo"))
                    etCity.setText(document.getString("city"))
                }
            }

        } else {
            Toast.makeText(context, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateProfile() {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val updates = mapOf(
                "fullName" to etFullName.text.toString(),
                "phone" to etPhone.text.toString(),
                "address" to etAddress.text.toString(),
                "houseNo" to etHouseNo.text.toString(),
                "city" to etCity.text.toString()
            )

            db.collection("users").document(uid).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Profile berhasil diupdate",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Profile gagal diupdate: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(context, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

    }

    private fun confirmDelete() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Akun")
            .setMessage("Yakin mau hapus profile & akun ini? Aksi ini nggak bisa dibatalin.")
            .setPositiveButton("Ya") { _, _ -> deleteProfile() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteProfile() {
        val user = auth.currentUser
        val uid = user?.uid


        if (uid != null) {
            db.collection("users").document(uid).delete()
                .addOnSuccessListener {
                    user.delete().addOnCompleteListener { task ->
                        Toast.makeText(
                            requireContext(),
                            "Akun berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(
                            android.content.Intent(
                                requireContext(),
                                AuthActivity::class.java
                            )
                        )
                        requireActivity().finish()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Gagal hapus akun Coba login ulang dulu.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(context, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

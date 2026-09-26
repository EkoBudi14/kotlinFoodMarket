package com.example.foodmarketkotlin.ui.detail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.IntentCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.response.Product
import com.example.foodmarketkotlin.databinding.FragmentPaymentBinding
import com.example.foodmarketkotlin.util.parcelableOrNull
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var productData: Product? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as DetailActivity).toolbarPayment()
        setData()

       binding.btnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_payment_success)
        }
    }


    private fun setData() {
        productData = arguments?.parcelableOrNull<Product>("product")
            ?: IntentCompat.getParcelableExtra(requireActivity().intent, "foodResponse", Product::class.java)
        val fromArgs = arguments?.parcelableOrNull<Product>("product")
        Toast.makeText(requireContext(), "fromArgs = $fromArgs", Toast.LENGTH_LONG).show()


        val auth = Firebase.auth
        val db = Firebase.firestore
        val currentUser = auth.currentUser?.uid


        productData?.let {
            product ->
            binding.tvTitle.text = product.title
            binding.tvPrice.text = "Rp ${product.price}"
            binding.tvHarga.text = "Rp ${product.price}"
            binding.textView14.text = "1 items"
            binding.textView12.text = "Rp ${product.price}"
            binding.tvTax.text =    "Rp ${product.price}"
            binding.tvTotal.text = "Rp ${product.price}"
            Glide.with(requireContext())
                .load(product.thumbnail)
                .into(binding.ivPoster)
        }

        if (currentUser != null) {
            db.collection("users").document(currentUser).addSnapshotListener { document, error ->
                if (error != null) {
                    Toast.makeText(context, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val name = document.getString("fullName") ?: document.getString("name")
                    val phone = document.getString("phone")
                    val address = document.getString("address")
                    val city = document.getString("city")

                    binding.tvName.text = name
                    binding.textPhoneNo.text = phone
                    binding.tvAddress.text = address
                    binding.tvCity.text = city
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
        _binding = null
    }

}
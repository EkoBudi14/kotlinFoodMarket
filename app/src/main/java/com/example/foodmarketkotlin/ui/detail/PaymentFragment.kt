package com.example.foodmarketkotlin.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.response.Product
import com.example.foodmarketkotlin.databinding.FragmentPaymentBinding
import com.example.foodmarketkotlin.util.parcelableOrNull
import com.example.foodmarketkotlin.utils.DRIVER_FEE
import com.example.foodmarketkotlin.utils.priceIDR
import com.example.foodmarketkotlin.utils.taxOf
import com.example.foodmarketkotlin.utils.toRupiah
import com.example.foodmarketkotlin.utils.totalOf
import com.example.foodmarketkotlin.viewModel.PaymentUiState
import com.example.foodmarketkotlin.viewModel.PaymentViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var productData: Product? = null

    private val viewModel: PaymentViewModel by viewModels()

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
        observeCheckout()

        binding.btnCheckout.setOnClickListener {
            val product = productData ?: return@setOnClickListener
            viewModel.checkout(product.id, QTY)
        }
    }

    private fun observeCheckout() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.btnCheckout.isEnabled = state !is PaymentUiState.Loading
                    binding.btnCheckout.text =
                        if (state is PaymentUiState.Loading) "Processing..." else "Checkout Now"

                    when (state) {
                        is PaymentUiState.Success -> {
                            viewModel.resetState()
                            val bundle = bundleOf(
                                MidtransPaymentFragment.ARG_ORDER_ID to state.transaction.orderId,
                                MidtransPaymentFragment.ARG_REDIRECT_URL to state.transaction.redirectUrl
                            )
                            findNavController().navigate(R.id.action_payment_midtrans, bundle)
                        }

                        is PaymentUiState.Error -> {
                            viewModel.resetState()
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    private fun setData() {
//        productData = arguments?.parcelableOrNull<Product>("product")
//            ?: IntentCompat.getParcelableExtra(requireActivity().intent, "foodResponse", Product::class.java)

        productData = arguments?.parcelableOrNull<Product>("product")

        val auth = Firebase.auth
        val db = Firebase.firestore
        val currentUser = auth.currentUser?.uid


        productData?.let { product ->
            // Hanya untuk tampilan; total yang ditagih dihitung ulang di backend.
            val subtotal = product.priceIDR * QTY
            binding.tvTitle.text = product.title
            binding.textView7.text = product.title
            binding.tvPrice.text = product.priceIDR.toRupiah()
            binding.tvHarga.text = subtotal.toRupiah()
            binding.textView14.text = "$QTY items"
            binding.textView12.text = DRIVER_FEE.toRupiah()
            binding.tvTax.text = taxOf(subtotal).toRupiah()
            binding.tvTotal.text = totalOf(subtotal).toRupiah()
            Glide.with(requireContext())
                .load(product.thumbnail)
                .into(binding.ivPoster)
        }

        if (currentUser != null) {
            db.collection("users").document(currentUser).addSnapshotListener { document, error ->

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

    companion object {
        private const val QTY = 1
    }
}
package com.example.foodmarketkotlin.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.IntentCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.response.Product
import com.example.foodmarketkotlin.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private var productData: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as DetailActivity).toolbarDetail()
        setData()
        navigateButton()

    }

    private fun navigateButton() {
        binding.tvBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnOrderNowDetail.setOnClickListener {

            val bundle = bundleOf("product" to productData )
            findNavController().navigate(R.id.action_payment, bundle)
        }
    }

    private fun setData() {
        productData = IntentCompat.getParcelableExtra(requireActivity().intent, "foodResponse", Product::class.java)


        productData?.let { product ->
            binding.tvTitleDetail.text = product.title
            binding.tvDescDetail.text = product.description
            binding.tvIngridientsDetail.text = product.brand ?: "-"
            binding.tvTotalDetail.text = "Rp ${product.price}"
            binding.ratingBar.rating = product.rating.toFloat()
            Glide.with(requireContext())
                .load(product.thumbnail)
                .into(binding.imageView4)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

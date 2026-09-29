package com.example.foodmarketkotlin.ui.detail

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.databinding.FragmentMidtransPaymentBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore


class MidtransPaymentFragment : Fragment() {
    private var _binding: FragmentMidtransPaymentBinding? = null
    private val binding get() = _binding!!

    private var orderId: String? = null
    private var orderListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMidtransPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as DetailActivity).toolbarPayment()

        orderId = requireArguments().getString(ARG_ORDER_ID).orEmpty()
        val redirectUrl = requireArguments().getString(ARG_REDIRECT_URL).orEmpty()

        setupWebView(redirectUrl)
        setupBackPress()
    }

    override fun onStart() {
        super.onStart()
        listenOrderStatus()
    }

    override fun onStop() {
        super.onStop()
        orderListener?.remove()
        orderListener = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(url: String) {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean =
                openExternalIfNeeded(request.url)

            override fun onPageFinished(view: WebView, url: String) {
                _binding?.progressBar?.visibility = View.GONE
            }
        }

        binding.webView.loadUrl(url)
    }

    private fun openExternalIfNeeded(uri: Uri): Boolean {
        val scheme = uri.scheme ?: return false
        if (scheme == "http" || scheme == "https") return false

        try {
            val inten = if (scheme == "intent") {
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    component = null
                    selector = null
                }
            } else {
                Intent(Intent.ACTION_VIEW, uri)
            }

            try {
                startActivity(inten)
            } catch (e: ActivityNotFoundException) {
                val fallbackUrl = inten.getStringExtra("browser_fallback_url")
                if (fallbackUrl != null) {
                    binding.webView.loadUrl(fallbackUrl)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Aplikasi pembayaran tidak terpasang",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Tidak bisa membuka link pembayaran", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        )
    }

    private fun listenOrderStatus() {
        val id = orderId
        if (id.isNullOrEmpty() || orderListener != null) return

        orderListener =
            Firebase.firestore.collection("orders").document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || _binding == null) {
                        return@addSnapshotListener
                    }

                    when (snapshot.getString("status")) {
                        STATUS_PAID -> {
                            orderListener?.remove()
                            orderListener = null

                            findNavController().navigate(R.id.action_payment_success)
                        }

                        STATUS_EXPIRED -> {
                            orderListener?.remove()
                            orderListener = null
                            Toast.makeText(
                                context,
                                "Pembayaran dibatalkan / kedaluwarsa",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                    }
                }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.destroy()
        _binding = null
    }


    companion object {
        const val ARG_ORDER_ID = "orderId"
        const val ARG_REDIRECT_URL = "redirectUrl"

        // Nilai status dari backend (orders/{orderId}.status).
        private const val STATUS_PAID = "PAID"
        private const val STATUS_EXPIRED = "EXPIRED"
        private const val STATUS_CANCELLED = "CANCELLED"
    }
}
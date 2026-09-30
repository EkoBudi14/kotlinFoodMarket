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
import com.example.foodmarketkotlin.ui.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import java.net.URISyntaxException


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
        stopListening()
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
        if (isFinishRedirect(uri)) {
            handleFinishRedirect(uri.getQueryParameter("transaction_status"))
            return true
        }
        if (scheme == "http" || scheme == "https") return false

        val intent = try {
            if (scheme == "intent") {
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    component = null
                    selector = null
                }
            } else {
                Intent(Intent.ACTION_VIEW, uri)
            }
        } catch (e: URISyntaxException) {
            Toast.makeText(context, "Link pembayaran tidak valid", Toast.LENGTH_SHORT).show()
            return true
        }


        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
            if (fallbackUrl != null) {
                binding.webView.loadUrl(fallbackUrl)
            } else {
                Toast.makeText(
                    context,
                    "Tidak ditemukan aplikasi untuk membuka link ini",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return true
    }

    // Snap redirect ke Finish/Unfinish/Error URL (dashboard Midtrans) dengan query
    // order_id & transaction_status. URL itu bukan halaman asli, jadi jangan di-load.
    private fun isFinishRedirect(uri: Uri): Boolean {
        val host = uri.host.orEmpty()
        if (host.endsWith("midtrans.com") || host.endsWith("veritrans.co.id")) return false
        return !uri.isOpaque &&
            uri.getQueryParameter("order_id") != null &&
            uri.getQueryParameter("transaction_status") != null
    }

    private fun handleFinishRedirect(transactionStatus: String?) {
        val nav = findNavController()
        if (nav.currentDestination?.id != R.id.fragmentMidtransPayment) return

        when (transactionStatus) {
            "settlement", "capture" -> {
                stopListening()
                nav.navigate(R.id.action_payment_success)
            }

            "pending" -> {
                Toast.makeText(
                    context,
                    "Menunggu pembayaran, cek status di halaman Order",
                    Toast.LENGTH_SHORT
                ).show()
                openOrderPage()
            }

            else -> {
                Toast.makeText(
                    context,
                    "Pembayaran gagal / dibatalkan",
                    Toast.LENGTH_SHORT
                ).show()
                openOrderPage()
            }
        }
    }

    // Order sudah tersimpan di Firestore sejak checkout, jadi setelah keluar dari Midtrans
    // user diarahkan ke tab Order (bukan balik ke Payment) untuk pantau statusnya.
    private fun openOrderPage() {
        stopListening()
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_ORDER, true)
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                // Sengaja tidak pakai webView.goBack(): Snap menyimpan banyak entri history
                // (redirect & route internal), jadi user harus back berkali-kali. Navigasi
                // di dalam Snap cukup lewat tombol back milik halaman Snap sendiri.
                // Status belum PAID di sini (kalau sudah, listener sudah pindah ke halaman sukses).
                override fun handleOnBackPressed() = openOrderPage()
            }
        )
    }

    private fun listenOrderStatus() {
        val id = orderId
        if (id.isNullOrEmpty() || orderListener != null) return

        orderListener =
            Firebase.firestore.collection("orders").document(id)
                .addSnapshotListener { snapshot, error ->
                    if (_binding == null) return@addSnapshotListener
                    if (error != null) {
                        Toast.makeText(
                            context,
                            "Gagal cek status: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }
                    if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

                    val nav = findNavController()
                    if (nav.currentDestination?.id != R.id.fragmentMidtransPayment) return@addSnapshotListener

                    when (snapshot.getString("status")) {
                        STATUS_PAID -> {
                            stopListening()
                            nav.navigate(R.id.action_payment_success)
                        }

                        STATUS_EXPIRED, STATUS_CANCELLED -> {
                            stopListening()
                            Toast.makeText(
                                context,
                                "Pembayaran dibatalkan / kedaluwarsa",
                                Toast.LENGTH_SHORT
                            ).show()
                            nav.popBackStack()
                        }
                    }
                }


    }

    private fun stopListening() {
        orderListener?.remove()
        orderListener = null
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
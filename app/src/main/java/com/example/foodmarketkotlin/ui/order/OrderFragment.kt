package com.example.foodmarketkotlin.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.response.Order
import com.example.foodmarketkotlin.databinding.FragmentOrdersBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore

class OrderFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val adapter = OrderAdapter()
    private var ordersListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvOrders.adapter = adapter

        binding.btnFindFoods.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                .selectedItemId = R.id.navigation_home
        }

        // RecyclerView ada di dalam FrameLayout, jadi cek scroll-nya manual supaya
        // tarik ke bawah di tengah list tidak memicu refresh.
        binding.swipeRefresh.setOnChildScrollUpCallback { _, _ ->
            binding.layoutOrders.isVisible && binding.rvOrders.canScrollVertically(-1)
        }
        binding.swipeRefresh.setOnRefreshListener { refreshOrders() }

        listenOrders()
    }

    private fun ordersQuery(uid: String): Query =
        Firebase.firestore.collection("orders").whereEqualTo("uid", uid)

    private fun listenOrders() {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            showOrders(emptyList())
            return
        }

        // Snapshot listener supaya status berubah otomatis saat webhook Midtrans masuk.
        ordersListener = ordersQuery(uid)
            .addSnapshotListener { snapshot, error ->
                if (_binding == null) return@addSnapshotListener

                if (error != null) {
                    Toast.makeText(context, "Gagal memuat order: ${error.message}", Toast.LENGTH_SHORT).show()
                    showOrders(emptyList())
                    return@addSnapshotListener
                }

                showOrders(snapshot?.documents.orEmpty().toOrders())
            }
    }

    // Ambil ulang langsung dari server (bukan cache) saat user tarik ke bawah.
    private fun refreshOrders() {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            binding.swipeRefresh.isRefreshing = false
            showOrders(emptyList())
            return
        }

        ordersQuery(uid).get(Source.SERVER)
            .addOnSuccessListener { snapshot ->
                if (_binding == null) return@addOnSuccessListener
                showOrders(snapshot.documents.toOrders())
            }
            .addOnFailureListener { error ->
                if (_binding == null) return@addOnFailureListener
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(context, "Gagal refresh: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Diurutkan di client supaya tidak perlu composite index (uid + createdAt).
    private fun List<DocumentSnapshot>.toOrders(): List<Order> =
        map(Order::from).sortedByDescending { it.createdAt }

    private fun showOrders(orders: List<Order>) {
        binding.swipeRefresh.isRefreshing = false
        binding.progressBar.isVisible = false
        binding.layoutOrders.isVisible = orders.isNotEmpty()
        binding.layoutEmpty.isVisible = orders.isEmpty()
        adapter.submitList(orders)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ordersListener?.remove()
        ordersListener = null
        _binding = null
    }
}

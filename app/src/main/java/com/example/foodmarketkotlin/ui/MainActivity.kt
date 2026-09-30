package com.example.foodmarketkotlin.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
        NavigationUI.setupWithNavController(navView, navController)

        // Dikirim PaymentSuccessFragment ("View My Order") supaya langsung buka tab Order.
        if (intent.getBooleanExtra(EXTRA_OPEN_ORDER, false)) {
            navView.selectedItemId = R.id.navigation_order
        }
    }

    companion object {
        const val EXTRA_OPEN_ORDER = "open_order"
    }
}

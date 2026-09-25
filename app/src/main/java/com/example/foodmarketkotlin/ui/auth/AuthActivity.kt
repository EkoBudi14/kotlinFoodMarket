@file:Suppress("DEPRECATION")

package com.example.foodmarketkotlin.ui.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodmarketkotlin.R

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun toolbarSignUp() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Sign Up"
        toolbar.subtitle = "Register and eat"
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_000, null)
        toolbar.setNavigationOnClickListener {  onBackPressedDispatcher.onBackPressed()  }
    }


    fun toolbarSignUpAddress() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Address"
        toolbar.subtitle = "Make sure it's valid"
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_000, null)
        toolbar.setNavigationOnClickListener {  onBackPressedDispatcher.onBackPressed()  }
    }


    fun toolbarSignUpSuccess() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE
    }

}
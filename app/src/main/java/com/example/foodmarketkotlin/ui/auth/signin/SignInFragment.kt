package com.example.foodmarketkotlin.ui.auth.signin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.ui.MainActivity
import com.example.foodmarketkotlin.ui.auth.AuthActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SignInFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Simple findViewById approach
        val btnSignup = view.findViewById<Button>(R.id.btnSignup)
        val btnSignIn = view.findViewById<Button>(R.id.btnSignin)
        val etEmail = view.findViewById<EditText>(R.id.emailTypeSignIn)
        val etPassword = view.findViewById<EditText>(R.id.passwordTypeSignIn)


        btnSignup.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.fragmentSignUp)
            (activity as AuthActivity).toolbarSignUp()
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            var isValid = true



            if (email.isEmpty()) {
                etEmail.error = "Email tidak boleh kosong"
                isValid = false

            }

            if (password.isEmpty()) {
                etPassword.error = "Password tidak boleh kosong"
                isValid = false
            }


            // ⛔ Jika ada yang kosong, STOP dan jangan pindah halaman!
            if (!isValid) {
                Toast.makeText(requireContext(), "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performSignIn(email, password)
        }
    }

    private fun performSignIn(email: String, password: String) {
        val auth = Firebase.auth

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}
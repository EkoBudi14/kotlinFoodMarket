package com.example.foodmarketkotlin.ui.auth.signup

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.ui.auth.AuthActivity

class SignUpFragment : Fragment() {

    private  var filePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Simple findViewById approach
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        val btnSignup = view.findViewById<Button>(R.id.btnContinue)
        val etFullName = view.findViewById<EditText>(R.id.fullNameType)
        val etEmail = view.findViewById<EditText>(R.id.emailType)
        val etPassword = view.findViewById<EditText>(R.id.passwordType)

        ivProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSignup.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            var isValid = true

            if (fullName.isEmpty()) {
                etFullName.error = "Nama tidak boleh kosong"
                isValid = false
            }

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


            val bundleParameter = Bundle().apply {
                putString("fullName", fullName)
                putString("email", email)
                putString("password", password)
                putString("profileUri", filePath?.toString())
            }

            Navigation.findNavController(it).navigate(R.id.fragmentSignUpAddress, bundleParameter)
            (activity as AuthActivity).toolbarSignUpAddress()
        }
    }

    // Launcher for photos
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
            uri : Uri? ->
        if (uri != null) {
            filePath = uri

            // show image picker
            val ivProvile = view?.findViewById<ImageView>(R.id.ivProfile)
            ivProvile?.let {
                Glide.with(this).load(uri).apply(RequestOptions.circleCropTransform()).into(it)
            }
        }
    }


}
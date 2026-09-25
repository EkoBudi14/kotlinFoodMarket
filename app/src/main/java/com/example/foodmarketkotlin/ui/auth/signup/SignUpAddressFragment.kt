package com.example.foodmarketkotlin.ui.auth.signup

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
import com.example.foodmarketkotlin.ui.auth.AuthActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class SignUpAddressFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Simple findViewById approach
        val btnSignup = view.findViewById<Button>(R.id.btnSignUpNow)
        val etPhone = view.findViewById<EditText>(R.id.phoneNoType)
        val etAddress = view.findViewById<EditText>(R.id.addressType)
        val etHouseNo = view.findViewById<EditText>(R.id.houseNoType)
        val etCity = view.findViewById<EditText>(R.id.cityNoType)


        btnSignup.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val houseNo = etHouseNo.text.toString().trim()
            val city = etCity.text.toString().trim()

            if (phone.isEmpty() || address.isEmpty() || houseNo.isEmpty() || city.isEmpty()) {
                Toast.makeText(context, "Semua field alamat harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            performFullRegistration(phone, address, houseNo, city)
        }
    }

    private fun performFullRegistration(phone: String, address: String, houseNo: String, city: String) {
        val auth = Firebase.auth
        val db = Firebase.firestore


        val fullName= arguments?.getString("fullName")
        val email = arguments?.getString("email")
        val password = arguments?.getString("password")
        val profileUri = arguments?.getString("profileUri")

        auth.createUserWithEmailAndPassword(email.toString(), password.toString()).addOnCompleteListener {
            task -> if (task.isSuccessful) {
            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener


            val userMap = hashMapOf(
                "uid" to userId,
                "fullName" to fullName,
                "email" to email,
                "password" to password,
                "profileUri" to profileUri,
                "phone" to phone,
                "address" to address,
                "houseNo" to houseNo,
                "city" to city
            )

            db.collection("users").document(userId).set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(requireView()).navigate(R.id.fragmentSignIn)
                    (activity as AuthActivity).toolbarSignUp()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }

        } else  {
            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }

        }

    }

}
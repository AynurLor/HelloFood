package com.example.hellofood

import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.hellofood.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        firstNamefocusListenener()
        lastNamefocusListenener()
        addressfocusListenener()
        emailFocusListenener()
        passwordFocusListenener()
        confirmPasswordFocusListenener()
        phoneFocusListenener()

        binding.cl01.setOnClickListener {
            clearFocus()
        }

        binding.tvLogin.setOnClickListener {
            goToLoginPage()
        }
        binding.btnSignup.setOnClickListener {
            clearFocus()
            if (!allFieldsFilled()) {
                Toast.makeText(this, R.string.toast_text_fill_up_failed, Toast.LENGTH_SHORT)
                    .show()
            } else if (!allValidCheck()) {
                Toast.makeText(this, R.string.toast_text_valid_info, Toast.LENGTH_SHORT).show()
            } else {
                registerUser()
            }
        }
    }

    private fun goToLoginPage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun registerUser() {
        binding.progressBar.visibility = View.VISIBLE
        val firstName = binding.etRegisterFirstname.text.toString().trim()
        val lastName = binding.etRegisterLastname.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()
        val phoneno = binding.etRegisterPhoneno.text.toString().trim()
        val address = binding.etRegisterAddress.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    database =
                        FirebaseDatabase.getInstance("https://hellofood-c1d85-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference()

                    val user = User(firstName, lastName, email, phoneno, address)
                    database.child("users").child(Firebase.auth.currentUser!!.uid).setValue(user)
                        .addOnSuccessListener {
                            verifyEmail()
                            val alertDialog = AlertDialog.Builder(this)
                            alertDialog.setMessage(R.string.text_message_verification_email)
                            alertDialog.setPositiveButton(R.string.text_ok, DialogInterface.OnClickListener { dialogInterface, i ->
                                goToLoginPage()
                            })
                            alertDialog.show()
                            binding.progressBar.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            Toast.makeText(baseContext, R.string.toast_text_registration_failed, Toast.LENGTH_LONG)
                                .show()
                            binding.progressBar.visibility = View.GONE
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, R.string.toast_text_authentication_failed, Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

    private fun verifyEmail() {
        val user = Firebase.auth.currentUser

        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }

    }

    private fun clearFocus() {
        binding.etRegisterFirstname.clearFocus()
        binding.etRegisterLastname.clearFocus()
        binding.etRegisterEmail.clearFocus()
        binding.etRegisterPassword.clearFocus()
        binding.etRegisterConfirmPassword.clearFocus()
        binding.etRegisterPhoneno.clearFocus()
        binding.etRegisterAddress.clearFocus()
    }

    private fun allFieldsFilled(): Boolean {
        if (binding.etRegisterFirstname.text.isEmpty()
            || binding.etRegisterLastname.text.isEmpty()
            || binding.etRegisterEmail.text.isEmpty()
            || binding.etRegisterPassword.text.isEmpty()
            || binding.etRegisterConfirmPassword.text.isEmpty()
            || binding.etRegisterPhoneno.text.isEmpty()
            || binding.etRegisterAddress.text.isEmpty()
        ) {
            return false
        }
        return true
    }

    private fun clearFields() {
        binding.etRegisterFirstname.text = null
        binding.etRegisterLastname.text = null
        binding.etRegisterEmail.text = null
        binding.etRegisterPassword.text = null
        binding.etRegisterConfirmPassword.text = null
        binding.etRegisterPhoneno.text = null
        binding.etRegisterAddress.text = null
    }

    private fun allValidCheck(): Boolean {
        if (binding.tilRegisterFirstname.helperText != null
            || binding.tilRegisterLastname.helperText != null
            || binding.tilRegisterEmail.helperText != null
            || binding.tilRegisterPassword.helperText != null
            || binding.tilRegisterConfirmPassword.helperText != null
            || binding.tilRegisterPhoneno.helperText != null
            || binding.tilRegisterAddress.helperText != null
        ) {
            return false
        }
        return true
    }

    private fun addressfocusListenener() {
        binding.etRegisterAddress.setOnFocusChangeListener { view, focused ->
            if (!focused) {
                binding.tilRegisterAddress.helperText = validAddressCheck()
            }
        }
    }

    private fun validAddressCheck(): String? {
        val address = binding.etRegisterAddress.text.toString().trim()
        if (address.length == 0) {
            return getString(R.string.helper_text_required)
        }
        return null
    }

    private fun lastNamefocusListenener() {
        binding.etRegisterLastname.setOnFocusChangeListener { view, focused ->
            if (!focused) {
                binding.tilRegisterLastname.helperText = validLnameCheck()
            }
        }
    }

    private fun validFnameCheck(): String? {
        val fname = binding.etRegisterFirstname.text.toString().trim()
        if (fname.length == 0) {
            return getString(R.string.helper_text_required)
        }
        return null
    }

    private fun firstNamefocusListenener() {
        binding.etRegisterFirstname.setOnFocusChangeListener { view, focused ->
            if (!focused) {
                binding.tilRegisterFirstname.helperText = validFnameCheck()
            }
        }
    }

    private fun validLnameCheck(): String? {
        val lname = binding.etRegisterLastname.text.toString().trim()
        if (lname.length == 0) {
            return getString(R.string.helper_text_required)
        }
        return null
    }

    private fun emailFocusListenener() {
        binding.etRegisterEmail.setOnFocusChangeListener { view, focused ->
            if (!focused) {
                binding.tilRegisterEmail.helperText = validEmailCheck()
            }
        }
    }

    private fun validEmailCheck(): String? {
        val email = binding.etRegisterEmail.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return getString(R.string.toast_text_email_failed)
        }
        return null
    }

    private fun passwordFocusListenener() {
        binding.etRegisterPassword.setOnFocusChangeListener { view, focused ->
            if (!focused) {
                binding.tilRegisterPassword.helperText = validPasswordCheck()
            }
        }
    }

    private fun validPasswordCheck(): String? {
        val password = binding.etRegisterPassword.text.toString().trim()
        if (password.length < 8) {
            return getString(R.string.toast_text_valid_min_8_chars)
        }
        if (!password.matches(".*[A-Z].*".toRegex())) {
            return getString(R.string.toast_text_valid_1_uppers_chars)
        }
        if (!password.matches(".*[a-z].*".toRegex())) {
            return getString(R.string.toast_text_valid_1_lowers_chars)
        }
        if (!password.matches(".*[0-9].*".toRegex())) {
            return getString(R.string.toast_text_valid_1_digit)
        }
        if (!password.matches(".*[!@#$%^&*-+=].*".toRegex())) {
            return getString(R.string.toast_text_valid_1_special_chars)
        }
        return null
    }

    private fun confirmPasswordFocusListenener() {
        binding.etRegisterConfirmPassword.setOnFocusChangeListener { view, focused ->
            if (!focused) {
                binding.tilRegisterConfirmPassword.helperText = validConfirmPasswordCheck()
            }
        }
    }

    private fun validConfirmPasswordCheck(): String? {
        val password1 = binding.etRegisterPassword.text.toString().trim()
        val password2 = binding.etRegisterConfirmPassword.text.toString().trim()
        if (password1 != password2) {
            return getString(R.string.toast_text_password_doesnt_matcher)
        }
        return null
    }

    private fun phoneFocusListenener() {
        binding.etRegisterPhoneno.setOnFocusChangeListener { view, focused ->
            if (!focused) {
                binding.tilRegisterPhoneno.helperText = validPhoneCheck()
            }
        }
    }

    private fun validPhoneCheck(): String? {
        val phoneno = binding.etRegisterPhoneno.text.toString().trim()
        if (phoneno.length != 11) {
            return getString(R.string.toast_text_valid_11_digits)
        }
        if (!phoneno.matches(".*[0-9].*".toRegex())) {
            return getString(R.string.toast_text_valid_all_digits)
        }
        return null
    }

}


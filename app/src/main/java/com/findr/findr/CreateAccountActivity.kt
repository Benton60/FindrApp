package com.findr.findr

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.entity.User
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // Get references to UI elements
        val fullNameInput = findViewById<EditText>(R.id.fullname_input)
        val ageInput = findViewById<EditText>(R.id.age_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirm_password_input)
        val accountDescriptionInput = findViewById<EditText>(R.id.account_description_input)
        val signUpButton = findViewById<Button>(R.id.signup_button)
        val haveAccount = findViewById<TextView>(R.id.login_link)
        // Handle sign-up button click
        signUpButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val ageStr = ageInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            val accountDescription = accountDescriptionInput.text.toString().trim()

            // Validate input fields
            if (fullName.isEmpty() || ageStr.isEmpty() || email.isEmpty() ||
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                accountDescription.isEmpty()) {

                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate age
            val age = ageStr.toIntOrNull()
            if (age == null || age <= 0) {
                Toast.makeText(this, "Please enter a valid age!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate password match
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a User object
            val newUser = User(fullName, age, email, username, password, accountDescription)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiWorker = RetrofitClient.getInstanceWithoutAuth().create(ApiService::class.java)
                    apiWorker.createUser(newUser)
                    finish()
                } catch (e: Exception) {
                    Log.e("Creating Account", e.toString())
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@CreateAccountActivity, "Username Already Exists", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        haveAccount.setOnClickListener{
            finish()
        }
    }
}
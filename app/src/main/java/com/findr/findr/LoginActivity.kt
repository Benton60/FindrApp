package com.findr.findr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

//TODO -- add in the forgot password functionality

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        val btnLogin = findViewById<Button>(R.id.login_button)
        val edtUsername = findViewById<EditText>(R.id.username_input)
        val edtPassword = findViewById<EditText>(R.id.password_input)

        findViewById<MaterialTextView>(R.id.sign_up_link).setOnClickListener{
            startActivity(Intent(this@LoginActivity, CreateAccountActivity::class.java))
        }

        btnLogin.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    var retrofitClient = RetrofitClient.getInstance(edtUsername.text.toString(),
                        edtPassword.text.toString()
                    ).create(ApiService::class.java)
                    retrofitClient.checkCredentials()
                    try {
                        openFileOutput("Authentication.txt", MODE_PRIVATE).use { fos ->
                            fos.write((edtUsername.text.toString() + "\n" + edtPassword.text.toString()).toByteArray())
                            fos.close()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    finish()
                }catch (e:Exception){
                    Log.e("Can't verify", e.toString())
                    CoroutineScope(Dispatchers.Main).launch{
                        Toast.makeText(this@LoginActivity, "Incorrect Username or Password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
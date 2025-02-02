package com.findr.findr

import ApiService
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.findr.findr.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        val btnLogin = findViewById<Button>(R.id.login_button)
        val edtUsername = findViewById<EditText>(R.id.username_input)
        val edtPassword = findViewById<EditText>(R.id.password_input)


        btnLogin.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    var retrofitClient = RetrofitClient.getInstance(edtUsername.text.toString(),
                        edtPassword.text.toString()
                    ).create(ApiService::class.java)
                    retrofitClient.getUser(edtUsername.text.toString())
                    try {
                        openFileOutput("Authentication.txt", MODE_PRIVATE).use { fos ->
                            fos.write((edtUsername.text.toString() + "\n" + edtPassword.text.toString()).toByteArray())
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
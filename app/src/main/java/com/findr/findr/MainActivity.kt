package com.findr.findr

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val txtView = findViewById<TextView>(R.id.txtView)

        CoroutineScope(Dispatchers.IO).launch {
            //testing the retrofit api call
            val retrofitClient = RetrofitClient.getInstance().create(ApiService::class.java)
            val user = retrofitClient.getUser("user")
            CoroutineScope(Dispatchers.Main).launch {
                txtView.text = user.toString()
            }
        }
    }
}
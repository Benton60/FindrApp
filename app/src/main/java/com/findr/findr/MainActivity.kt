package com.findr.findr

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.fragments.CameraFragment
import com.findr.findr.fragments.HomeFragment
import com.findr.findr.fragments.MapFragment
import com.findr.findr.fragments.VideoFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var retrofitClient: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        atStart()
    }

    override fun onResume() {
        super.onResume()
        atStart()
    }

    private fun atStart(){
        loadCredentials()
        replaceFragment(HomeFragment())
        onClickForNavBar()
    }
    //this function checks whether there is a validation saved in the apps data
    private fun loadCredentials(){
        try {
            val fileInputStream = BufferedReader(InputStreamReader(openFileInput("Authentication.txt")))
            retrofitClient = RetrofitClient.getInstance(fileInputStream.readLine(),fileInputStream.readLine()).create(ApiService::class.java)
        }catch(e: Exception){
            Log.e("Reading Credentials", e.toString())
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    private fun onClickForNavBar(){
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Handle Home click
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.camera -> {
                    // Handle Camera click
                    replaceFragment(CameraFragment())
                    true
                }
                R.id.map -> {
                    // Handle Map click
                    replaceFragment(MapFragment(retrofitClient))
                    true
                }
                R.id.videos -> {
                    // Handle Videos click
                    replaceFragment(VideoFragment())
                    true
                }
                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.addToBackStack(null) // Optional: Add to back stack for navigation
        fragmentTransaction.commit()
    }
}
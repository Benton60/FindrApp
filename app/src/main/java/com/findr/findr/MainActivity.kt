package com.findr.findr

import ApiService
import android.content.ClipData
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.findr.findr.api.RetrofitClient
import com.findr.findr.entity.User
import com.findr.findr.fragments.CameraFragment
import com.findr.findr.fragments.HomeFragment
import com.findr.findr.fragments.VideoFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    var username = ""
    var password = ""
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

        CoroutineScope(Dispatchers.IO).launch {
            val retroFitClient = RetrofitClient.getInstance("guest", "guest").create(ApiService::class.java)
            retroFitClient.addFriend(1,  2)
        }
    }

    override fun onResume() {
        super.onResume()
        atStart()
    }

    fun atStart(){
        //loadCredentials()
        onClickForNavBar()
    }
    //this function checks whether there is a validation saved in the apps data
    fun loadCredentials(){
        try {
            val fileInputStream = BufferedReader(InputStreamReader(openFileInput("Authentication.txt")))
            username = fileInputStream.readLine()
            password = fileInputStream.readLine()
        }catch(e: Exception){
            Log.e("Reading Credentials", e.toString())
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    fun onClickForNavBar(){
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
                    replaceFragment(MapFragment())
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
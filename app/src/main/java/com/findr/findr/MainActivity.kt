package com.findr.findr

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.config.LocationConfig
import com.findr.findr.fragments.CameraFragment
import com.findr.findr.fragments.HomeFragment
import com.findr.findr.fragments.MapFragment
import com.findr.findr.fragments.ProfileViewerFragment
import com.findr.findr.fragments.VideoFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.SocketTimeoutException


//TODO -- All the try{}catch{} blocks where they switch to the no internet activity need to actually check whether it is an internet related error


class MainActivity : AppCompatActivity() {

    private lateinit var retrofitClient: ApiService
    //this is for activities that require the home fragment to be reloaded when they return to the MainActivity Fragment
    private val restartHomeFragmentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            replaceFragment(HomeFragment(retrofitClient))   // Only refresh after login
        }
    }
    //this array holds all the permissions needed for the application to work
    //a.k.a. camera and location + whatever else i eventually need
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    //this class is what interacts with Android and actually requests the permissions
    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.all { it.value }
        if(!allGranted){
            //right now this closes the app if the user doesn't accept all permissions.
            finish()
        }else{
            replaceFragment(HomeFragment(retrofitClient))
        }
    }





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


        //this runs when the app is first opened to initialize the first fragment
        replaceFragment(HomeFragment(retrofitClient))

        // Add listener for fragment changes
        supportFragmentManager.addOnBackStackChangedListener {
            onFragmentChanged()
        }

        //this allows the user to click the profile icon
        //it starts the profileViewer fragment to view your own profile
        findViewById<TextView>(R.id.profileLink).setOnClickListener{
            replaceFragment(ProfileViewerFragment(retrofitClient).apply {
                arguments = Bundle().apply{
                    putString("username", RetrofitClient.getCurrentUsername())
                }
            })
        }
    }

    override fun onStart(){
        super.onStart()
        atStart()
    }

    //the atStart() function is a general purpose function that gets called whenever this activity becomes live
    //it mostly holds credential/permissions interactions
    private fun atStart() {
        if(!hasPermissions()){
            requestPermissions()
        }
        loadCredentials()
        onClickForNavBar()
        updateUserLocation()
    }

    //this function checks whether there are user credentials saved in the app's data
    private fun loadCredentials() {
        try {
            val fileInputStream = BufferedReader(InputStreamReader(openFileInput("Authentication.txt")))
            try {
                retrofitClient = RetrofitClient.getInstance(
                    fileInputStream.readLine(),
                    fileInputStream.readLine()
                ).create(ApiService::class.java)
            }catch(e: SocketTimeoutException){
                //starts the no internet activity
                startActivity(Intent(this, InternetLessActivity()::class.java))
            }
        }catch(e: Exception){
            //this is when they don't have credentials saved so they are sent to the login activity
            Log.d("File Not Found", "File not found")
            Log.e("Reading Credentials", e.toString())
            val intent = Intent(this, LoginActivity::class.java)
            //launch with the launcher so that the Fragment is reloaded when they return to this activity
            restartHomeFragmentLauncher.launch(intent)
        }
    }

    private fun updateUserLocation(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                retrofitClient.updateLocation(LocationConfig(this@MainActivity).roughLocation)
            }catch(e: Exception){
                withContext(Dispatchers.Main) {
                    startActivity(Intent(this@MainActivity, InternetLessActivity()::class.java))
                }
            }
        }
    }

    private fun onClickForNavBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Handle Home click
                    replaceFragment(HomeFragment(retrofitClient))
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

    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        // Optional: Add to back stack for navigation
        if(fragment !is HomeFragment){
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        requestPermissionsLauncher.launch(requiredPermissions)
    }

    //this updates the little icon bar in the bottom of the screen
    private fun onFragmentChanged() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        when (currentFragment) {
            is HomeFragment -> bottomNavigationView.menu.findItem(R.id.home).isChecked = true
            is CameraFragment -> bottomNavigationView.menu.findItem(R.id.camera).isChecked = true
            is MapFragment -> bottomNavigationView.menu.findItem(R.id.map).isChecked = true
            is VideoFragment -> bottomNavigationView.menu.findItem(R.id.videos).isChecked = true
        }
    }
}
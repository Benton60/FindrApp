package com.findr.findr

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import kotlinx.coroutines.launch

class InternetLessActivity : AppCompatActivity() {

    private lateinit var retryButton: Button
    private lateinit var noInternetTitle: TextView
    private lateinit var serverErrorTitle: TextView
    private lateinit var statusMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_less)

        retryButton = findViewById(R.id.retry_button)
        noInternetTitle = findViewById(R.id.no_internet_title)
        serverErrorTitle = findViewById(R.id.server_error_title)
        statusMessage = findViewById(R.id.status_message)

        retryButton.setOnClickListener {
            checkConnectivity()
        }
        checkConnectivity()
    }


    //this function is used to check why the user was sent here. either for no internet or server cant connect
    private fun checkConnectivity() {

        //no internet check
        if (!isNetworkAvailable(this)) {
            showNoInternet()
            //if there is any internet then it should just return here without doing anything
            return
        }

        //otherwise it checks whether there is an API connection
        lifecycleScope.launch {
            if (isBackendReachable()) {
                //if there is a connection it closes the activity
                finish()
            } else {
                showServerError()
            }
        }
    }

    private fun showNoInternet() {
        noInternetTitle.visibility = View.VISIBLE
        serverErrorTitle.visibility = View.GONE
        statusMessage.text = "Please check your internet connection and try again."
    }

    private fun showServerError() {
        noInternetTitle.visibility = View.GONE
        serverErrorTitle.visibility = View.VISIBLE
        statusMessage.text = "We’re having trouble reaching our servers. Please try again later."
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false

        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private suspend fun isBackendReachable(): Boolean {
        return try {
            val retrofitClient = RetrofitClient.getInstanceWithoutAuth().create(ApiService::class.java)

            val response = retrofitClient.healthCheck()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

package com.findr.findr.fragments
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.config.LocationConfig
import com.findr.findr.entity.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment(private val retrofitClient: ApiService) : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            retrofitClient.createPost(Post("Test post", RetrofitClient.getCurrentUsername(), LocationConfig(requireContext()).preciseLocation))
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getPosts()
            }catch(e: SecurityException){
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(requireContext(), "Unable to access location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getPosts(){
        val loc = LocationConfig(this.context).roughLocation;
        val p = retrofitClient.getPostsByLocation(Integer(loc.x), Integer(loc.y))
        Log.d("Location: ", p.toString())
    }
}

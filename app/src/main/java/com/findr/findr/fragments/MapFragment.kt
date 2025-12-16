package com.findr.findr.fragments


//this fragment obviously is the map that shows where friends are.
//i didn't go too into detail on the documentation as most of it is straight out of googles documentation. which ill link here
//https://developers.google.com/maps/documentation/android-sdk/overview?section=start
//its a fairly simple activity for now


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.findr.findr.InternetLessActivity
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class MapFragment(private val retrofitClient: ApiService) : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        CoroutineScope(Dispatchers.IO).launch{
            try {
                val friends =
                    retrofitClient.getFriendsByUsername(RetrofitClient.getCurrentUsername())
                for (friend in friends) {
                    Log.v("Friend", "Friend: " + friend.username + ", Location: " + friend.location)
                    CoroutineScope(Dispatchers.Main).launch {
                        mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    friend.location.latitude,
                                    friend.location.longitude,
                                )
                            ).title(friend.username)
                        )
                    }
                }
            }catch(e: SocketTimeoutException){
                if (isAdded) {
                    startActivity(Intent(requireContext(), InternetLessActivity::class.java))
                }
            }
        }
    }

}

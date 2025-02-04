package com.findr.findr.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

            val friends = retrofitClient.getFriendsByUsername(RetrofitClient.getCurrentUsername())
            for(friend in friends){
                Log.v("Friend", "Friend: " + friend.username + ", Location: " + friend.location)
                CoroutineScope(Dispatchers.Main).launch {
                    mMap.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                //this division is because my api is being retarded and won't let me store doubles
                                friend.location.x/100000.0,
                                friend.location.y/-100000.0
                            )
                        ).title(friend.username)
                    )
                }
            }
        }
    }
}

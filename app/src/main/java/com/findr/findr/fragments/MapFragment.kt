package com.findr.findr.fragments


//this fragment obviously is the map that shows where friends are.
//i didn't go too into detail on the documentation as most of it is straight out of googles documentation. which ill link here
//https://developers.google.com/maps/documentation/android-sdk/overview?section=start
//its a fairly simple activity for now


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.findr.findr.InternetLessActivity
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.net.SocketTimeoutException

class MapFragment(private val retrofitClient: ApiService) : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
                val friends = retrofitClient.getFriendsByUsername(RetrofitClient.getCurrentUsername())
                for (friend in friends) {
                    Log.v("Friend", "Friend: " + friend.username + ", Location: " + friend.location)
                    var profilePic: ResponseBody? = null
                    try {
                        profilePic = retrofitClient.downloadProfilePhoto(friend.username)
                    } catch (e: Exception) {
                        Log.e("ProfilePic", "Failed to download profile picture for ${friend.username}", e)
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        val markerView = LayoutInflater.from(requireContext()).inflate(R.layout.marker_friend, null)

                        val image = markerView.findViewById<ImageView>(R.id.friendImage)

                        profilePic?.let {
                            val circularBitmap = getCircularBitmap(responseBodyToBitmap(profilePic))
                            image.setImageBitmap(circularBitmap)
                        }



                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(friend.location.latitude, friend.location.longitude))
                                .title(friend.username)
                                .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerView))))
                    }
                }
            }catch(e: SocketTimeoutException){
                if (isAdded) {
                    startActivity(Intent(requireContext(), InternetLessActivity::class.java))
                }
            }
        }
    }

    fun createBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun responseBodyToBitmap(body: ResponseBody): Bitmap {
        return BitmapFactory.decodeStream(body.byteStream())
    }

    fun getCircularBitmap(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val left = (src.width - size) / 2
        val top = (src.height - size) / 2

        canvas.drawBitmap(src, -left.toFloat(), -top.toFloat(), paint)

        return output
    }



}

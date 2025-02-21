package com.findr.findr.fragments
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.findr.findr.R
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.findr.findr.config.LocationConfig
import com.findr.findr.entity.Post
import com.findr.findr.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment(private val retrofitClient: ApiService) : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getPosts()
                getFriends()
            }catch(e: SecurityException){
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(requireContext(), "Unable to access location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getPosts(){
    }
    private suspend fun getFriends(){
        CoroutineScope(Dispatchers.IO).launch {
            val friendsContainer = view?.findViewById<LinearLayout>(R.id.friendsContainer)
            try {
                val friends: List<User> = retrofitClient.getFriendsByUsername(RetrofitClient.getCurrentUsername())
                CoroutineScope(Dispatchers.Main).launch {
                    for (friend in friends) {
                        val friendView = LayoutInflater.from(context).inflate(R.layout.item_friend, friendsContainer, false)
                        Log.d("Friends", friends.size.toString())
                        friendView.findViewById<TextView>(R.id.friendName).text = friend.username

                        //changing the circle around the users color
                        val strokeWidth = 3 * resources.displayMetrics.density //adjusts the 3 pixels to 3 dp
                        val drawable = friendView.findViewById<View>(R.id.color_border).background as GradientDrawable
                        drawable.setStroke(strokeWidth.toInt(), getColor(requireContext(), R.color.green))

                        friendsContainer?.addView(friendView)
                        Log.d("Friends", friend.username)
                    }

                }
            } catch (e: Exception) {
                //TODO -- make it jump to the no internet activity
            }
        }
    }
}

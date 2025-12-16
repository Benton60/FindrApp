package com.findr.findr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.findr.findr.api.ApiService
import com.findr.findr.api.RetrofitClient
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException



//This activity is reached two separate ways.
//  1) if the user doesn't have saved credentials they will be immediately sent here to login which creates them.
//      in the credential check it is launched with a checker that waits until the activity is closed and if it is it relaunches the
//      HomeFragment to redisplay the relevant posts. it is impossible to get past the credential checker as it is always the first developer defined code to run when the main activity is returned to.
//  2) if the user is in the profileViewerFragment and it is their own profile the friend/unfriend button will turn into a switch account button
//      if they actually switch accounts it will switch back to the home-fragment and reload the posts otherwise it will stay in the profile-viewer

//TODO -- add in the forgot password functionality

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        val btnLogin = findViewById<Button>(R.id.login_button)
        val edtUsername = findViewById<EditText>(R.id.username_input)
        val edtPassword = findViewById<EditText>(R.id.password_input)

        findViewById<MaterialTextView>(R.id.sign_up_link).setOnClickListener{
            startActivity(Intent(this@LoginActivity, CreateAccountActivity::class.java))
        }

        btnLogin.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    var retrofitClient = RetrofitClient.getInstance(edtUsername.text.toString(),
                        edtPassword.text.toString()
                    ).create(ApiService::class.java)
                    retrofitClient.checkCredentials()
                    try {
                        openFileOutput("Authentication.txt", MODE_PRIVATE).use { fos ->
                            fos.write((edtUsername.text.toString() + "\n" + edtPassword.text.toString()).toByteArray())
                            fos.close()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    //this says hey he managed to sign in not just use the back arrows
                    setResult(Activity.RESULT_OK)
                    finish()
                }catch(e: SocketTimeoutException){

                    startActivity(Intent(this@LoginActivity, InternetLessActivity::class.java))
                }catch (e:Exception){
                    Log.e("Can't verify", e.toString())
                    CoroutineScope(Dispatchers.Main).launch{
                        Toast.makeText(this@LoginActivity, "Incorrect Username or Password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
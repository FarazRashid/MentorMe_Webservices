package com.muhammadfarazrashid.i2106595

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper

class loginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var email: TextView
    private lateinit var password: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        val signupText: TextView = findViewById(R.id.textView11)
        email = findViewById(R.id.userEmail)
        password = findViewById(R.id.userPassword)

        // Set OnClickListener for the signup text
        signupText.setOnClickListener {
            val intent = Intent(this, signUpActivity::class.java)
            startActivity(intent)
        }

        val forgotPasswordText: TextView = findViewById(R.id.forgotYourPasswordText)

        // Set OnClickListener for the forgot password text
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, forgotPasswordActivity::class.java)
            startActivity(intent)
        }

        val loginButton: Button = findViewById(R.id.signupButton)

        loginButton.setOnClickListener {
            login()
        }
    }

    fun verifyTextFields(): Boolean {


        if (email.text.toString().isEmpty()) {
            email.error = "Please enter your email"
            email.requestFocus()
            return false
        }

        if (password.text.toString().isEmpty()) {
            password.error = "Please enter your password"
            password.requestFocus()
            return false
        }

        return true
    }

    fun login() {
        if (!verifyTextFields()) {
            return
        }
        val email: String = findViewById<TextView>(R.id.userEmail).text.toString()
        val password: String = findViewById<TextView>(R.id.userPassword).text.toString()

        val webserviceHelper = WebserviceHelper(this)
        webserviceHelper.loginUser(email, password) { user ->
            if (user != null) {
                val intent = Intent(this, homePageActivity::class.java)
                //call user managerclass and save it
                val userManager = UserManager.getInstance()
                userManager.setCurrentUser(user)
                UserManager.saveUserLoggedInSP(
                    true,
                    getSharedPreferences("USER_LOGIN", MODE_PRIVATE)
                )
                UserManager.saveUserEmailSP(email, getSharedPreferences("USER_LOGIN", MODE_PRIVATE))

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@addOnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result

                    // Log and toast
                    val msg = token
                    Log.d("MyToken", msg)
                    webserviceHelper.addFCMTokenToUser(user.id, token)
                    UserManager.getCurrentUser()?.fcmToken = token.toString()
                   Toast.makeText(baseContext, "Token: ${user.password}", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                }

            }else {
                    Log.w(TAG, "signInWithEmail:failure")
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }




}


package com.muhammadfarazrashid.i2106595

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.muhammadfarazrashid.i2106595.dataclasses.User
import com.muhammadfarazrashid.i2106595.dataclasses.getUserWithEmail
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

object UserManager {


    // Singleton instance
    private var instance: UserManager? = null
    @JvmStatic
    fun getInstance(): UserManager {
        if (instance == null) {
            instance = UserManager
        }
        return instance!!
    }

    //save user logged in in shared preferences as boolean
    fun saveUserLoggedInSP(isLoggedIn:Boolean, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_LOGGED_IN", isLoggedIn)
        editor.apply()
    }

    fun saveUserEmailSP(email:String, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putString("EMAIL", email)
        editor.apply()
    }

    //retrieve user logged in from shared preferences

    fun getUserLoggedInSP(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean("IS_LOGGED_IN", false)
    }

    fun getUserEmailSP(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString("EMAIL", null)
    }

    private lateinit var currentUser: User

    fun getCurrentUser(): User? {
        return if (::currentUser.isInitialized) currentUser else null
    }


    fun setCurrentUser(user: User) {
        currentUser = user
    }

    fun setUserUrl(url: String) {
        this.currentUser.profilePictureUrl = url
    }

    fun getUserUrl(): String {
        return this.currentUser.profilePictureUrl
    }

    fun logUser(user: User){
        Log.d(TAG, "loadUserInformation: ${currentUser.id}")
        Log.d(TAG, "loadUserInformation: ${currentUser.name}")
        Log.d(TAG, "loadUserInformation: ${currentUser.email}")
        Log.d(TAG, "loadUserInformation: ${currentUser.country}")
        Log.d(TAG, "loadUserInformation: ${currentUser.city}")
        Log.d(TAG, "loadUserInformation: ${currentUser.phone}")
        Log.d(TAG, "loadUserInformation: ${currentUser.profilePictureUrl}")
        Log.d(TAG, "loadUserInformation: ${currentUser.bannerImageUrl}")
    }

    fun fetchAndSetCurrentUser(email: String, context: Context, callback: () -> Unit) {
        //set context to be where it is being called from
        val webserviceHelper = WebserviceHelper(context)
        webserviceHelper.getUserByEmail(email) { user ->
            if (user != null) {
                currentUser = user
                logUser(user)
                setCurrentUser(user)
                Log.d(TAG, "loadUserInformation: ${currentUser.id}")
                callback.invoke() // Execute the callback function
            }
        }

    }
}

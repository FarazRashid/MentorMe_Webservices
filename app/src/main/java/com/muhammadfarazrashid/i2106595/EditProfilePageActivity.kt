package com.muhammadfarazrashid.i2106595


import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.muhammadfarazrashid.i2106595.dataclasses.User
import com.muhammadfarazrashid.i2106595.dataclasses.getUserWithEmail
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import java.util.Collections
import java.util.Locale

class EditProfilePageActivity : AppCompatActivity() {

    private lateinit var userCountrySpinner: Spinner
    private lateinit var userCitySpinner: Spinner
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: User
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var userProfilePicture : ImageView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_page)

        //click on back button and go back

        //getuser
        name = findViewById(R.id.userNameTextBox)
        email = findViewById(R.id.userEmailTextBos)
        phone = findViewById(R.id.userContactNumberTextBox)
        userCountrySpinner = findViewById(R.id.userCountryTextBos)
        userCitySpinner = findViewById(R.id.userCityTextBox)
        userProfilePicture = findViewById(R.id.userProfilePhoto)

        loadUserInformation()



        val backButton = findViewById<Button>(R.id.backbutton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        //click on submit button and go back to profile page

        val submitButton = findViewById<Button>(R.id.bookAnAppointmentButton)
        submitButton.setOnClickListener {

            submit()
            onBackPressed()
        }
    }

    private fun countryList(): List<String> {
        val locales = Locale.getAvailableLocales()
        val countries = ArrayList<String>()
        for (locale in locales) {
            val country = locale.displayCountry
            if (country.trim { it <= ' ' }.length > 0 && !countries.contains(country)) {
                countries.add(country)
            }
        }
        Collections.sort(countries)
        countries.add(0, currentUser.country)
        return countries
    }

    private fun cityList(): List<String> {
        return listOf(currentUser.city, "Islamabad", "Canada", "Karachi")
    }

    private fun setUpSpinners() {
        userCountrySpinner = findViewById(R.id.userCountryTextBos)
        val countryList = countryList()
        val countryAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            countryList
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner (use for hint)
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }
                return view
            }
        }
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userCountrySpinner.adapter = countryAdapter

        userCountrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItemText = parent?.getItemAtPosition(position).toString()
                // If user changes the default selection
                // First item is disabled and used for hint
                if (position > 0) {
                    // Notify the selected item text
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected
            }
        }

        userCitySpinner = findViewById(R.id.userCityTextBox)
        val cityList = cityList()
        val cityAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            cityList
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner (use for hint)
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }
                return view
            }
        }
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userCitySpinner.adapter = cityAdapter

        userCitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItemText = parent?.getItemAtPosition(position).toString()
                // If user changes the default selection
                // First item is disabled and used for hint
                if (position > 0) {
                    // Notify the selected item text
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected
            }
        }
    }




    private fun loadUserInformation() {
        currentUser = UserManager.getCurrentUser()!!
        val userEmail = mAuth.currentUser?.email
        if (currentUser != null) {

            Log.d(ContentValues.TAG, "loadUserInformation: ${currentUser.id}")
            name.setText(currentUser.name)
            email.setText(currentUser.email)
            phone.setText(currentUser.phone)
            setUpSpinners()
            val userProfileImage = UserManager.getUserUrl()
            if (userProfileImage.isNotEmpty()) {
                Picasso.get().load(userProfileImage)
                    .into(userProfilePicture)

            }
        }
    }


//    private fun checkEmailAvailability(email: String, completion: (Boolean) -> Unit) {
//        val database = FirebaseDatabase.getInstance()
//        val usersRef = database.getReference("users")
//
//        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val isAvailable = snapshot.childrenCount.toInt() == 0
//                completion(isAvailable)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle database error
//                completion(false)
//            }
//        })
//    }
//
//    private fun checkPhoneAvailability(phone: String, completion: (Boolean) -> Unit) {
//        val database = FirebaseDatabase.getInstance()
//        val usersRef = database.getReference("users")
//
//        usersRef.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(object :
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val isAvailable = snapshot.childrenCount.toInt() == 0
//                completion(isAvailable)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle database error
//                completion(false)
//            }
//        })
//    }

    private fun verify(field: String, value: String, completion: (Boolean) -> Unit) {
        if (field == "email") {
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (!value.matches(emailPattern.toRegex())) {
                email.error = "Invalid Email"
                email.requestFocus()
                completion(false)
                return
            }
            completion(true)
            // Check email availability
//            checkEmailAvailability(value) { isEmailAvailable ->
//                if (!isEmailAvailable) {
//                    email.error = "Email is already in use"
//                    email.requestFocus()
//                    completion(false)
//                    return@checkEmailAvailability
//                }
//                // If email is available, continue
//                completion(true)
//            }
        } else if (field == "phone") {
            val phonePattern = "^[+]?[0-9]{13}$"
            if (!value.matches(phonePattern.toRegex())) {
                phone.error = "Invalid Phone Number"
                phone.requestFocus()
                completion(false)
                return
            }
            completion(true)
            // Check phone number availability
//            checkPhoneAvailability(value) { isPhoneAvailable ->
//                if (!isPhoneAvailable) {
//                    phone.error = "Phone number is already in use"
//                    phone.requestFocus()
//                    completion(false)
//                    return@checkPhoneAvailability
//                }
//                // If phone number is available, continue
//                completion(true)
//            }
        }
    }


//    private fun updateUser(field: String, value: String) {
//        val userId = currentUser.id
//        if (userId != null) {
//            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
//            userRef.child(field).setValue(value).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Update email in Firebase Authentication if email field is being updated
//                    if (field == "email") {
//                        val currentUser = mAuth.currentUser
//                        if (currentUser != null) {
//                            currentUser.updateEmail(value).addOnCompleteListener { emailUpdateTask ->
//                                if (emailUpdateTask.isSuccessful) {
//                                    Log.d("UpdateUser", "Email updated successfully")
//                                } else {
//                                    Log.e("UpdateUser", "Failed to update email: ${emailUpdateTask.exception}")
//                                    // If updating email fails, rollback the change in the real-time database
//                                    userRef.child(field).setValue(currentUser.email, DatabaseReference.CompletionListener { rollbackError, _ ->
//                                        if (rollbackError != null) {
//                                            Log.e("UpdateUser", "Failed to rollback: $rollbackError")
//                                        } else {
//                                            Log.d("UpdateUser", "Rollback successful")
//                                        }
//                                    })
//                                }
//                            }
//                        }
//                    } else {
//                        Log.d("UpdateUser", "$field updated successfully")
//                    }
//                } else {
//                    Log.e("UpdateUser", "Failed to update $field: ${task.exception}")
//                }
//            }
//        }
//    }


    private fun submit() {
        val updates = mutableMapOf<String, String>()

        if (name.text.toString() != currentUser.name) {
            updates["name"] = name.text.toString()
            UserManager.getCurrentUser()?.name = name.text.toString()
        }
        if (email.text.toString() != currentUser.email) {
            updates["email"] = email.text.toString()
            UserManager.getCurrentUser()?.email = email.text.toString()
        }
        if (phone.text.toString() != currentUser.phone) {
            updates["phone"] = phone.text.toString()
            UserManager.getCurrentUser()?.phone = phone.text.toString()
        }
        if (userCountrySpinner.selectedItem.toString() != currentUser.country) {
            updates["country"] = userCountrySpinner.selectedItem.toString()
            UserManager.getCurrentUser()?.country = userCountrySpinner.selectedItem.toString()
        }
        if (userCitySpinner.selectedItem.toString() != currentUser.city) {
            updates["city"] = userCitySpinner.selectedItem.toString()
            UserManager.getCurrentUser()?.city = userCitySpinner.selectedItem.toString()
        }

        val webserviceHelper = WebserviceHelper(this)
        UserManager.getCurrentUser()?.let { webserviceHelper.updateUser(it.id,updates) }

    }


}


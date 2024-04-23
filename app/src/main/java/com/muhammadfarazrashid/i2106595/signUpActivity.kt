package com.muhammadfarazrashid.i2106595

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager
import com.muhammadfarazrashid.i2106595.dataclasses.User
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import java.util.Collections
import java.util.Locale
import java.util.UUID


class signUpActivity : AppCompatActivity() {

    private lateinit var userCountrySpinner: Spinner
    private lateinit var userCitySpinner: Spinner
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var phone: EditText

    private var mAuth :FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        name = findViewById(R.id.userNameTextBox)
        email = findViewById(R.id.userEmailTextBos)
        password = findViewById(R.id.userPasswordTextBox)
        phone = findViewById(R.id.userContactNumberTextBox)


        setUpSpinners()

        val signUpButton: Button = findViewById(R.id.signupButton)
        signUpButton.setOnClickListener {
            signUp()
        }

        val loginText: TextView = findViewById(R.id.textView11)
        loginText.setOnClickListener {
            navigateToLogin()
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
        countries.add(0, "Select A Country")
        return countries
    }

    private fun cityList(): List<String> {
        return listOf("Select A City", "Islamabad", "Canada", "Karachi")
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



    private fun checkEmailAvailability(email: String, completion: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isAvailable = snapshot.childrenCount.toInt() == 0
                completion(isAvailable)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                completion(false)
            }
        })
    }

    private fun checkPhoneAvailability(phone: String, completion: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isAvailable = snapshot.childrenCount.toInt() == 0
                completion(isAvailable)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                completion(false)
            }
        })
    }

    private fun verifyUser(user: User, completion: (Boolean) -> Unit) {
        if (user.name.isEmpty()) {
            name.error = "Name is required"
            name.requestFocus()
            completion(false)
            return
        }
        if (user.email.isEmpty()) {
            email.error = "Email is required"
            email.requestFocus()
            completion(false)
            return
        }
        if(user.email.contains("@") && user.email.contains(".")) {
        }
        else{
            email.error = "Email is not valid"
            email.requestFocus()
            completion(false)
            return
        }

        if (user.password.isEmpty()) {
            password.error = "Password is required"
            password.requestFocus()
            completion(false)
            return
        }
        if (user.password.length < 8) {
            password.error = "Password must be at least 8 characters long"
            password.requestFocus()
            completion(false)
            return
        }
        if (user.phone.isEmpty()) {
            phone.error = "Phone number is required"
            phone.requestFocus()
            completion(false)
            return
        }
        if (user.phone.length != 13) {
            phone.error = "Phone number must be 13 characters long"
            phone.requestFocus()
            completion(false)
            return
        }
        if (user.phone[0] != '+') {
            phone.error = "Phone number must start with +"
            phone.requestFocus()
            completion(false)
            return
        }

        completion(true)

        // Check email availability

//        checkEmailAvailability(user.email) { isEmailAvailable ->
//            if (!isEmailAvailable) {
//                email.error = "Email is already in use"
//                email.requestFocus()
//                completion(false)
//                return@checkEmailAvailability
//            }
//
//            // Check phone number availability
//            checkPhoneAvailability(user.phone) { isPhoneAvailable ->
//                if (!isPhoneAvailable) {
//                    phone.error = "Phone number is already in use"
//                    phone.requestFocus()
//                    completion(false)
//                    return@checkPhoneAvailability
//                }
//
//                // If both email and phone are available, continue
//                completion(true)
//            }
//        }
    }

    private fun saveUserAuthentication(user:User)
    {
        UserManager.setCurrentUser(user)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            val token = task.result

            // Log and toast
            val msg = token
            Log.d("MyToken", msg)
            val webserviceHelper = WebserviceHelper(this)
            webserviceHelper.addFCMTokenToUser(UserManager.getCurrentUser()?.id.toString(), token)
           // UserManager.saveUserLoggedInSP(true, getSharedPreferences("USER_LOGIN", MODE_PRIVATE))
           // UserManager.saveUserEmailSP(user.email, getSharedPreferences("USER_LOGIN", MODE_PRIVATE))
            UserManager.getCurrentUser()?.fcmToken = token.toString()

        }

    }


    private fun signUp() {
        val webserviceHelper = WebserviceHelper(this)

        val user = User(
            id = UUID.randomUUID().toString(),
            name = name.text.toString(),
            email = email.text.toString(),
            country = userCountrySpinner.selectedItem.toString(),
            city = userCitySpinner.selectedItem.toString(),
            password = password.text.toString(),
            phone = phone.text.toString()
        )

        Log.d("Sign Up Activity", "User data: $user")

        verifyUser(user) { isValid ->
            if (isValid) {
                Log.d("Sign Up Activity", "User data is valid")
                webserviceHelper.saveUserToWebService(user)
                saveUserAuthentication(user)
            } else {
                Log.e(TAG, "User data is not valid")
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, loginActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToPhoneVerification() {
        val intent = Intent(this, phoneVerificationActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(this, homePageActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, MyProfileActivity::class.java)
        startActivity(intent)
    }
}

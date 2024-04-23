package com.muhammadfarazrashid.i2106595.managers

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.muhammadfarazrashid.i2106595.Mentor
import com.muhammadfarazrashid.i2106595.dataclasses.User
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

//using volley
class WebserviceHelper(private val context: Context) {

    private val BASE_URL = "http://172.16.140.33/"

    fun saveUserToWebService(user: User) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "signup.php" // Replace with your server URL

        Log.d("Sign Up webservice", "URL: $url")


        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Toast.makeText(context, "User saved successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Response: $response")
            },
            Response.ErrorListener { error ->
                // Handle error

                Log.e(TAG, "Error Sign up: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = user.id
                params["name"] = user.name
                params["email"] = user.email
                params["country"] = user.country
                params["city"] = user.city
                params["password"] = user.password
                params["phone"] = user.phone
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun loginUser(email: String, password: String, callback: (User?) -> Unit) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "login.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                // Parse the response to a User object
                val user = parseResponseToUser(response)
                callback(user)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error: ${error.message}")
                callback(null)
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["password"] = password
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun parseResponseToUser(response: String): User? {
        return try {
            val jsonObject = JSONObject(response)
            User(
                id = jsonObject.getString("id"),
                name = jsonObject.getString("name"),
                email = jsonObject.getString("email"),
                country = jsonObject.getString("country"),
                city = jsonObject.getString("city"),
                password = jsonObject.getString("password"),
                phone = jsonObject.getString("phone"),
                profilePictureUrl = BASE_URL + "Images/MentorMe/" + jsonObject.optString("profilePictureUrl", ""),
                bannerImageUrl = BASE_URL + "Images/MentorMe/" + jsonObject.optString("bannerImageUrl", ""),
                fcmToken = jsonObject.optString("fcmToken", "")
            )
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    fun addFCMTokenToUser(userId: String, token: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "add_fcm_token.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
            },
            Response.ErrorListener { error ->

                Log.e(TAG, "Error: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = userId
                params["fcmToken"] = token
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //addMentorFunction

    fun addMentor(mentor: Mentor,selectedImage:String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "add_mentor.php" // Replace with your server URL

        Log.d("Add Mentor webservice", "URL: $url")

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Toast.makeText(context, "Mentor saved successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Response: $response")
            },
            Response.ErrorListener { error ->
                // Handle error

                Log.e(TAG, "Error Add Mentor: ${error.message}")
            }
            ) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["id"] = UUID.randomUUID().toString()
                    params["name"] = mentor.name
                    //description,position,salary,status,encodedImage as profilePictureUrl
                    params["description"] = mentor.description
                    params["position"] = mentor.position
                    params["salary"] = mentor.salary
                    params["availability"] = mentor.availability
                    params["image"] = selectedImage
                    return params
                }
            }
            queue.add(stringRequest)
        }

     fun getMentors(callback: (List<Mentor>?) -> Unit) {
        val url = BASE_URL + "get_mentors.php"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getInt("success")
                    if (success == 1) {
                        val mentorsArray = response.getJSONArray("mentors")
                        val mentors = mutableListOf<Mentor>()
                        for (i in 0 until mentorsArray.length()) {
                            val mentorObject = mentorsArray.getJSONObject(i)
                            val id = mentorObject.getString("id")
                            val name = mentorObject.getString("name")
                            val position = mentorObject.getString("position")
                            val availability = mentorObject.getString("availability")
                            val salary = mentorObject.getString("salary")
                            val description = mentorObject.getString("description")
                            val profilePictureUrl = this.BASE_URL + "Images/MentorMe/"+ mentorObject.getString("profilePictureUrl")
                            mentors.add(Mentor(id, name, position, availability, salary, description, profilePictureUrl))
                            Log.d("FetchData", "Fetched data: $id, $name, $position, $availability, $salary, $description, $profilePictureUrl")
                        }
                        callback(mentors)
                    } else {
                        Log.e("FetchData", "API call was not successful")
                        callback(null)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("FetchData", "Error occurred: ${e.message}")
                    callback(null)
                }
            },
            { error ->
                Log.e("FetchData", "Error occurred: ${error.message}")
                callback(null)
            })

        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }


    //write function to upload profile picture to user

    fun uploadProfilePicture(userId: String, image: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "upload_profile_picture.php" // Replace with your server URL

        Log.d("Upload Profile Picture", "URL: $url")

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Toast.makeText(context, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Response: $response")
            },
            Response.ErrorListener { error ->
                // Handle error

                Log.e(TAG, "Error Upload Profile Picture: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = userId
                params["image"] = image
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //write function to upload banner image to user

    fun uploadBannerImage(userId: String, image: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "upload_banner_image.php" // Replace with your server URL

        Log.d("Upload Banner Image", "URL: $url")

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Toast.makeText(context, "Banner image uploaded successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Response: $response")
            },
            Response.ErrorListener { error ->
                // Handle error

                Log.e(TAG, "Error Upload Banner Image: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = userId
                params["image"] = image
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

     fun updateUser(userId: String,updates: Map<String, String>) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "update_user.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show()

                //update current user


            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error updating user: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                for ((key, value) in updates) {
                    params[key] = value
                }
                params["id"] = userId
                return params
            }

        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }






}

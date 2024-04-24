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
import com.muhammadfarazrashid.i2106595.AllMessagesChat
import com.muhammadfarazrashid.i2106595.Mentor
import com.muhammadfarazrashid.i2106595.MentorItem
import com.muhammadfarazrashid.i2106595.ReviewItem
import com.muhammadfarazrashid.i2106595.Session
import com.muhammadfarazrashid.i2106595.UserManager
import com.muhammadfarazrashid.i2106595.dataclasses.User
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

//using volley
class WebserviceHelper(private val context: Context) {

    private val BASE_URL = "http://192.168.18.54/"

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


    fun addToFavourite(userId: String, mentorId: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "add_to_favourites.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Mentor added to favourites", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error adding mentor to favourites: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"]= UUID.randomUUID().toString()
                params["userId"] = userId
                params["mentorId"] = mentorId
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //remove from favourites

    fun removeFromFavourite(userId: String, mentorId: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "remove_from_favourites.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Mentor removed from favourites", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error removing mentor from favourites: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId
                params["mentorId"] = mentorId
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    // get favourites, we will pass userId as parameter and we will get a list of mentorIds

    fun getFavourites(userId: String, callback: FavouritesCallback) {
        val url = BASE_URL + "get_favourite_mentors.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonArray = JSONArray(response)
                    val mentorIds = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val mentorId = jsonObject.getString("mentorId")
                        mentorIds.add(mentorId)
                    }
                    callback.onSuccess(mentorIds)
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}")
                    callback.onError(null)
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error getting favourites: ${error.message}")
                callback.onError(null)
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(stringRequest)
    }

    //write code to add bookings

    fun addBooking(userId: String, mentorId: String, bookingDate: String, bookingTime: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "add_booking.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Booking added successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error adding booking: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = UUID.randomUUID().toString()
                params["userId"] = userId
                params["mentorId"] = mentorId
                params["bookingDate"] = bookingDate
                params["bookingTime"] = bookingTime
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //get booking and return list of sessions

    fun getBookings(userId: String, callback: (MutableList<Session>?) -> Unit){
        val url = BASE_URL + "get_booking.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.has("success") && jsonObject.getInt("success") == 1) {
                        val bookingsArray = jsonObject.getJSONArray("bookings")
                        val sessions = mutableListOf<Session>()
                        for (i in 0 until bookingsArray.length()) {
                            val bookingObject = bookingsArray.getJSONObject(i)
                            val bookingDate = bookingObject.getString("bookingDate")
                            val bookingTime = bookingObject.getString("bookingTime")
                            val mentorId = bookingObject.getString("mentorId")
                            val session = Session(bookingDate, bookingTime, mentorId)
                            sessions.add(session)
                        }
                        callback(sessions)
                    } else {
                        callback(null)
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}")
                    callback(null)
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error getting bookings: ${error.message}")
                callback(null)
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(stringRequest)
    }

    //write code to add review

    fun addReview(userId: String, mentorId: String, rating: Float, review: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "add_review.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Review added successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error adding review: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = UUID.randomUUID().toString()
                params["userId"] = userId
                params["mentorId"] = mentorId
                params["rating"] = rating.toString()
                params["reviewText"] = review
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //write code to get reviews

    fun getReviews(userId: String, callback: (MutableList<ReviewItem>?) -> Unit){
        val url = BASE_URL + "get_review.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.has("success") && jsonObject.getInt("success") == 1) {
                        val reviewsArray = jsonObject.getJSONArray("reviews")
                        val reviews = mutableListOf<ReviewItem>()
                        for (i in 0 until reviewsArray.length()) {
                            Log.d(TAG, "Review: $i")
                            val reviewObject = reviewsArray.getJSONObject(i)
                            val rating = reviewObject.getString("rating").toFloat()
                            val reviewText = reviewObject.getString("reviewText")
                            val mentorName = reviewObject.getString("name") // Fetch mentor's name
                            val review = ReviewItem(mentorName, reviewText, rating) // Include mentor's name in ReviewItem
                            reviews.add(review)
                        }
                        callback(reviews)
                    } else {
                        callback(null)
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}")
                    callback(null)
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error getting reviews: ${error.message}")
                callback(null)
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(stringRequest)
    }

    //register for community chat, pass mentor id as id and userId as userId

    fun registerForCommunityChat(userId: String, mentorId: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "register_for_community_chat.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Registered for community chat", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error registering for community chat: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = mentorId
                params["userId"] = userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //register for mentor chat similar to above, but add mentor as param as well

    fun registerForMentorChat(userId: String, mentorId: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "register_for_mentor_chat.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Registered for mentor chat", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error registering for mentor chat: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["mentorId"] = mentorId
                params["userId"] = userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //fetch mentor chat and get mentor objects and return them
    fun fetchMentorChats(userId: String, callback: (MutableList<AllMessagesChat>) -> Unit) {
        val url = BASE_URL + "fetch_mentor_chat.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getInt("success")

                    if (success == 1) {
                        val chatsJsonArray = jsonObject.getJSONArray("chats")
                        val chats = mutableListOf<AllMessagesChat>()
                        for (i in 0 until chatsJsonArray.length()) {
                            val chatJsonObject = chatsJsonArray.getJSONObject(i)
                            val mentor = Mentor(
                                chatJsonObject.getString("id"),
                                chatJsonObject.getString("name"),
                                chatJsonObject.getString("position"),
                                chatJsonObject.getString("availability"),
                                chatJsonObject.getString("salary"),
                                chatJsonObject.getString("description"),
                                BASE_URL + "Images/MentorMe/" + chatJsonObject.getString("profilePictureUrl")
                            )
                            var available = false
                            if (mentor.availability == "available")
                                available = true

                            Log.d("FetchMentorChats", "Mentor Name: ${mentor.name} Mentor ID: ${mentor.id} Mentor Position: ${mentor.position} Mentor Availability: ${mentor.availability} Mentor Salary: ${mentor.salary} Mentor Description: ${mentor.description} Mentor Profile Picture URL: ${mentor.getprofilePictureUrl()}")

                            val mentorItem = AllMessagesChat(mentor, mentor.id, mentor.id+"_"+ (UserManager.getCurrentUser()?.id
                                ?: ""),mentor.name, mentor.getprofilePictureUrl())
                            chats.add(mentorItem)
                        }
                        callback(chats)
                    } else {
                        callback(emptyList<AllMessagesChat>().toMutableList())
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    callback(emptyList<AllMessagesChat>().toMutableList())
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error fetching community chats: ${error.message}")
                callback(emptyList<AllMessagesChat>().toMutableList())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(stringRequest)
    }

    fun fetchCommunityChats(userId: String, callback: (List<MentorItem>) -> Unit) {
        val url = BASE_URL + "fetch_community_chats.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getInt("success")

                    if (success == 1) {
                        val chatsJsonArray = jsonObject.getJSONArray("chats")
                        val chats = mutableListOf<MentorItem>()
                        for (i in 0 until chatsJsonArray.length()) {
                            val chatJsonObject = chatsJsonArray.getJSONObject(i)
                            val mentor = Mentor(
                                chatJsonObject.getString("id"),
                                chatJsonObject.getString("name"),
                                chatJsonObject.getString("position"),
                                chatJsonObject.getString("availability"),
                                chatJsonObject.getString("salary"),
                                chatJsonObject.getString("description"),
                                BASE_URL + "Images/MentorMe/" + chatJsonObject.getString("profilePictureUrl")
                            )
                            var available = false
                            if (mentor.availability == "available")
                                available = true


                            Log.d("FetchCommunityChats", "Mentor Name: ${mentor.name} Mentor ID: ${mentor.id} Mentor Position: ${mentor.position} Mentor Availability: ${mentor.availability} Mentor Salary: ${mentor.salary} Mentor Description: ${mentor.description} Mentor Profile Picture URL: ${mentor.getprofilePictureUrl()}")

                            val mentorItem = MentorItem(mentor, mentor.id, mentor.getprofilePictureUrl(), available)
                            chats.add(mentorItem)
                        }
                        callback(chats)
                    } else {
                        callback(emptyList())
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    callback(emptyList())
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error fetching community chats: ${error.message}")
                callback(emptyList())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(stringRequest)
    }





    interface FavouritesCallback {
        fun onSuccess(favourites: List<String?>?)
        fun onError(errorMessage: String?)
    }




}

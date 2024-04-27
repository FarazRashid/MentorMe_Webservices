package com.muhammadfarazrashid.i2106595.managers

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.muhammadfarazrashid.i2106595.AllMessagesChat
import com.muhammadfarazrashid.i2106595.ChatMessage
import com.muhammadfarazrashid.i2106595.Mentor
import com.muhammadfarazrashid.i2106595.MentorItem
import com.muhammadfarazrashid.i2106595.ReviewItem
import com.muhammadfarazrashid.i2106595.Session
import com.muhammadfarazrashid.i2106595.UserManager
import com.muhammadfarazrashid.i2106595.dataclasses.User
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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

    //send message in mentor chat function and pass it
    // the message object in parms. It will  check if
    // any of the properties are not null and then push it to the database
    //e.g check if messageImarUrl is not "" or videoImageUrl is not "" etc.

    fun sendMessageInMentorChat(message: ChatMessage,mentor:Mentor) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "send_message_in_mentor_chat.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error sending message: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["chatId"] = mentor.id+"_"+(UserManager.getCurrentUser()?.id
                    ?: "")
                params["messageId"] = message.id
                params["userId"] = UserManager.getCurrentUser()?.id ?: ""
                params["time"] = message.time
                message.message?.let { if
                        (it.isNotEmpty()) params["message"] = it }
                message.messageImageUrl?.let { if (it.isNotEmpty()) params["messageImageUrl"] = it }
                message.videoImageUrl?.let { if (it.isNotEmpty()) params["videoImageUrl"] = it }
                message.voiceMemoUrl?.let { if (it.isNotEmpty()) params["voiceMemoUrl"] = it }
                message.documentUrl?.let { if (it.isNotEmpty()) params["documentUrl"] = it }
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun sendMessageInCommunityChat(message: ChatMessage,mentor:Mentor) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "send_message_in_community_chat.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error sending message: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["chatId"] = mentor.id
                params["messageId"] = message.id
                params["userId"] = UserManager.getCurrentUser()?.id ?: ""
                params["time"] = message.time
                message.message?.let { if (it.isNotEmpty()) params["message"] = it }
                message.messageImageUrl?.let { if (it.isNotEmpty()) params["messageImageUrl"] = it }
                message.videoImageUrl?.let { if (it.isNotEmpty()) params["videoImageUrl"] = it }
                message.voiceMemoUrl?.let { if (it.isNotEmpty()) params["voiceMemoUrl"] = it }
                message.documentUrl?.let { if (it.isNotEmpty()) params["documentUrl"] = it }
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //fetch messages in mentor chat function

    fun fetchMessagesInMentorChat(userId: String, mentorId: String, mentorImageUrl:String, callback: (List<ChatMessage>) -> Unit) {
        val url = BASE_URL + "fetch_messages_in_mentor_chat.php"
        Log.d("FetchMessages", "chatId= $mentorId"+"_"+userId)
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getInt("success")

                    if (success == 1) {
                        val messagesJsonArray = jsonObject.getJSONArray("messages")
                        val messages = mutableListOf<ChatMessage>()
                        for (i in 0 until messagesJsonArray.length()) {
                            val messageJsonObject = messagesJsonArray.getJSONObject(i)

                            var isUser=false
                            if(messageJsonObject.getString("userId")==userId)
                                isUser=true

                            val message = ChatMessage(
                                messageJsonObject.getString("messageId"),
                                messageJsonObject.optString("message", ""),
                                messageJsonObject.getString("time"),
                                isUser,
                                mentorImageUrl,
                                messageJsonObject.optString("messageImageUrl", ""), // Use optString instead of getString
                                messageJsonObject.optString("videoImageUrl", ""), // Use optString instead of getString
                                messageJsonObject.optString("voiceMemoUrl", ""), // Use optString instead of getString
                                messageJsonObject.optString("documentUrl", "") // Use optString instead of getString
                            )
                            Log.d("FetchMessages", "Message: ${message.message} Message ID: ${message.id} Time: ${message.time} Is User: ${message.isUser} Message Image URL: ${message.messageImageUrl} Video Image URL: ${message.videoImageUrl} Voice Memo URL: ${message.voiceMemoUrl} Document URL: ${message.documentUrl}")
                            messages.add(message)
                        }
                        callback(messages)
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
                Log.e("Fetch messages service", "Error fetching messages: ${error.message}")
                callback(emptyList())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["chatId"]=mentorId+"_"+userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(stringRequest)
    }

    fun fetchMessagesInCommunityChat(userId: String, mentorId: String, mentorImageUrl:String, userList: ArrayList<User>, callback: (List<ChatMessage>) -> Unit) {
        val url = BASE_URL + "fetch_messages_in_community_chats.php"
        Log.d("FetchMessages", "chatId= $mentorId")
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getInt("success")

                    if (success == 1) {
                        val messagesJsonArray = jsonObject.getJSONArray("messages")
                        val messages = mutableListOf<ChatMessage>()
                        for (i in 0 until messagesJsonArray.length()) {
                            val messageJsonObject = messagesJsonArray.getJSONObject(i)
                            val messageUserId=messageJsonObject.getString("userId")

                            var isUser=false
                            if(messageUserId==userId)
                                isUser=true
                            var mentorImageUrl1=mentorImageUrl

                            if(!isUser && messageUserId!=mentorId){
                                for(user in userList){
                                    if(user.id==messageUserId){
                                        mentorImageUrl1=user.profilePictureUrl
                                    }
                                }
                            }

                            val message = ChatMessage(
                                messageJsonObject.getString("messageId"),
                                messageJsonObject.optString("message", ""),
                                messageJsonObject.getString("time"),
                                isUser,
                                mentorImageUrl1,
                                messageJsonObject.optString("messageImageUrl", ""), // Use optString instead of getString
                                messageJsonObject.optString("videoImageUrl", ""), // Use optString instead of getString
                                messageJsonObject.optString("voiceMemoUrl", ""), // Use optString instead of getString
                                messageJsonObject.optString("documentUrl", "") // Use optString instead of getString
                            )
                            Log.d("FetchMessages", "Message: ${message.message} Message ID: ${message.id} Time: ${message.time} Is User: ${message.isUser} Message Image URL: ${message.messageImageUrl} Video Image URL: ${message.videoImageUrl} Voice Memo URL: ${message.voiceMemoUrl} Document URL: ${message.documentUrl}")
                            messages.add(message)
                        }
                        callback(messages)
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
                Log.e("Fetch messages service", "Error fetching messages: ${error.message}")
                callback(emptyList())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["chatId"]=mentorId
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(stringRequest)
    }

    //edit message

    fun editMessageInMentorChat(messageId: String, message: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "edit_message_mentor_chat.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Message edited successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error editing message: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["messageId"] = messageId
                params["message"] = message
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun editMessageInCommunityChat(messageId: String, message: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "edit_message_mentor_chat.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Message edited successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error editing message: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["messageId"] = messageId
                params["message"] = message
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //delete message

    fun deleteMessage(messageId: String,chatType:String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "delete_message_"+ chatType+".php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Message deleted successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error deleting message: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["messageId"] = messageId
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }


    interface FavouritesCallback {
        fun onSuccess(favourites: List<String?>?)
        fun onError(errorMessage: String?)
    }

    //write function get user with email and return user with callback

    fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "get_user_with_email.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                val user = parseResponseToUser(response)
                callback(user)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error getting user by email: ${error.message}")
                callback(null)
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //write function to upload file to the server, by first converting it to base64 string and then uploading it
    // the user will pass a "image_type" as a parameter to specify the type of image being uploaded
    //e.g imageUrl, voiceMemoUrl, videoUrl, documentUrl etc.
    //after the file has been succesfully uploaded, the message will be sent to the mentor chat with the file url

    private fun encodeFileToBase64(fileUri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val bytes = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
            bytes.write(buffer, 0, bytesRead)
        }
        val base64 = Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT)
        inputStream?.close()
        return base64
    }

    fun uploadFileToServer(fileType: String, fileUri: Uri, callback: (Boolean, String?) -> Unit) {
        val url = BASE_URL + "upload_file_to_server.php"
        val queue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                // Handle successful response
                Toast.makeText(context, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                Log.d("API Response", response)

                // Extract file URL from response
                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                var fileUrl = if (success) jsonResponse.getString("fileUrl") else null
                Log.d("API Response", "File URL: $fileUrl")
                // Notify the callback with success and file URL
                fileUrl= BASE_URL  + fileUrl
                callback(success, fileUrl)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("API Error", "Error occurred: ${error.message}")

                // Notify the callback that insertion failed
                callback(false, null)
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                when (fileType) {
                    "image" -> {
                        val encodedImage = encodeFileToBase64(fileUri)
                        params["file"] = encodedImage
                        params["file_type"] = "image"
                    }
                    "audio" -> {
                        val encodedAudio = encodeFileToBase64(fileUri)
                        params["file"] = encodedAudio
                        params["file_type"] = "audio"
                    }
                    "video" -> {
                        val encodedVideo = encodeFileToBase64(fileUri)
                        params["file"] = encodedVideo
                        params["file_type"] = "video"
                    }
                    "document" -> {
                        val encodedDocument = encodeFileToBase64(fileUri)
                        params["file"] = encodedDocument
                        params["file_type"] = "document"
                    }
                }
                return params
            }
        }
        queue.add(stringRequest)
    }

    //fetch all users in community chat and return them with callback

    fun fetchAllUsersInCommunityChat(chatId: String, callback: (List<User>) -> Unit) {
        val url = BASE_URL + "fetch_all_users_in_community_chat.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    Log.d("FetchUsers", "Response: $jsonObject")
                    if (jsonObject.has("status")) { // Check for "status"
                            val usersJsonArray = jsonObject.getJSONArray("users")
                            val users = mutableListOf<User>()
                            for (i in 0 until usersJsonArray.length()) {
                                val userJsonObject = usersJsonArray.getJSONObject(i)
                                val user = User(
                                    userJsonObject.getString("id"),
                                    userJsonObject.getString("name"),
                                    userJsonObject.getString("email"),
                                    userJsonObject.getString("country"),
                                    userJsonObject.getString("city"),
                                    userJsonObject.getString("password"),
                                    userJsonObject.getString("phone"),
                                    BASE_URL + "Images/MentorMe/" + userJsonObject.getString("profilePictureUrl"),
                                    BASE_URL + "Images/MentorMe/" + userJsonObject.getString("bannerImageUrl"),
                                    userJsonObject.optString("fcmToken", "")
                                )
                                Log.d(
                                    "FetchUsers",
                                    "User Name: ${user.name} User ID: ${user.id} User Email: ${user.email} User Country: ${user.country} User City: ${user.city} User Password: ${user.password} User Phone: ${user.phone} User Profile Picture URL: ${user.profilePictureUrl} User Banner Image URL: ${user.bannerImageUrl}"
                                )
                                users.add(user)
                            }

                            callback(users)

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
                Log.e(TAG, "Error fetching users: ${error.message}")
                callback(emptyList())
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["chatId"] = chatId
                return params
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(stringRequest)
    }

    //add notifications to user

    fun addNotification(userId: String, notificationMessage: String,notificationType:String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "add_notification.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Notification added successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error adding notification: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = UUID.randomUUID().toString()
                params["userId"] = userId
                params["notification"] = notificationMessage
                params["notificationType"]= notificationType
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun getNotifications(userId: String, callback: (List<Notification>) -> Unit) {
        val url = BASE_URL + "get_notifications.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")

                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.has("status")) {
                        val notificationsArray = jsonObject.getJSONArray("notifications")
                        val notifications = mutableListOf<Notification>()
                        for (i in 0 until notificationsArray.length()) {
                            val notificationObject = notificationsArray.getJSONObject(i)
                            val notification = Notification(
                                notificationObject.getString("id"),
                                notificationObject.getString("userId"),
                                notificationObject.getString("notification"),
                                notificationObject.getString("notificationType")
                            )
                            notifications.add(notification)
                        }
                        callback(notifications)
                    } else {
                        callback(emptyList())
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parsing error: ${e.message}")
                    callback(emptyList())
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error getting notifications: ${error.message}")
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

    //delete notification

    fun deleteNotification(notificationId: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "delete_notification.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "Notification deleted successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error deleting notification: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = notificationId
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //delete all messages of user

    fun deleteAllMessagesOfUser(userId: String) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "delete_all_notifcations_of_user.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response
                Log.d(TAG, "Response: $response")
                Toast.makeText(context, "All messages deleted successfully", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error deleting all messages: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    //get mentor by id without a parseresponsetomentor

    fun getMentorById(mentorId: String, callback: (Mentor?) -> Unit) {
        val queue = Volley.newRequestQueue(context)

        val url = BASE_URL + "get_mentor_by_id.php" // Replace with your server URL

        val stringRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                val jsonObject = JSONObject(response)
                Log.d(TAG, "Response: $response")
                val mentor=Mentor(
                    mentorId,
                    jsonObject.getString("name"),
                    jsonObject.getString("position"),
                    jsonObject.getString("availability"),
                    jsonObject.getString("salary"),
                    jsonObject.getString("description"),
                    BASE_URL + "Images/MentorMe/" + jsonObject.getString("profilePictureUrl")
                )
                callback(mentor)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e(TAG, "Error getting mentor by id: ${error.message}")
                callback(null)
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = mentorId
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }





    data class Notification(
        val id: String,
        val userId: String,
        val notification: String,
        val notificationType: String
    )

}

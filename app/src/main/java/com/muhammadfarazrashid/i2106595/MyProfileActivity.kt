package com.muhammadfarazrashid.i2106595


import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.muhammadfarazrashid.i2106595.dataclasses.User
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.IOException


class MyProfileActivity : AppCompatActivity() {

    private lateinit var reviewAdapter: ReviewItemAdapter
    private lateinit var topMentorsRecycler: RecyclerView
    private lateinit var topMentors: ArrayList<Mentor>
    private var currentUser: User = UserManager.getCurrentUser()!!
    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var name: TextView
    private lateinit var city: TextView
    private lateinit var banner: ImageView
    private lateinit var profilePicture: ImageView
    private lateinit var editProfilePicture: ImageView
    private lateinit var editProfileBanner: ImageView
    private var selectedImageRequestCode: Int = 0
    private var isImageSelectionInProgress = false
    private lateinit var logoutButton:ImageView
    private var selectedImage: String? = null // Variable to store base64 encoded image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)


        Log.d(TAG, "onCreate: ${currentUser.id}")
        initReviewRecyclerView()
        initViews()
        fetchReviewsData()
        loadUserInformationWithoutFirebase()
        fetchUserFavorites()
        initBottomNavigation()
        setOnClickListeners()

    }

    private fun initViews() {
        name = findViewById(R.id.nameText)
        city = findViewById(R.id.location)
        banner = findViewById(R.id.bannerImage)
        profilePicture = findViewById(R.id.userProfilePhoto)
        editProfilePicture = findViewById(R.id.editProfilePicture)
        editProfileBanner = findViewById(R.id.editBanner)
        logoutButton = findViewById(R.id.logoutButton)
    }

    private fun fetchReviewsData() {
        val userId = currentUser.id
        val reviewsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("reviews")

        reviewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reviewList = mutableListOf<ReviewItem>()
                for (reviewSnapshot in snapshot.children) {
                    val mentorName = reviewSnapshot.child("mentorName").getValue(String::class.java)
                    val rating = reviewSnapshot.child("rating").getValue(Float::class.java)
                    val comment = reviewSnapshot.child("reviewText").getValue(String::class.java)
                    rating?.let { r ->
                        comment?.let { c ->
                            val review = ReviewItem(mentorName, c, r)
                            reviewList.add(review)
                        }
                    }
                }
                    initReviewRecyclerView(reviewList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchReviews", "Failed to fetch reviews: ${error.message}")
            }
        })
    }



    private fun initReviewRecyclerView(review: List<ReviewItem> = mutableListOf()) {
        val myReviewsRecycler = findViewById<RecyclerView>(R.id.myReviewsRecycler)
        reviewAdapter = ReviewItemAdapter(review)
        myReviewsRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        myReviewsRecycler.adapter = reviewAdapter

    }


    private fun initBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemReselectedListener { item ->
            when (item.itemId) {
                R.id.menu_search -> startActivity(Intent(this, searchPageActivity::class.java))
                R.id.menu_home -> startActivity(Intent(this, homePageActivity::class.java))
                R.id.menu_chat -> startActivity(Intent(this, mainChatActivity::class.java))
                R.id.menu_profile -> startActivity(Intent(this, MyProfileActivity::class.java))
            }
        }
    }

    private fun setOnClickListeners() {
        val addMentor = findViewById<ImageView>(R.id.addMentorButton)
        addMentor.setOnClickListener {
            startActivity(Intent(this, AddAMentor::class.java))
        }

        val bookedSessions = findViewById<View>(R.id.bookedSessions)
        bookedSessions.setOnClickListener {
            startActivity(Intent(this, BookedSessionsActivity::class.java))
        }

        val editProfile = findViewById<View>(R.id.editProfile)
        editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfilePageActivity::class.java))
        }

        val backButton = findViewById<Button>(R.id.backbutton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        profilePicture.setOnClickListener {
            editProfilePicture()
        }

        banner.setOnClickListener {
            editProfileBanner()
        }

        logoutButton.setOnClickListener {
            logout()
            startActivity(Intent(this, loginActivity::class.java))
            finish()
        }
    }
    private fun logout()
    {
        UserManager.saveUserLoggedInSP(false, getSharedPreferences("USER_LOGIN", MODE_PRIVATE))
        UserManager.saveUserEmailSP("", getSharedPreferences("USER_LOGIN", MODE_PRIVATE))
        mAuth.signOut()

        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("Logout", "Token deleted")
            }
        }

    }

    private fun fetchUserFavorites() {
        val webserviceHelper = WebserviceHelper(this)
        val userId = UserManager.getInstance().getCurrentUser()?.id ?: ""

        webserviceHelper.getFavourites(userId, object : WebserviceHelper.FavouritesCallback {

            override fun onSuccess(favourites: List<String?>?) {
                if (favourites != null) {
                    fetchMentors(favourites.filterNotNull())
                }
            }

            override fun onError(errorMessage: String?) {
                Log.e("FetchFavorites", "Failed to fetch favorites: $errorMessage")
            }
        })
    }

    private fun fetchMentors(favoriteMentorIds: List<String>) {
        val webserviceHelper = WebserviceHelper(this)
        webserviceHelper.getMentors { mentors ->
            if (mentors != null) {
                val favoriteMentors = mentors.filter { mentor -> favoriteMentorIds.contains(mentor.id) }
                initTopMentorsRecyclerView(favoriteMentors)


            }
            else{
                Log.d("homePageActivity", "Failed to fetch mentors")
            }

        }
    }


    private fun initTopMentorsRecyclerView(mentors: List<Mentor>) {
        topMentorsRecycler = findViewById(R.id.topMentorsRecycler)
        topMentorsRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        if(mentors.isEmpty()) {
            findViewById<TextView>(R.id.favoriteMentorsText).visibility = View.GONE
        }
        val horizontalLayoutResourceId = R.layout.mentorcard
        val topMentorsAdapter = MentorCardAdapter(this, mentors, horizontalLayoutResourceId)
        topMentorsRecycler.adapter = topMentorsAdapter
    }


    override fun onResume() {
        super.onResume()
        loadUserInformationWithoutFirebase()
    }



    private fun loadUserInformationWithoutFirebase() {
        val currentUser = UserManager.getCurrentUser()
        if (currentUser != null) {

            Log.d(ContentValues.TAG, "loadUserInformation: ${currentUser.id}")
            name.setText(currentUser.name)
            city.setText(currentUser.city)
            Log.d("LoadUserInformation", "Profile Picture URL: ${currentUser.profilePictureUrl}")
            if (currentUser.profilePictureUrl.isNotEmpty()) {
                Picasso.get().load(currentUser.profilePictureUrl).into(profilePicture)
            }
            if(currentUser.bannerImageUrl.isNotEmpty())
                Picasso.get().load(currentUser.bannerImageUrl).into(banner)


        }
    }

//    private fun loadUserInformation() {
//        val currentUser = UserManager.getCurrentUser()
//        if (currentUser != null) {
//
//            Log.d(ContentValues.TAG, "loadUserInformation: ${currentUser.id}")
//            name.setText(currentUser.name)
//            city.setText(currentUser.city)
//            Log.d("LoadUserInformation", "Profile Picture URL: ${currentUser.profilePictureUrl}")
//            if (currentUser.profilePictureUrl.isNotEmpty())
//                retrieveImageFromFirebaseStorage(this,"profile_picture", profilePicture)
//            if(currentUser.bannerImageUrl.isNotEmpty())
//                Picasso.get().load(currentUser.bannerImageUrl).into(banner)
//
//
//        }
//    }

//    private fun retrieveImageFromFirebaseStorage(context: Context, imageType: String, imageView: ImageView) {
//        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
//        if (currentUserUid != null) {
//            // Define the reference to the image in Firebase Storage
//            val imageRef = storageReference.child("profile_images").child("$currentUserUid/$imageType.jpg")
//
//            // Get the download URL of the image
//            imageRef.downloadUrl.addOnSuccessListener { uri ->
//                // Log the download URL for debugging
//                Log.d("RetrieveImage", "Download URL: $uri")
//
//                // Check if the image is loaded from cache or fetched from network
//                val startTime = System.currentTimeMillis()
//                Picasso.get().load(uri)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE)
//                    .networkPolicy(NetworkPolicy.OFFLINE)
//                    .into(imageView, object : Callback {
//                        override fun onSuccess() {
//                            val endTime = System.currentTimeMillis()
//                            val duration = endTime - startTime
//                            Log.d("RetrieveImage", "Image loaded from network in $duration ms")
//                        }
//
//                        override fun onError(e: Exception?) {
//                            Picasso.get().load(uri).into(imageView)
//                        }
//                    })
//            }.addOnFailureListener { e ->
//                // Handle any errors that occur during download
//                Log.e("RetrieveImage", "Failed to retrieve image: $e")
//            }
//        }
//    }

    private fun encodeImage(bitmap: Bitmap?): String? {
        bitmap?.let {
            val byteArrayOutputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
            return encodedImage
        }
        return null
    }


//    private fun uploadImageToFirebaseStorage(imageUri: Uri, imageType: String, selectedImageUri: Uri) {
//        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
//        if (currentUserUid != null) {
//            // Define path for the image in Firebase Storage
//            val filePath =
//                storageReference.child("profile_images").child("$currentUserUid/$imageType.jpg")
//
//            // Upload image to Firebase Storage
//            filePath.putFile(imageUri)
//                .addOnSuccessListener { taskSnapshot ->
//                    // Image uploaded successfully, get the download URL
//                    filePath.downloadUrl.addOnSuccessListener { uri ->
//                        // Save the download URL to Firebase Realtime Database or Firestore
//                        saveImageUrlToDatabase(uri.toString(), imageType, selectedImageUri)
//                        UserManager.setUserUrl(uri.toString())
//                    }
//                }
//                .addOnFailureListener { e ->
//                    // Image upload failed, handle the error
//                    Log.e("UploadImage", "Failed to upload image: $e")
//                    Snackbar.make(
//                        findViewById(android.R.id.content),
//                        "Failed to upload image",
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                }
//        }
//    }
//
//    private fun saveImageUrlToDatabase(imageUrl: String, imageType: String, selectedImageUri: Uri) {
//        // Save the image URL to Firebase Realtime Database or Firestore under the user's profile
//        // For example, if you're using Realtime Database:
//        val currentUserUid = UserManager.getCurrentUser()?.id
//        if (currentUserUid != null) {
//            val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserUid)
//            val imageField = if (imageType == "profile_picture") "profilePictureUrl" else "bannerImageUrl"
//            databaseReference.child(imageField).setValue(imageUrl)
//                .addOnSuccessListener {
//                    Snackbar.make(findViewById(android.R.id.content), "Image uploaded successfully", Snackbar.LENGTH_SHORT).show()
//
//                    // Image URL saved successfully, no need to load the image from database
//                    when (imageType) {
//                        "profile_picture" -> {
//                            // Update the profile picture ImageView with the locally selected image
//                            profilePicture.setImageURI(selectedImageUri)
//                            Picasso.get().load(selectedImageUri).into(profilePicture)
//                        }
//                        "banner" -> {
//                            // Update the banner ImageView with the locally selected image
//                            banner.setImageURI(selectedImageUri)
//                            Picasso.get().load(selectedImageUri).into(banner)
//                        }
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("SaveImageUrl", "Failed to save image URL to database: $e")
//                    Snackbar.make(findViewById(android.R.id.content), "Failed to save image URL", Snackbar.LENGTH_SHORT).show()
//                }
//        }
//    }


    private val pickImageGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            try {
                selectedImageUri?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    selectedImage = encodeImage(bitmap)
                    // Set the selected image to the corresponding ImageView
                    when (selectedImageRequestCode) {
                        REQUEST_CODE_PROFILE_PICTURE -> {

                            val webserviceHelper = WebserviceHelper(this)
                            selectedImage?.let {
                                webserviceHelper.uploadProfilePicture(currentUser.id,
                                    it
                                )
                                profilePicture.setImageURI(uri)
                                Picasso.get().load(uri).into(profilePicture)
                            }
                        }
                        REQUEST_CODE_BANNER -> {

                            val webserviceHelper = WebserviceHelper(this)
                            selectedImage?.let {
                                webserviceHelper.uploadBannerImage(currentUser.id,
                                    it
                                )
                                banner.setImageURI(uri)
                                Picasso.get().load(uri).into(banner)
                            }


                        }

                        else -> {
                            Snackbar.make(findViewById(android.R.id.content), "Invalid request code", Snackbar.LENGTH_SHORT).show()}
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Snackbar.make(findViewById(android.R.id.content), "Error loading image", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun editProfilePicture() {
        isImageSelectionInProgress = true
        selectedImageRequestCode = REQUEST_CODE_PROFILE_PICTURE
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageGalleryLauncher.launch(galleryIntent)

    }

    private fun editProfileBanner() {
        isImageSelectionInProgress = true
        selectedImageRequestCode = REQUEST_CODE_BANNER
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageGalleryLauncher.launch(galleryIntent)
    }



    companion object {
        private const val REQUEST_CODE_PROFILE_PICTURE = 1
        private const val REQUEST_CODE_BANNER = 2
    }
}
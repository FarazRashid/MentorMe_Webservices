package com.muhammadfarazrashid.i2106595

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhammadfarazrashid.i2106595.managers.WebserviceHelper

class searchResultsActivity : AppCompatActivity() {

    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchResultsAdapter: MentorCardAdapter
    private lateinit var topMentors: ArrayList<Mentor>
    private lateinit var originalTopMentors: ArrayList<Mentor>
    private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.searchresults)

        //get intent data
        val intent = intent
        searchQuery = intent.getStringExtra("search_query")
        Log.d("searchResultsActivity", "Search query: $searchQuery")
        fetchUserFavorites()
        setupFilterSpinner()
        setupBottomNavigation()
        setupAddMentorButton()
        setupBackButton()
        setupCardClickListener()
    }

    private fun fetchUserFavorites() {
    val webserviceHelper = WebserviceHelper(this)
    val userId = UserManager.getInstance().getCurrentUser()?.id ?: ""

    webserviceHelper.getFavourites(userId, object : WebserviceHelper.FavouritesCallback {
        override fun onSuccess(favourites: List<String?>?) {
            if (favourites != null) {
                initializeTopMentors(favourites.filterNotNull())
            }
        }

        override fun onError(errorMessage: String?) {
            Log.e("searchResultsActivity", "Error getting favorites: $errorMessage")
        }
    })
}

    //fetch mentors that have the search query in their name or position
    private fun initializeTopMentors(favoriteMentorIds: List<String>) {
        val webserviceHelper = WebserviceHelper(this)

        webserviceHelper.getMentors { mentors ->
            if (mentors != null) {
                topMentors = ArrayList()
                originalTopMentors = ArrayList()

                for (mentor in mentors) {
                    if (mentor.name.contains(searchQuery.toString(), ignoreCase = true) ||
                        mentor.position.contains(searchQuery.toString(), ignoreCase = true)) {
                        Log.d("searchResultsActivity", "Mentor ${mentor.id} matches search query")

                        if (favoriteMentorIds.contains(mentor.id)) {
                            mentor.isFavorite = true
                            Log.d("searchResultsActivity", "Mentor ${mentor.id} is a favorite")
                        }

                        topMentors.add(mentor)
                        originalTopMentors.add(mentor)
                    }
                }

                setupSearchResultsRecyclerView()
            } else {
                Log.e("searchResultsActivity", "Error getting data")
            }
        }
    }

    private fun setupSearchResultsRecyclerView() {
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        val verticalLayoutResourceId = R.layout.verticalmentorcards
        searchResultsAdapter = MentorCardAdapter(this, topMentors, verticalLayoutResourceId)
        searchResultsAdapter.setOnItemClickListener { mentor ->
            navigateToMentorAbout(mentor)
        }
        searchResultsRecyclerView.adapter = searchResultsAdapter
    }

    private fun setupFilterSpinner() {
        val items = listOf("Filter", "Available", "Not Available")
        val spinner: Spinner = findViewById(R.id.filterSpinner)
        val adapter = CustomSpinnerAdapter(this, items)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {onNothingSelected(spinner)} // Filter option, do nothing
                    1 -> filterMentors(true) // Available
                    2 -> filterMentors(false) // Not Available
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //if search results adapter has been initialized, update the list with the original list
                if (::searchResultsAdapter.isInitialized) {
                    searchResultsAdapter.updateList(originalTopMentors)
                }

            }
        }
    }

    private fun filterMentors(isAvailable: Boolean) {
        val filteredMentors = if (isAvailable) {
            topMentors.filter { it.availability == "Available" }
        } else if(!isAvailable) {
            topMentors.filter { it.availability == "Not Available" }
        }else{
            originalTopMentors
        }
        searchResultsAdapter.updateList(filteredMentors)
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemReselectedListener { item ->
            val intent = when (item.itemId) {
                R.id.menu_search -> Intent(this, searchPageActivity::class.java)
                R.id.menu_home -> Intent(this, homePageActivity::class.java)
                R.id.menu_chat -> Intent(this, mainChatActivity::class.java)
                R.id.menu_profile -> Intent(this, MyProfileActivity::class.java)
                else -> null
            }
            intent?.let {
                startActivity(it)
            }
        }
    }

    private fun setupAddMentorButton() {
        val addMentor = findViewById<ImageView>(R.id.addMentorButton)
        addMentor.setOnClickListener {
            val intent = Intent(this, AddAMentor::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToMentorAbout(mentor: Mentor) {
        val intent = Intent(this, aboutMentorPage::class.java)
        Log.d("searchPageActivity", "Navigating to aboutMentorPage with mentor: ${mentor.id}")
        Log.d("searchActivity", "Navigating to aboutMentorPage with mentor: ${mentor.getprofilePictureUrl()}")
        intent.putExtra("mentor", mentor) // Pass the mentor data to the aboutMentorPage
        startActivity(intent)
    }

    private fun setupBackButton() {
        val imageView4 = findViewById<ImageView>(R.id.imageView10)
        imageView4.setOnClickListener {
            onBackPressed()
        }
    }


    private fun setupCardClickListener() {
        if (::searchResultsAdapter.isInitialized) {
            searchResultsAdapter.setOnItemClickListener { position ->
                val intent = Intent(this, aboutMentorPage::class.java)
                startActivity(intent)
            }
        } else {
            Log.e("searchResultsActivity", "searchResultsAdapter is not initialized")
        }
    }


}

class CustomSpinnerAdapter(context: Context, items: List<String>) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        (view.findViewById(android.R.id.text1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.dropdownmenu))
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        (view.findViewById(android.R.id.text1) as TextView).setTextColor(ContextCompat.getColor(context, R.color.dropdownmenu))
        return view
    }
}

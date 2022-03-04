package com.example.movietime

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.movietime.data.Movie
import com.example.movietime.databinding.ActivityMainBinding
import com.example.movietime.ui.detail.EXTRA_MOVIE
import com.example.movietime.ui.detail.MovieDetailFragment
import com.example.movietime.ui.home.MovieListAdapter
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : AppCompatActivity() {

    private val apiBaseUrl = "https://api.themoviedb.org/3"
    private val apiKey = "c281ffcb75795819837f8b2643521195"

    private lateinit var requestQueue: RequestQueue
    private lateinit var binding: ActivityMainBinding
    private val movieAdapter = MovieListAdapter(::onMovieClick)
    private lateinit var searchResultsListRV: RecyclerView
    private lateinit var popularResultsListRV: RecyclerView
    private lateinit var searchErrorTV: TextView
    private lateinit var loadingIndicator: CircularProgressIndicator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestQueue = Volley.newRequestQueue(this)

        val searchBoxET: EditText = findViewById(R.id.et_search_box)
        val searchBtn: Button = findViewById(R.id.btn_search)

        searchErrorTV = findViewById(R.id.tv_search_error)
        loadingIndicator = findViewById(R.id.loading_indicator)

        searchResultsListRV = findViewById(R.id.rv_search_results)
        searchResultsListRV.layoutManager = LinearLayoutManager(this)
        searchResultsListRV.setHasFixedSize(true)

        searchResultsListRV.adapter = movieAdapter

        popularResultsListRV = findViewById(R.id.rv_popular_results)
        popularResultsListRV.layoutManager = LinearLayoutManager(this)
        popularResultsListRV.setHasFixedSize(true)

        popularResultsListRV.adapter = movieAdapter

        searchBtn.setOnClickListener {
            val query = searchBoxET.text.toString()
            if(!TextUtils.isEmpty(query)) {
                doMovieSearch(query)
                searchResultsListRV.scrollToPosition(0)
            }
        }

        popularMovies()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_discover, R.id.navigation_library, R.id.navigation_calendar
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_setting -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun popularMovies() {
        Log.d("popularMovies", "discover popular movies" )
        val url = "$apiBaseUrl/movie/popular?api_key=$apiKey&page=1"
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<MovieResults> =
            moshi.adapter(MovieResults::class.java)

        val req = StringRequest(
            Request.Method.GET,
            url,
            {
                Log.d("popular results", it)
                val results = jsonAdapter.fromJson(it)
                Log.d("movie popular result", results.toString())
                movieAdapter.updateMovieList(results?.results)
                //loadingIndicator.visibility = View.INVISIBLE
                popularResultsListRV.visibility = View.VISIBLE
            },
            {
                //loadingIndicator.visibility = View.INVISIBLE
                //searchErrorTV.visibility = View.VISIBLE
            }
        )
        requestQueue.add(req)
    }

    private fun doMovieSearch(q: String) {
        Log.d("movies search function", q)
        val url = "$apiBaseUrl/search/movie?api_key=$apiKey&query=$q&page=1"
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<MovieResults> =
            moshi.adapter(MovieResults::class.java)

        val req = StringRequest(
            Request.Method.GET,
            url,
            {
                Log.d("results", it)
                val results = jsonAdapter.fromJson(it)
                Log.d("movie result", results.toString())
                movieAdapter.updateMovieList(results?.results)
                loadingIndicator.visibility = View.INVISIBLE
                searchResultsListRV.visibility = View.VISIBLE
            },
            {
                loadingIndicator.visibility = View.INVISIBLE
                searchErrorTV.visibility = View.VISIBLE
            }
        )
        loadingIndicator.visibility = View.VISIBLE
        searchResultsListRV.visibility = View.INVISIBLE
        searchErrorTV.visibility = View.INVISIBLE
        requestQueue.add(req)
    }

    private fun onMovieClick(movie: Movie) {
        Log.d("click", "clicked")
        val intent = Intent(this, MovieDetailFragment::class.java).apply {
            putExtra(EXTRA_MOVIE, movie)
        } // create intent, name the class of the activity
        startActivity(intent) // start the activity
    }

    private data class MovieResults(
        val results: List<Movie>
    )

}

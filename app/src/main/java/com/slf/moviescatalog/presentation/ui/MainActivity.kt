package com.slf.moviescatalog.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.slf.moviescatalog.*
import com.slf.moviescatalog.presentation.ViewModel.UserViewModel
import com.slf.moviescatalog.data.MoviesRepository
import com.slf.moviescatalog.databinding.ActivityMainBinding
import com.slf.moviescatalog.data.model.Movie
import com.slf.moviescatalog.data.model.UserRepository
import com.slf.moviescatalog.presentation.adapter.MoviesAdapter
import com.slf.moviescatalog.utils.Constant
import com.slf.moviescatalog.utils.SharedHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var moviesRepository: MoviesRepository

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository
    private lateinit var shared: SharedHelper
    private lateinit var userViewModel: UserViewModel

    private lateinit var popularMoviesAdapter: MoviesAdapter
    private lateinit var topRatedMoviesAdapter: MoviesAdapter
    private lateinit var upcomingMoviesAdapter: MoviesAdapter

    private var popularMoviesPage = 1
    private var topRatedMoviesPage = 1
    private var upcomingMoviesPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()

        getPopularMovies()
        getTopRatedMovies()
        getUpcomingMovies()
    }

    override fun onStart() {
        super.onStart()
        shared = SharedHelper(this)
        userRepository = UserRepository(this)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        if (shared.getBoolean(Constant.LOGIN, false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                val getName = userRepository.getUser()
                launch(Dispatchers.Main) {
                    binding.showName.text = "${getString(R.string.hi_name)}, ${getName.name}"
                }
            }
        }
    }

    private fun setupRecyclerViews() {
        popularMoviesAdapter = MoviesAdapter(mutableListOf()) { movie -> showMovieDetails(movie) }
        binding.popularMovies.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularMoviesAdapter
            addOnScrollListener(createScrollListener { getPopularMovies() })
        }

        topRatedMoviesAdapter = MoviesAdapter(mutableListOf()) { movie -> showMovieDetails(movie) }
        binding.topRatedMovies.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = topRatedMoviesAdapter
            addOnScrollListener(createScrollListener { getTopRatedMovies() })
        }

        upcomingMoviesAdapter = MoviesAdapter(mutableListOf()) { movie -> showMovieDetails(movie) }
        binding.upcomingMovies.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = upcomingMoviesAdapter
            addOnScrollListener(createScrollListener { getUpcomingMovies() })
        }
    }

    private fun createScrollListener(loadMore: () -> Unit) = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val totalItemCount = layoutManager.itemCount
            val visibleItemCount = layoutManager.childCount
            val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

            if (firstVisibleItem + visibleItemCount >= totalItemCount / 2) {
                recyclerView.removeOnScrollListener(this)
                loadMore()
            }
        }
    }

    private fun getPopularMovies() {
        moviesRepository.getPopularMovies(popularMoviesPage, ::onPopularMoviesFetched, ::onError)
    }

    private fun onPopularMoviesFetched(movies: List<Movie>) {
        popularMoviesAdapter.appendMovies(movies)
        popularMoviesPage++
    }

    private fun getTopRatedMovies() {
        moviesRepository.getTopRatedMovies(topRatedMoviesPage, ::onTopRatedMoviesFetched, ::onError)
    }

    private fun onTopRatedMoviesFetched(movies: List<Movie>) {
        topRatedMoviesAdapter.appendMovies(movies)
        topRatedMoviesPage++
    }

    private fun getUpcomingMovies() {
        moviesRepository.getUpcomingMovies(upcomingMoviesPage, ::onUpcomingMoviesFetched, ::onError)
    }

    private fun onUpcomingMoviesFetched(movies: List<Movie>) {
        upcomingMoviesAdapter.appendMovies(movies)
        upcomingMoviesPage++
    }

    private fun showMovieDetails(movie: Movie) {
        val intent = Intent(this, MovieDetailsActivity::class.java).apply {
            putExtra(MOVIE_BACKDROP, movie.backdropPath)
            putExtra(MOVIE_POSTER, movie.posterPath)
            putExtra(MOVIE_TITLE, movie.title)
            putExtra(MOVIE_RATING, movie.rating)
            putExtra(MOVIE_RELEASE_DATE, movie.releaseDate)
            putExtra(MOVIE_OVERVIEW, movie.overview)
        }
        startActivity(intent)
    }

    private fun onError() {
        Toast.makeText(this, getString(R.string.error_fetch_movies), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.account_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.akun -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
                true
            }
            R.id.keluar -> {
                shared = SharedHelper(this)
                shared.clear()
                Toast.makeText(this, "User LogOut Successfully.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

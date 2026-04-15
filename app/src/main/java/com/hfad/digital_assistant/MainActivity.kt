package com.hfad.digital_assistant

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hfad.digital_assistant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavView = findViewById(R.id.bottom_nav)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupSwipeRefresh()

        swipeRefreshLayout.isRefreshing = false
        bottomNavView.visibility = View.GONE

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.registrationFragment -> {
                    bottomNavView.visibility = View.GONE
                    swipeRefreshLayout.isEnabled = false
                }

                R.id.mainFragment,
                R.id.routeFragment,
                R.id.practicumFragment,
                R.id.monitoringFragment,
                R.id.reflectionFragment -> {
                    bottomNavView.visibility = View.VISIBLE
                    swipeRefreshLayout.isEnabled = true
                }

                else -> {
                    bottomNavView.visibility = View.GONE
                    swipeRefreshLayout.isEnabled = false
                }
            }
        }

        bottomNavView.setupWithNavController(navController)
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshCurrentScreen()
        }

        swipeRefreshLayout.setOnChildScrollUpCallback { _, _ ->
            val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
            currentFragment?.view?.canScrollVertically(-1) ?: false
        }
    }

    private fun refreshCurrentScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            swipeRefreshLayout.isRefreshing = false
            recreate()
        }, 500)
    }
}
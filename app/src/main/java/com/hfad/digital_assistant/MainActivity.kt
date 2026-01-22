package com.hfad.digital_assistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hfad.digital_assistant.databinding.ActivityMainBinding
import androidx.navigation.NavController

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavView = binding.bottomNav

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Скрываем BottomNavigationView при запуске
        bottomNavView.visibility = android.view.View.GONE

        // Настраиваем слушатель для изменения видимости BottomNavigationView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.registrationFragment -> {
                    // Скрываем на экране регистрации
                    bottomNavView.visibility = android.view.View.GONE
                }
                R.id.mainFragment,
                R.id.routeFragment,
                R.id.practicumFragment,
                R.id.monitoringFragment,
                R.id.reflectionFragment -> {
                    // Показываем на всех остальных экранах
                    bottomNavView.visibility = android.view.View.VISIBLE
                }
                else -> {
                    // Для остальных фрагментов скрываем (если будут добавлены новые)
                    bottomNavView.visibility = android.view.View.GONE
                }
            }
        }

        // Настраиваем BottomNavigationView с контроллером навигации
        bottomNavView.setupWithNavController(navController)
    }
}
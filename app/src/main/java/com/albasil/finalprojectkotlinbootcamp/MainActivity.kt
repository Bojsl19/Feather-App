package com.albasil.finalprojectkotlinbootcamp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

/*
class MainActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.fragment_sign_in)
    }
}
*/



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)




        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.sign_in -> {
                    bottomNavView.visibility = View.GONE
                }
                R.id.signUP -> {
                    bottomNavView.visibility = View.GONE
                }
                else -> {
                    bottomNavView.visibility = View.VISIBLE
                }
            }
        }


    }
}
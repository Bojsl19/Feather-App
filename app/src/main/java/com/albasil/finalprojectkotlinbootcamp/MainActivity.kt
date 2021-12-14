package com.albasil.finalprojectkotlinbootcamp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

private lateinit var settings : SharedPreferences
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

       // setLocate("ar")

       // loadLocate()


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.sign_in -> {
                    bottomNavView.visibility = View.GONE
                    //bottomNavView.visibility = View.VISIBLE
                }
                R.id.signUP -> {
                    bottomNavView.visibility = View.GONE
                }
              /*  R.id.addArticle ->{
                    bottomNavView.visibility = View.GONE

                }*/
                else -> {
                    bottomNavView.visibility = View.VISIBLE
                }
            }
        }




    }


    private fun setLocate(Lang:String){
        val locale = Locale("ar")

        Locale.setDefault(locale)

        val config = Configuration()

        config.locale = locale

        //---------------------------------------------------------------
        this?.resources?.updateConfiguration(config, this.resources.displayMetrics)


        settings = this.getSharedPreferences("Settings", Context.MODE_PRIVATE)

        settings.edit()
        settings.getString("My_lang","ar".toString())
        settings.apply{}

    }

    private fun loadLocate(){


        val sharedPreferences= this.getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        val language = sharedPreferences.getString("My_lang","")

        setLocate(language.toString())

    }


}
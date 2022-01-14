package com.supersuman.gitamtransit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.supersuman.gitamtransit.adapters.RoutesAdapter
import com.supersuman.gitamtransit.fragments.FavouritesFragment
import com.supersuman.gitamtransit.fragments.RoutesFragment
import com.supersuman.gitamtransit.fragments.SearchFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.*

val coroutineScope = CoroutineScope(Dispatchers.IO)

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView : BottomNavigationView
    private lateinit var navigationView : NavigationView
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var routesFragment : RoutesFragment
    private lateinit var searchFragment : SearchFragment
    private lateinit var favouritesFragment: FavouritesFragment
    private lateinit var menuButton: MaterialButton
    private lateinit var title : TextView
    private lateinit var searchButton: MaterialButton

    private val frameLayout = R.id.mainActivityFrameLayout
    private val routeMenu = R.id.routeMenu
    private val favouritesMenu = R.id.favouritesMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initListeners()
        setFragment()
    }

    private fun initViews() {
        title = findViewById(R.id.mainActivityTitle)
        navigationView = findViewById(R.id.mainActivityNavigationView)
        drawerLayout = findViewById(R.id.mainActivityDrawerLayout)
        bottomNavigationView = findViewById(R.id.mainActivityBottomNavigationView)
        menuButton = findViewById(R.id.mainActivityMenuButton)
        searchButton = findViewById(R.id.mainActivitySearchButton)

        routesFragment = RoutesFragment()
        searchFragment = SearchFragment()
        favouritesFragment = FavouritesFragment()
    }


    private fun setFragment() {
        val manager = supportFragmentManager.beginTransaction()
        manager.replace(frameLayout, routesFragment)
        manager.commit()
        title.text = "Bus Routes"
    }

    private fun initListeners() {

        menuButton.setOnClickListener {
            drawerLayout.open()
        }

        searchButton.setOnClickListener {
            val manager = supportFragmentManager.beginTransaction()
            manager.replace(frameLayout, searchFragment)
            manager.commit()
            title.text = "Search"
            bottomNavigationView.menu.getItem(2).isChecked = true
        }

        navigationView.setNavigationItemSelectedListener {
            drawerLayout.close()
            when(it.itemId){
                R.id.repository_item -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/supersu-man/GitamTransit"))
                    startActivity(browserIntent)
                }
                R.id.contribute_item -> {
                    val intent = Intent(this, ContributionActivity::class.java)
                    startActivity(intent)
                }
                R.id.telegram_item -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+UEEaRZf9YBhlNzRl"))
                    startActivity(browserIntent)
                }
            }
            true
        }

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                routeMenu -> {
                    val manager = supportFragmentManager.beginTransaction()
                    manager.replace(frameLayout, routesFragment)
                    manager.commit()
                    title.text = "Bus Routes"
                }
                favouritesMenu -> {
                    val manager = supportFragmentManager.beginTransaction()
                    manager.replace(frameLayout, favouritesFragment)
                    manager.commit()
                    title.text = "Favourites"
                }
            }
            true
        }
    }

}
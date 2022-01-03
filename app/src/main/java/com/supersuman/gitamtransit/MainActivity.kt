package com.supersuman.gitamtransit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

val coroutineScope = CoroutineScope(Dispatchers.IO)

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView : BottomNavigationView
    private lateinit var routesFragment : RoutesFragment
    private lateinit var searchFragment: SearchFragment
    private val mainFragment = R.id.mainFragment
    private val routeMenu = R.id.routeMenu
    private val searchMenu = R.id.searchMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        modifyViews()
        initListeners()
    }

    private fun initViews() {
        navigationView = findViewById(R.id.navigationView)
        routesFragment = RoutesFragment()
        searchFragment = SearchFragment()

    }

    private fun modifyViews() {
        val manager = supportFragmentManager.beginTransaction()
        manager.replace(mainFragment, routesFragment)
        manager.commit()
    }

    private fun initListeners() {
        navigationView.setOnItemSelectedListener {
            when(it.itemId){
                routeMenu -> {
                    val manager = supportFragmentManager.beginTransaction()
                    manager.replace(mainFragment, routesFragment)
                    manager.commit()
                }
                searchMenu -> {
                    val manager = supportFragmentManager.beginTransaction()
                    manager.replace(mainFragment, searchFragment)
                    manager.commit()
                }
            }
            true
        }
    }

}
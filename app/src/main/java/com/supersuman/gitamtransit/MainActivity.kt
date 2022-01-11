package com.supersuman.gitamtransit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
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
import com.supersuman.gitamtransit.fragments.RoutesFragment
import com.supersuman.gitamtransit.fragments.SearchFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private lateinit var progressBar : CircularProgressIndicator
    private lateinit var textView : TextView
    private lateinit var mPrefs : SharedPreferences
    private lateinit var menuButton: MaterialButton
    private lateinit var title : TextView

    private val frameLayout = R.id.mainActivityFrameLayout
    private val routeMenu = R.id.routeMenu
    private val searchMenu = R.id.searchMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        getData()
        initListeners()
    }

    private fun initViews() {
        title = findViewById(R.id.mainActivityTitle)
        navigationView = findViewById(R.id.mainActivityNavigationView)
        drawerLayout = findViewById(R.id.mainActivityDrawerLayout)
        bottomNavigationView = findViewById(R.id.mainActivityBottomNavigationView)
        routesFragment = RoutesFragment()
        searchFragment = SearchFragment()
        progressBar = findViewById(R.id.progressbar)
        textView = findViewById(R.id.mainActivityTextView)
        mPrefs = getPreferences(Context.MODE_PRIVATE)
        menuButton = findViewById(R.id.mainActivityMenuButton)
    }

    private fun getData(){
        if (!mPrefs.contains("RoutesDataList")){
            coroutineScope.launch {
                runOnUiThread{ showProgressBar() }
                val list = getAssetsData()
                saveData(list, "RoutesDataList")
                runOnUiThread {
                    cancelProgressBar()
                    setFragment()
                }
            }
        }
        else{
            setFragment()
        }
    }

    private fun getAssetsData(): MutableList<RoutesData> {
        val jsonString = assets.open("routesTesting.json").bufferedReader().use { it.readText() }
        val routes = JSONArray(jsonString)
        val data = mutableListOf<RoutesData>()
        for (i in 0 until routes.length()) {
            val it = routes.getJSONObject(i)
            val busName = it.get("busName").toString()
            val startPoint = it.get("startPoint").toString()
            val route = JSONArray(it.get("route").toString())
            val coordinatesDetailsList = getCoordinatesDetails(route)
            val busInfo = RoutesData(busName, startPoint, it.get("route").toString(), coordinatesDetailsList)
            data.add(busInfo)
        }
        return data
    }

    private fun getCoordinatesDetails(route: JSONArray): MutableList<String> {
        val mutableList = mutableListOf<String>()
        val geocoder = Geocoder (this, Locale.getDefault())
        var previousLocality = ""
        var previousThoroughfare = ""
        for (i in 0 until route.length()){
            if (i%3 != 0) continue
            val latitude = route.getJSONArray(i).getDouble(0)
            val longitude = route.getJSONArray(i).getDouble(1)
            val addresses : List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
            val locality = addresses[0].locality
            val thoroughfare = addresses[0].thoroughfare
            if (locality != null && locality != previousLocality && locality !in mutableList){
                previousLocality = locality
                mutableList.add(locality)
            }
            if (thoroughfare != null && thoroughfare != previousThoroughfare && thoroughfare !in mutableList){
                previousThoroughfare = thoroughfare
                mutableList.add(thoroughfare)
            }
        }
        return mutableList
    }

    private fun saveData(list: MutableList<RoutesData>, key: String){
        val prefsEditor = mPrefs.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        prefsEditor?.putString(key, json)
        prefsEditor?.apply()
    }

    private fun setFragment() {
        val manager = supportFragmentManager.beginTransaction()
        manager.replace(frameLayout, routesFragment)
        manager.commit()
        title.text = "Bus Routes"
    }

    private fun showProgressBar() {
        progressBar.isIndeterminate = true
        textView.visibility = View.VISIBLE
    }

    private fun cancelProgressBar() {
        progressBar.isIndeterminate = false
        textView.visibility = View.INVISIBLE
    }

    private fun initListeners() {

        menuButton.setOnClickListener {
            drawerLayout.open()
        }

        navigationView.setNavigationItemSelectedListener {
            drawerLayout.close()
            when(it.itemId){
                R.id.contribute_item -> {
                    val intent = Intent(this, ContributionActivity::class.java)
                    startActivity(intent)
                }
                R.id.telegram_item -> {
                    Toast.makeText(this, "Coming Soon...", Toast.LENGTH_SHORT).show()
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
                searchMenu -> {
                    val manager = supportFragmentManager.beginTransaction()
                    manager.replace(frameLayout, searchFragment)
                    manager.commit()
                    title.text = "Search"
                }
            }
            true
        }
    }

}
package com.supersuman.gitamtransit

import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.*
import kotlin.concurrent.thread

val coroutineScope = CoroutineScope(Dispatchers.IO)

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView : BottomNavigationView
    private lateinit var routesFragment : RoutesFragment
    private lateinit var searchFragment : SearchFragment
    private lateinit var progressBar : CircularProgressIndicator
    private lateinit var textView : TextView
    private lateinit var mPrefs : SharedPreferences

    private val mainFragment = R.id.mainFragment
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
        navigationView = findViewById(R.id.navigationView)
        routesFragment = RoutesFragment()
        searchFragment = SearchFragment()
        progressBar = findViewById(R.id.progressbar)
        textView = findViewById(R.id.mainActivityTextView)
        mPrefs = getPreferences(Context.MODE_PRIVATE)
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
        manager.replace(mainFragment, routesFragment)
        manager.commit()
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
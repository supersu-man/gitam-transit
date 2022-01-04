package com.supersuman.gitamtransit


import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.supersuman.gitamtransit.databinding.ActivityMapsBinding
import org.json.JSONArray

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var floatingActionButton: FloatingActionButton
    private val list = mutableListOf<LatLng>()
    private var boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val route = JSONArray(intent.getStringExtra("route").toString())
        getRoute(route)
        floatingActionButton = findViewById(R.id.floatingActionButton)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        addRoute()
        initListeners()

    }

    private fun getRoute(route: JSONArray) {
        list.clear()
        for(i in 0 until route.length()){
            val latitude = route.getJSONArray(i).getDouble(0)
            val longitude = route.getJSONArray(i).getDouble(1)
            list.add(LatLng(latitude, longitude))
        }
    }

    private fun addRoute(){
        mMap.addPolyline(PolylineOptions()
            .addAll(list)
            .width(13f)
            .color(Color.RED)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getCenterPointInPolygon(list), 10.0f))
    }

    private fun initListeners() {
        floatingActionButton.setOnClickListener {
            if (boolean){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getCenterPointInPolygon(list), 10.0f))
                boolean = false
            }
            else{
                val stop = list[0]
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stop, 15.0f))
                boolean = true
            }
        }
    }

    private fun getCenterPointInPolygon(list: MutableList<LatLng>): LatLng {
        val latLngBounds = LatLngBounds.builder()
        for (latLng in list) {
            latLngBounds.include(latLng)
        }
        return latLngBounds.build().center
    }
}

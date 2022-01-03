package com.supersuman.gitamtransit

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amalbit.trail.OverlayMarker
import com.amalbit.trail.Route
import com.amalbit.trail.RouteOverlayView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.supersuman.gitamtransit.databinding.ActivityMapsBinding
import org.json.JSONArray
import com.google.android.gms.maps.model.PolylineOptions

import com.google.android.gms.maps.model.Polyline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.gms.maps.model.LatLngBounds





class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var route : JSONArray


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        route = JSONArray(intent.getStringExtra("route").toString())

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        addRoute(mMap, route)

    }

    private fun addRoute(mMap: GoogleMap, route: JSONArray){
        var latitude = route.getJSONArray(0).getDouble(0)
        var longitude = route.getJSONArray(0).getDouble(1)
        val stop = LatLng(latitude, longitude)


        val list = mutableListOf<LatLng>()
        for(i in 0 until route.length()){
            latitude = route.getJSONArray(i).getDouble(0)
            longitude = route.getJSONArray(i).getDouble(1)
            list.add(LatLng(latitude, longitude))
        }

        mMap.addPolyline(PolylineOptions()
            .addAll(list)
            .width(13f)
            .color(Color.RED)
        )

        mMap.moveCamera(CameraUpdateFactory.zoomTo(10.0f))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(getCenterPointInPolygon(list)))
    }

    private fun getCenterPointInPolygon(list: MutableList<LatLng>): LatLng {
        val latLngBounds = LatLngBounds.builder()
        for (latLng in list) {
            latLngBounds.include(latLng)
        }
        return latLngBounds.build().center
    }
}

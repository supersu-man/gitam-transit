package com.supersuman.gitamtransit.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.supersuman.gitamtransit.R
import com.supersuman.gitamtransit.RoutesData
import com.supersuman.gitamtransit.adapters.RoutesAdapter
import com.supersuman.gitamtransit.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.lang.reflect.Type

class FavouritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private val data = mutableListOf<RoutesData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        modifyViews()
        getLatestData()
        initListeners()
    }

    private fun initViews() {
        recyclerView = requireActivity().findViewById(R.id.favouritesFragmentRecyclerView)
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
    }

    private fun modifyViews() {
        val linearManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.VERTICAL, false
        )
        recyclerView.layoutManager = linearManager
        recyclerView.adapter = RoutesAdapter(data, requireActivity())
    }


    private fun initListeners(){
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "Stars" && activity != null){
                getLatestData()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLatestData() = coroutineScope.launch {
        val jsonString =
            khttp.get("https://raw.githubusercontent.com/supersu-man/GitamTransit/main/assets/busRoutes.json").text
        val routes = JSONArray(jsonString)
        data.clear()
        val starList = getStarsList()
        for (i in 0 until routes.length()) {
            val it = routes.getJSONObject(i)
            val busName = it.get("busName") as String
            if(busName !in starList) continue
            val startPoint = it.get("startPoint") as String
            val busInfo = RoutesData(busName, startPoint, it.get("route").toString(), mutableListOf())
            data.add(busInfo)
        }
        activity?.runOnUiThread {
            recyclerView.recycledViewPool.clear()
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun getStarsList(): MutableList<String> {
        val gson = Gson()
        val string = sharedPreferences.getString("Stars", "")
        if(string=="") return mutableListOf()
        val type: Type = object : TypeToken<MutableList<String?>?>() {}.type
        val starsList : MutableList<String> =  gson.fromJson(string, type)
        return starsList
    }
}
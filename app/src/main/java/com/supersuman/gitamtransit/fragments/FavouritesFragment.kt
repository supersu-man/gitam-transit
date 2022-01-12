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
import java.lang.reflect.Type

class FavouritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private val newData = mutableListOf<RoutesData>()

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
        setData()
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
        recyclerView.adapter = RoutesAdapter(newData, requireActivity())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData(){
        val data = getData()
        val starList = getStarsList()
        newData.clear()
        for (i in data){
            if (i.busName in starList){
                newData.add(i)
            }
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun initListeners(){
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "Stars" && activity != null){
                setData()
            }
        }
    }

    private fun getData(): MutableList<RoutesData> {
        val gson = Gson()
        val json = sharedPreferences.getString("RoutesDataList", "")
        val type: Type = object : TypeToken<MutableList<RoutesData?>?>() {}.type
        val data: MutableList<RoutesData> = gson.fromJson(json, type)
        return data
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
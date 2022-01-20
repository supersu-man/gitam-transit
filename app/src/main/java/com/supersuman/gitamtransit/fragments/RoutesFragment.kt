package com.supersuman.gitamtransit.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.json.JSONArray
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.supersuman.gitamtransit.R
import com.supersuman.gitamtransit.RoutesData
import com.supersuman.gitamtransit.adapters.RoutesAdapter
import com.supersuman.gitamtransit.coroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.reflect.Type


class RoutesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mPrefs : SharedPreferences
    private val data = mutableListOf<RoutesData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        modifyViews()
        getLatestData()

    }

    private fun initViews() {
        recyclerView = requireActivity().findViewById(R.id.recyclerView)
        mPrefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
    }

    private fun modifyViews() {
        val linearManager =LinearLayoutManager(requireActivity(),
            LinearLayoutManager.VERTICAL,false)
        recyclerView.layoutManager = linearManager
        recyclerView.adapter = RoutesAdapter(data, requireActivity())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLatestData() = coroutineScope.launch {
        val jsonString =
            khttp.get("https://raw.githubusercontent.com/supersu-man/GitamTransit/main/assets/busRoutes.json").text
        val routes = JSONArray(jsonString)
        data.clear()
        for (i in 0 until routes.length()) {
            val it = routes.getJSONObject(i)
            val busName = it.get("busName") as String
            val startPoint = it.get("startPoint") as String
            val busInfo = RoutesData(busName, startPoint, it.get("route").toString(), mutableListOf())
            data.add(busInfo)
        }

        activity?.runOnUiThread {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}
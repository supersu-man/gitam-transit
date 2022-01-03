package com.supersuman.gitamtransit

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


class RoutesFragment : Fragment() {



    private lateinit var recyclerView: RecyclerView
    private var data = mutableListOf<RoutesData>()

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
        getData()
        //getLatestData()
    }

    private fun initViews() {
        recyclerView = requireActivity().findViewById(R.id.recyclerView)
    }
    private fun modifyViews() {
        val linearManager =LinearLayoutManager(requireActivity().applicationContext,
            LinearLayoutManager.VERTICAL,false)
        recyclerView.layoutManager = linearManager
        recyclerView.adapter = RoutesAdapter(data, requireActivity())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        val jsonString = requireContext().assets.open("routesTesting.json").bufferedReader().use { it.readText() }
        val routes = JSONArray(jsonString)
        for(i in 0 until routes.length()){
            val it = routes.getJSONObject(i)
            val busName = it.get("busName") as String
            val startPoint = it.get("startPoint") as String
            val busInfo = RoutesData(busName, startPoint, it.get("route").toString())
            data.add(busInfo)
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLatestData() = coroutineScope.launch {
        val jsonString =
            khttp.get("https://raw.githubusercontent.com/supersu-man/GitamTransit/main/app/src/main/assets/routesTesting.json").text
        val routes = JSONArray(jsonString)
        for (i in 0 until routes.length()) {
            val it = routes.getJSONObject(i)
            val busName = it.get("busName") as String
            val startPoint = it.get("startPoint") as String
            val busInfo = RoutesData(busName, startPoint, it.get("route") as String)
            data.add(busInfo)
        }
        requireActivity().runOnUiThread {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}
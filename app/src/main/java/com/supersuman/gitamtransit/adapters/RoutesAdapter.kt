package com.supersuman.gitamtransit.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.supersuman.gitamtransit.MapsActivity
import com.supersuman.gitamtransit.R
import com.supersuman.gitamtransit.helpers.RoutesData
import com.supersuman.gitamtransit.helpers.getStarsList
import com.supersuman.gitamtransit.helpers.updateStarsList

class RoutesAdapter(private val data: MutableList<RoutesData>, private val requireActivity: FragmentActivity) : RecyclerView.Adapter<RoutesAdapter.ViewHolder>() {

    private val sharedPreferences: SharedPreferences = requireActivity.getPreferences(Context.MODE_PRIVATE)

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val textView1: TextView = view.findViewById(R.id.eachRouteTextView1)
        val textview2: TextView = view.findViewById(R.id.eachRouteTextView2)
        val card: MaterialCardView = view.findViewById(R.id.eachRouteCardView)
        val starButton : ImageView = view.findViewById(R.id.eachRouteStarButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.each_route,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val busName = data[position].busName
        val startPoint = data[position].startPoint
        val route = data[position].route

        holder.textView1.text = busName
        holder.textview2.text = startPoint
        holder.card.setOnClickListener {
            val intent = Intent(requireActivity, MapsActivity::class.java)
            intent.putExtra("route", route)
            requireActivity.startActivity((intent))
        }

        setStar(busName, getStarsList(sharedPreferences), holder)

        holder.starButton.setOnClickListener {
            val newStarsList = getStarsList(sharedPreferences)
            if (busName in newStarsList){
                it.setBackgroundResource(R.drawable.ic_baseline_star_outline_24)
                newStarsList.remove(busName)
                updateStarsList(newStarsList,sharedPreferences)
            } else{
                it.setBackgroundResource(R.drawable.ic_baseline_star_24)
                newStarsList.add(busName)
                updateStarsList(newStarsList,sharedPreferences)
            }
        }
    }

    private fun setStar(busName: String, starsList: MutableList<String>, holder: ViewHolder) {
        if (busName in starsList){
            holder.starButton.setBackgroundResource(R.drawable.ic_baseline_star_24)
        } else{
            holder.starButton.setBackgroundResource(R.drawable.ic_baseline_star_outline_24)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
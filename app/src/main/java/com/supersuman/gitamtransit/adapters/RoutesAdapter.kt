package com.supersuman.gitamtransit.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.supersuman.gitamtransit.MapsActivity
import com.supersuman.gitamtransit.R
import com.supersuman.gitamtransit.RoutesData

class RoutesAdapter(private val data: MutableList<RoutesData>, private val requireActivity: FragmentActivity) : RecyclerView.Adapter<RoutesAdapter.ViewHolder>() {
    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val textView1: TextView = view.findViewById(R.id.textView1)
        val textview2: TextView = view.findViewById(R.id.textView2)
        val card: MaterialCardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.each_route,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView1.text = data[position].busName
        holder.textview2.text = data[position].startPoint
        val route = data[position].route
        holder.card.setOnClickListener {
            val intent = Intent(requireActivity, MapsActivity::class.java)
            intent.putExtra("route", route)
            requireActivity.startActivity((intent))
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
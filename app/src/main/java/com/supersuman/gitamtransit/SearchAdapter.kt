package com.supersuman.gitamtransit

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class SearchAdapter(private val data: MutableList<NewData>, private val requireActivity: FragmentActivity) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val textView1 : TextView = view.findViewById(R.id.textView1)
        val textView2 : TextView = view.findViewById(R.id.textView2)
        val parentLayout : LinearLayout = view.findViewById(R.id.eachRouteParentLayout)
        val cardView : MaterialCardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.each_route,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView1.text = data[position].busName
        holder.textView2.visibility = View.GONE
        for (i in data[position].results){
            val textView = TextView(requireActivity)
            val padding = holder.textView1.paddingLeft
            textView.setPadding(padding, padding, padding, padding)
            textView.text = i
            holder.parentLayout.addView(textView)
        }
        val route = data[position].route
        holder.cardView.setOnClickListener {
            val intent = Intent(requireActivity, MapsActivity::class.java)
            intent.putExtra("route", route)
            requireActivity.startActivity((intent))
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
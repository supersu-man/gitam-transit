package com.supersuman.gitamtransit.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.supersuman.gitamtransit.MapsActivity
import com.supersuman.gitamtransit.R
import com.supersuman.gitamtransit.helpers.RoutesData
import com.supersuman.gitamtransit.helpers.getStarsList
import com.supersuman.gitamtransit.helpers.updateStarsList

class SearchAdapter(private val data: MutableList<RoutesData>, private val requireActivity: FragmentActivity) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private val sharedPreferences: SharedPreferences = requireActivity.getPreferences(Context.MODE_PRIVATE)

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val textView1 : TextView = view.findViewById(R.id.eachSearchTextView1)
        val linearLayout : LinearLayout = view.findViewById(R.id.eachSearchLinearLayout)
        val cardView : MaterialCardView = view.findViewById(R.id.eachSearchCardView)
        val starButton : ImageView = view.findViewById(R.id.eachSearchStarButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.each_search,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val busName = data[position].busName
        holder.textView1.text = busName
        holder.linearLayout.removeAllViews()
        for (i in data[position].keywords){
            val textView = getNewTextView(holder.textView1, i)
            holder.linearLayout.addView(textView)
        }
        val route = data[position].route
        holder.cardView.setOnClickListener {
            val intent = Intent(requireActivity, MapsActivity::class.java)
            intent.putExtra("route", route)
            requireActivity.startActivity(intent)
        }

        val starsList = getStarsList(sharedPreferences)
        setAllStars(busName, starsList, holder)
        holder.starButton.setOnClickListener {
            if (busName in starsList){
                it.setBackgroundResource(R.drawable.ic_baseline_star_outline_24)
                starsList.remove(busName)
                updateStarsList(starsList, sharedPreferences)
            } else{
                it.setBackgroundResource(R.drawable.ic_baseline_star_24)
                starsList.add(busName)
                updateStarsList(starsList, sharedPreferences)
            }
        }
    }

    private fun getNewTextView(previousTextView: TextView, i: String): TextView {
        val textView = TextView(requireActivity)
        val padding = previousTextView.paddingLeft
        textView.setPadding(padding, padding, padding, padding)
        textView.text = i
        return textView
    }

    private fun setAllStars(busName: String, starsList: MutableList<String>, holder: ViewHolder) {
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